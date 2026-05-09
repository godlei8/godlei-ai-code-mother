package com.godlei.godleiaicodemother.service;

import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryAdminQueryRequest;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryLatestRequest;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryOlderRequest;
import com.godlei.godleiaicodemother.model.entity.ChatHistory;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.ChatHistoryAdminVO;
import com.godlei.godleiaicodemother.model.vo.ChatHistoryCursorVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/godlei8">Godlei</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 记录用户发送的消息
     */
    void saveUserMessage(Long appId, Long ownerUserId, String message);

    /**
     * 记录 AI 完整回复（流式拼接后）
     */
    void saveAiMessage(Long appId, Long ownerUserId, String fullMessage);

    /**
     * 记录 AI 失败信息
     */
    void saveAiError(Long appId, Long ownerUserId, Throwable error);

    /**
     * 删除某应用下的全部对话历史（应用删除时调用）
     */
    void removeAllByAppId(long appId);

    /**
     * 查询最近一页对话（仅应用创建者或管理员）
     */
    ChatHistoryCursorVO listLatest(ChatHistoryLatestRequest request, User loginUser);

    /**
     * 向前加载更早的对话（仅应用创建者或管理员）
     */
    ChatHistoryCursorVO listOlder(ChatHistoryOlderRequest request, User loginUser);

    /**
     * 管理员分页查询全站对话历史（按消息时间降序）
     */
    Page<ChatHistoryAdminVO> listByPageAdmin(ChatHistoryAdminQueryRequest request);
}
