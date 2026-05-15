package com.godlei.godleiaicodemother.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.godlei.godleiaicodemother.langgraph4j.model.ImageResource;
import com.godlei.godleiaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logo 图片生成工具
 */
@Slf4j
@Component
public class LogoGeneratorTool {

    private static final String DEFAULT_LOGO_MODEL = "qwen-image-plus";

    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${dashscope.logo-model:" + DEFAULT_LOGO_MODEL + "}")
    private String logoModel;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        if (StrUtil.isBlank(dashScopeApiKey)) {
            log.warn("DashScope API key is blank, skip logo generation");
            return logoList;
        }

        String finalModel = resolveLogoModel(logoModel);
        String finalDescription = StrUtil.blankToDefault(description, "科技网站品牌 Logo");
        String logoPrompt = "请生成一个简洁现代、适合网页使用的品牌 Logo，不要包含任何文字。设计需求：" + finalDescription;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("watermark", false);
        parameters.put("prompt_extend", true);
        parameters.put("negative_prompt", "text, words, letters, watermark");

        try {
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(finalModel)
                    .prompt(logoPrompt)
                    .size("1024*1024")
                    .n(1)
                    .parameters(parameters)
                    .build();
            ImageSynthesisResult result = new ImageSynthesis().call(param);
            if (result == null || result.getOutput() == null || result.getOutput().getResults() == null) {
                return logoList;
            }

            List<Map<String, String>> results = result.getOutput().getResults();
            for (Map<String, String> imageResult : results) {
                String imageUrl = imageResult.get("url");
                if (StrUtil.isNotBlank(imageUrl)) {
                    logoList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.LOGO)
                            .description(finalDescription)
                            .url(imageUrl)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败, model={}: {}", finalModel, e.getMessage(), e);
        }
        return logoList;
    }

    private String resolveLogoModel(String configuredModel) {
        String model = StrUtil.blankToDefault(configuredModel, DEFAULT_LOGO_MODEL);
        if (StrUtil.startWithIgnoreCase(model, "qwen-image-2")) {
            log.warn("Model {} is not compatible with ImageSynthesis endpoint, fallback to {}", model, DEFAULT_LOGO_MODEL);
            return DEFAULT_LOGO_MODEL;
        }
        return model;
    }
}
