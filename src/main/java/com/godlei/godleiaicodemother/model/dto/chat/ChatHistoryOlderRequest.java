package com.godlei.godleiaicodemother.model.dto.chat;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 向前（更早）加载一页对话历史
 */
@Data
public class ChatHistoryOlderRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 每批条数（默认 10，最大 10）
     */
    private Integer pageSize;

    /**
     * 游标：更早于该时间的记录（与 {@link #beforeId} 配对）
     */
    private LocalDateTime beforeCreateTime;

    /**
     * 游标：更早于该 id 的记录（与 createTime 同时相等时用于区分）
     */
    private Long beforeId;

    private static final long serialVersionUID = 1L;
}
