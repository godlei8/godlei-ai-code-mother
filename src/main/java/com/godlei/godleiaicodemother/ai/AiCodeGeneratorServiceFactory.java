package com.godlei.godleiaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiCodeGeneratorServiceFactory 配置类
 * 用于创建和配置AiCodeGeneratorService的Bean实例
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    // 注入ChatModel，用于AI对话模型
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建AiCodeGeneratorService的Bean实例
     * @return 配置好的AiCodeGeneratorService实例
     */
    @Bean
    public AiCodeGeneratorService AiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
