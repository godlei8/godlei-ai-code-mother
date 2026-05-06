package com.godlei.godleiaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.godlei.godleiaicodemother.mapper")
public class GodleiAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(GodleiAiCodeMotherApplication.class, args);
    }

}
