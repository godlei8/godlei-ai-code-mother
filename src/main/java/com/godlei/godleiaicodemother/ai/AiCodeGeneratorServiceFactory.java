package com.godlei.godleiaicodemother.ai;

import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.godlei.godleiaicodemother.ai.guardrail.PromptSafetyInputGuardrail;
import com.godlei.godleiaicodemother.ai.model.AppNameResult;
import com.godlei.godleiaicodemother.ai.model.HtmlCodeResult;
import com.godlei.godleiaicodemother.ai.model.MultiFileCodeResult;
import com.godlei.godleiaicodemother.ai.tools.ToolManager;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.model.enums.CodeGenTypeEnum;
import com.godlei.godleiaicodemother.service.ChatHistoryService;
import com.godlei.godleiaicodemother.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Factory for creating AiCodeGeneratorService instances.
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    private static final String OPEN_AI_CHAT_MODEL_BEAN_NAME = "openAiChatModel";
    private static final String STREAMING_CHAT_MODEL_BEAN_NAME = "streamingChatModelPrototype";
    private static final String REASONING_STREAMING_CHAT_MODEL_BEAN_NAME = "reasoningStreamingChatModelPrototype";
    private static final int CHAT_MEMORY_MAX_MESSAGES = 20;
    private static final int MAX_SEQUENTIAL_TOOL_INVOCATIONS = 20;

    @Resource(name = OPEN_AI_CHAT_MODEL_BEAN_NAME)
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) ->
                    log.debug("AI service removed from cache, key: {}, cause: {}", key, cause))
            .build();

    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        if (codeGenType == CodeGenTypeEnum.VUE_PROJECT) {
            return createAiCodeGeneratorService(appId, codeGenType);
        }
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("Create AI service for appId: {}, codeGenType: {}", appId, codeGenType);

        MessageWindowChatMemory chatMemory = createPersistentChatMemory(appId);

        return switch (codeGenType) {
            case VUE_PROJECT -> {
                resetInvalidVueChatMemoryIfNeeded(appId, chatMemory);
                yield buildVueProjectService(chatMemory);
            }
            case HTML, MULTI_FILE -> {
                chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, CHAT_MEMORY_MAX_MESSAGES);
                yield buildStatefulService(chatMemory);
            }
            default -> throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "不支持的代码生成类型: " + codeGenType.getValue()
            );
        };
    }

    @Bean
    public AiCodeGeneratorService AiCodeGeneratorService() {
        AiCodeGeneratorService statelessService = createStatelessAiCodeGeneratorService();
        return new AiCodeGeneratorService() {
            @Override
            public HtmlCodeResult generateHtmlCode(String userMessage) {
                return statelessService.generateHtmlCode(userMessage);
            }

            @Override
            public MultiFileCodeResult generateMultiFileCode(String userMessage) {
                return statelessService.generateMultiFileCode(userMessage);
            }

            @Override
            public Flux<String> generateHtmlCodeStream(String userMessage) {
                return statelessService.generateHtmlCodeStream(userMessage);
            }

            @Override
            public Flux<String> generateMultiFileCodeStream(String userMessage) {
                return statelessService.generateMultiFileCodeStream(userMessage);
            }

            @Override
            public AppNameResult generateAppName(String userMessage) {
                return statelessService.generateAppName(userMessage);
            }

            @Override
            public TokenStream generateVueProjectCodeStream(long appId, String userMessage) {
                return statelessService.generateVueProjectCodeStream(appId, userMessage);
            }
        };
    }

    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }

    private MessageWindowChatMemory createPersistentChatMemory(long appId) {
        return MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(CHAT_MEMORY_MAX_MESSAGES)
                .build();
    }

    private AiCodeGeneratorService buildVueProjectService(MessageWindowChatMemory chatMemory) {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(getBean(REASONING_STREAMING_CHAT_MODEL_BEAN_NAME, StreamingChatModel.class))
                .chatMemoryProvider(memoryId -> chatMemory)
                .tools(toolManager.getAllTools())
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(
                                toolExecutionRequest,
                                "Error: there is no tool called " + toolExecutionRequest.name()
                        )
                )
                .maxSequentialToolsInvocations(20) // 最多调用20次工具
                .maxSequentialToolsInvocations(MAX_SEQUENTIAL_TOOL_INVOCATIONS)
                .inputGuardrails(new PromptSafetyInputGuardrail()) // 添加护轨
                // .outputGuardrails(new RetryOutputGuardrail()) // 添加输出护轨，为了流式输出，这里不使用
                .build();
    }

    private AiCodeGeneratorService buildStatefulService(MessageWindowChatMemory chatMemory) {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(getBean(STREAMING_CHAT_MODEL_BEAN_NAME, StreamingChatModel.class))
                .chatMemory(chatMemory)
                .maxSequentialToolsInvocations(20) // 最多调用20次工具
                .inputGuardrails(new PromptSafetyInputGuardrail()) // 添加护轨
                //  .outputGuardrails(new RetryOutputGuardrail()) // 添加输出护轨，为了流式输出，这里不使用
                .build();
    }

    private void resetInvalidVueChatMemoryIfNeeded(long appId, MessageWindowChatMemory chatMemory) {
        List<ChatMessage> messages = chatMemory.messages();
        if (!hasInvalidVueChatMemory(messages)) {
            return;
        }
        log.warn("appId: {} detected invalid Vue chat memory, clearing Redis chat memory for recovery", appId);
        chatMemory.clear();
    }

    static boolean hasInvalidVueChatMemory(List<ChatMessage> messages) {
        return hasIncompleteReasoningMessages(messages) || hasIncompleteToolExecutionMessages(messages);
    }

    static boolean hasIncompleteReasoningMessages(List<ChatMessage> messages) {
        return messages.stream()
                .filter(AiMessage.class::isInstance)
                .map(AiMessage.class::cast)
                .anyMatch(aiMessage -> StrUtil.isBlank(aiMessage.thinking()));
    }

    static boolean hasIncompleteToolExecutionMessages(List<ChatMessage> messages) {
        Set<String> pendingToolCallIds = new HashSet<>();
        for (ChatMessage message : messages) {
            if (message instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
                if (!pendingToolCallIds.isEmpty()) {
                    return true;
                }
                for (var toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                    if (StrUtil.isBlank(toolExecutionRequest.id())) {
                        return true;
                    }
                    pendingToolCallIds.add(toolExecutionRequest.id());
                }
                continue;
            }
            if (message instanceof ToolExecutionResultMessage toolExecutionResultMessage) {
                if (pendingToolCallIds.isEmpty() || StrUtil.isBlank(toolExecutionResultMessage.id())) {
                    return true;
                }
                if (!pendingToolCallIds.remove(toolExecutionResultMessage.id())) {
                    return true;
                }
                continue;
            }
            if (!pendingToolCallIds.isEmpty()) {
                return true;
            }
        }
        return !pendingToolCallIds.isEmpty();
    }

    private AiCodeGeneratorService createStatelessAiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(getBean(STREAMING_CHAT_MODEL_BEAN_NAME, StreamingChatModel.class))
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(CHAT_MEMORY_MAX_MESSAGES))
                .build();
    }

    private <T> T getBean(String beanName, Class<T> beanType) {
        return SpringContextUtil.getBean(beanName, beanType);
    }
}
