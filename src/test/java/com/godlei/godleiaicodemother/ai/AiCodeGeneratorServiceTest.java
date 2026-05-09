package com.godlei.godleiaicodemother.ai;

import com.godlei.godleiaicodemother.ai.model.HtmlCodeResult;
import com.godlei.godleiaicodemother.ai.model.MultiFileCodeResult;
import com.godlei.godleiaicodemother.core.AiCodeGeneratorFacade;
import com.godlei.godleiaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做一个简单介绍页，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做一个简单介绍页，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode(
                "做一个简单介绍页，不超过20行",
                CodeGenTypeEnum.MULTI_FILE,
                1L
        );
        Assertions.assertNotNull(file);
    }
}
