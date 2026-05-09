package com.godlei.godleiaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成应用名称")
@Data
public class AppNameResult {

    /**
     * 应用名称
     */
    @Description("应用名称")
    private String appName;
}
