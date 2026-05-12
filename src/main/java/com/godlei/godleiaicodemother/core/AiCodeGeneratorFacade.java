package com.godlei.godleiaicodemother.core;


import cn.hutool.json.JSONUtil;
import com.godlei.godleiaicodemother.ai.AiCodeGeneratorService;
import com.godlei.godleiaicodemother.ai.AiCodeGeneratorServiceFactory;
import com.godlei.godleiaicodemother.ai.model.AppNameResult;
import com.godlei.godleiaicodemother.ai.model.HtmlCodeResult;
import com.godlei.godleiaicodemother.ai.model.MultiFileCodeResult;
import com.godlei.godleiaicodemother.ai.model.message.AiResponseMessage;
import com.godlei.godleiaicodemother.ai.model.message.ToolExecutedMessage;
import com.godlei.godleiaicodemother.ai.model.message.ToolRequestMessage;
import com.godlei.godleiaicodemother.constant.AppConstant;
import com.godlei.godleiaicodemother.core.builder.VueProjectBuilder;
import com.godlei.godleiaicodemother.core.parser.CodeParserExecutor;
import com.godlei.godleiaicodemother.core.saver.CodeFileSaverExecutor;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI code generation facade.
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    public String generateAppName(String userMessage) {
        if (userMessage == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户提示词不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.AiCodeGeneratorService();
        AppNameResult result = aiCodeGeneratorService.generateAppName(userMessage);
        return result.getAppName();
    }

    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.AiCodeGeneratorService();
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型: " + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }

        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型: " + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> {
            AtomicReference<StreamingHandle> streamingHandleRef = new AtomicReference<>();
            AtomicBoolean cancellationRequested = new AtomicBoolean(false);
            AtomicBoolean terminated = new AtomicBoolean(false);

            sink.onCancel(() -> cancelStreamingHandle(streamingHandleRef, cancellationRequested));
            sink.onDispose(() -> cancelStreamingHandle(streamingHandleRef, cancellationRequested));

            tokenStream
                    .onPartialResponseWithContext((partialResponse, context) -> {
                        cacheStreamingHandle(streamingHandleRef, context.streamingHandle());
                        emitAiResponseChunk(sink, partialResponse.text());
                    })
                    .onPartialThinkingWithContext((partialThinking, context) ->
                            cacheStreamingHandle(streamingHandleRef, context.streamingHandle()))
                    .onPartialToolCallWithContext((partialToolCall, context) ->
                            cacheStreamingHandle(streamingHandleRef, context.streamingHandle()))
                    .beforeToolExecution(beforeToolExecution -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(beforeToolExecution.request());
                        if (!sink.isCancelled()) {
                            sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                        }
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        if (!sink.isCancelled()) {
                            sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                        }
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        if (!terminated.compareAndSet(false, true)) {
                            return;
                        }
                        try {
                            String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                            vueProjectBuilder.buildProject(projectPath);
                            if (!sink.isCancelled()) {
                                sink.complete();
                            }
                        } catch (Throwable error) {
                            log.error("Vue project build failed, appId: {}", appId, error);
                            if (!sink.isCancelled()) {
                                sink.error(error);
                            }
                        }
                    })
                    .onError((Throwable error) -> {
                        if (isIgnorableClosedStreamError(error, cancellationRequested.get(), sink.isCancelled(), streamingHandleRef.get())) {
                            log.debug("Ignore closed error after SSE cancellation, appId: {}", appId, error);
                            if (terminated.compareAndSet(false, true) && !sink.isCancelled()) {
                                sink.complete();
                            }
                            return;
                        }
                        if (!terminated.compareAndSet(false, true)) {
                            return;
                        }
                        log.error("TokenStream execution failed, appId: {}", appId, error);
                        if (!sink.isCancelled()) {
                            sink.error(error);
                        }
                    })
                    .start();
        });
    }

    private static boolean isIgnorableClosedStreamError(Throwable error,
                                                        boolean cancellationRequested,
                                                        boolean sinkCancelled,
                                                        StreamingHandle streamingHandle) {
        if (!(cancellationRequested || sinkCancelled || isStreamingHandleCancelled(streamingHandle))) {
            return false;
        }
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains("closed")) {
                return true;
            }
            if (current instanceof IOException && "closed".equalsIgnoreCase(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void emitAiResponseChunk(FluxSink<String> sink, String partialResponse) {
        if (sink.isCancelled() || partialResponse == null || partialResponse.isEmpty()) {
            return;
        }
        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
    }

    private static void cacheStreamingHandle(AtomicReference<StreamingHandle> streamingHandleRef,
                                             StreamingHandle streamingHandle) {
        if (streamingHandle != null) {
            streamingHandleRef.compareAndSet(null, streamingHandle);
        }
    }

    private static void cancelStreamingHandle(AtomicReference<StreamingHandle> streamingHandleRef,
                                              AtomicBoolean cancellationRequested) {
        cancellationRequested.set(true);
        StreamingHandle streamingHandle = streamingHandleRef.get();
        if (streamingHandle != null && !streamingHandle.isCancelled()) {
            streamingHandle.cancel();
        }
    }

    private static boolean isStreamingHandleCancelled(StreamingHandle streamingHandle) {
        return streamingHandle != null && streamingHandle.isCancelled();
    }

    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            try {
                String completeCode = codeBuilder.toString();
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，目录为: {}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }
}
