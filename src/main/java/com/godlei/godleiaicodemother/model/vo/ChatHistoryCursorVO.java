package com.godlei.godleiaicodemother.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 按游标分页的对话历史返回（时间升序，便于前端气泡展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryCursorVO implements Serializable {

    /**
     * 本批次消息（从早到晚）
     */
    private List<ChatHistoryVO> records;

    /**
     * 是否仍存在更早的消息可加载
     */
    private Boolean hasMore;

    /**
     * 加载「更早一批」时传入：当前批次中最早一条的创建时间
     */
    private LocalDateTime nextBeforeCreateTime;

    /**
     * 加载「更早一批」时传入：当前批次中最早一条的 id（与创建时间共同组成稳定游标）
     */
    private Long nextBeforeId;

    private static final long serialVersionUID = 1L;
}
