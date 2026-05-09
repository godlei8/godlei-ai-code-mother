package com.godlei.godleiaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史视图对象
 */
@Data
public class ChatHistoryVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 消息正文
     */
    private String message;

    /**
     * 消息类型（user / ai / ai_error）
     */
    private String messageType;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 应用创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}
