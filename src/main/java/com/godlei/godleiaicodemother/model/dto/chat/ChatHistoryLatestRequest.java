package com.godlei.godleiaicodemother.model.dto.chat;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询应用最近一页对话历史（默认最新 10 条）
 */
@Data
public class ChatHistoryLatestRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 每批条数（默认 10，最大 10）
     */
    private Integer pageSize;

    private static final long serialVersionUID = 1L;
}
