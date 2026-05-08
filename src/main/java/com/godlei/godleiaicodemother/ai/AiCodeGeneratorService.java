package com.godlei.godleiaicodemother.ai;

import com.godlei.godleiaicodemother.ai.model.HtmlCodeResult;
import com.godlei.godleiaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
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


    /**
     * 根据系统消息和用户消息生成多文件代码流
     * 该方法使用系统消息作为提示，根据用户输入生成多文件代码
     *
     * @param userMessage 用户输入的消息，用于生成代码
     * @return 返回一个Flux<String>类型的代码流，可以逐步生成和输出代码内容
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);


}
