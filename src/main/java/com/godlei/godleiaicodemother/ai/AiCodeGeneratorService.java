package com.godlei.godleiaicodemother.ai;

import com.godlei.godleiaicodemother.ai.model.AppNameResult;
import com.godlei.godleiaicodemother.ai.model.HtmlCodeResult;
import com.godlei.godleiaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI代码生成服务接口
 * 该接口定义了AI生成代码的基本功能规范
 */
public interface AiCodeGeneratorService {
    /**
     * 根据用户输入的消息生成Html代码
     *
     * @param userMessage 用户输入的消息，包含代码生成的需求描述
     * @return 生成的代码字符串
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 根据用户输入的消息生成多文件代码
     *
     * @param userMessage 用户输入的消息，包含代码生成的需求描述
     * @return 生成的代码字符串
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 根据用户消息生成HTML代码流的结果
     * 使用系统提示文件中的配置来指导代码生成
     *
     * @param userMessage 用户输入的消息，将作为生成HTML代码的依据
     * @return HtmlCodeResult 包含生成HTML代码的结果对象
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);



    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);


    /**
     * 根据用户消息生成应用命名系统
     * 使用系统提示文件中的配置来指导代码生成
     *
     * @param userMessage 用户输入的消息，将作为生成应用命名系统的依据
     * @return 应用命名系统
     */
    @SystemMessage(fromResource = "prompt/apply-naming-system-prompt.txt")
    AppNameResult generateAppName(String userMessage);


    /**
     * 生成 Vue 项目代码（流式）
     *
     * @param userMessage 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);

}
