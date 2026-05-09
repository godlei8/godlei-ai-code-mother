package com.godlei.godleiaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 对话消息类型枚举
 */
@Getter
public enum MessageTypeEnum {

    /**
     * 用户消息
     */
    USER("用户", "user"),

    /**
     * AI 正常回复（流式拼接后的完整内容）
     */
    AI("AI", "ai"),

    /**
     * AI 调用或生成失败时的错误记录
     */
    AI_ERROR("AI 错误", "ai_error");

    private final String text;
    private final String value;

    MessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 存储值
     * @return 枚举或 null
     */
    public static MessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (MessageTypeEnum anEnum : MessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
