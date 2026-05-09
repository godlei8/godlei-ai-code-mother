package com.godlei.godleiaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.godlei.godleiaicodemother.constant.AppConstant;
import com.godlei.godleiaicodemother.constant.UserConstant;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.exception.ThrowUtils;
import com.godlei.godleiaicodemother.mapper.AppMapper;
import com.godlei.godleiaicodemother.mapper.ChatHistoryMapper;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryAdminQueryRequest;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryLatestRequest;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryOlderRequest;
import com.godlei.godleiaicodemother.model.entity.App;
import com.godlei.godleiaicodemother.model.entity.ChatHistory;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.enums.MessageTypeEnum;
import com.godlei.godleiaicodemother.model.vo.ChatHistoryAdminVO;
import com.godlei.godleiaicodemother.model.vo.ChatHistoryCursorVO;
import com.godlei.godleiaicodemother.model.vo.ChatHistoryVO;
import com.godlei.godleiaicodemother.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/godlei8">Godlei</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    private static final int MESSAGE_MAX_LEN = 60000;

    @Resource
    private AppMapper appMapper;

    @Override
    public void saveUserMessage(Long appId, Long ownerUserId, String message) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ownerUserId == null || ownerUserId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR);
        LocalDateTime now = LocalDateTime.now();
        ChatHistory row = ChatHistory.builder()
                .appId(appId)
                .userId(ownerUserId)
                .messageType(MessageTypeEnum.USER.getValue())
                .message(truncateForDb(message))
                .createTime(now)
                .updateTime(now)
                .build();
        boolean ok = this.save(row);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR, "保存用户消息失败");
    }

    @Override
    public void saveAiMessage(Long appId, Long ownerUserId, String fullMessage) {
        if (StrUtil.isBlank(fullMessage)) {
            return;
        }
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ownerUserId == null || ownerUserId <= 0, ErrorCode.PARAMS_ERROR);
        LocalDateTime now = LocalDateTime.now();
        ChatHistory row = ChatHistory.builder()
                .appId(appId)
                .userId(ownerUserId)
                .messageType(MessageTypeEnum.AI.getValue())
                .message(truncateForDb(fullMessage))
                .createTime(now)
                .updateTime(now)
                .build();
        boolean ok = this.save(row);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR, "保存 AI 回复失败");
    }

    @Override
    public void saveAiError(Long appId, Long ownerUserId, Throwable error) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ownerUserId == null || ownerUserId <= 0, ErrorCode.PARAMS_ERROR);
        String text = error == null ? "未知错误" : StrUtil.blankToDefault(error.getMessage(), error.getClass().getSimpleName());
        LocalDateTime now = LocalDateTime.now();
        ChatHistory row = ChatHistory.builder()
                .appId(appId)
                .userId(ownerUserId)
                .messageType(MessageTypeEnum.AI_ERROR.getValue())
                .message(truncateForDb(text))
                .createTime(now)
                .updateTime(now)
                .build();
        boolean ok = this.save(row);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR, "保存 AI 错误信息失败");
    }

    @Override
    public void removeAllByAppId(long appId) {
        this.remove(QueryWrapper.create().eq("appId", appId));
    }

    @Override
    public ChatHistoryCursorVO listLatest(ChatHistoryLatestRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long appId = request.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        App app = requireApp(appId);
        checkCanViewAppChat(app, loginUser);

        int batchSize = normalizeChatPageSize(request.getPageSize());
        List<ChatHistory> rows = fetchBatchDesc(appId, null, null, batchSize + 1);
        return toCursorVo(rows, batchSize);
    }

    @Override
    public ChatHistoryCursorVO listOlder(ChatHistoryOlderRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long appId = request.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        ThrowUtils.throwIf(request.getBeforeCreateTime() == null || request.getBeforeId() == null,
                ErrorCode.PARAMS_ERROR, "加载更早记录时必须提供 beforeCreateTime 与 beforeId");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        App app = requireApp(appId);
        checkCanViewAppChat(app, loginUser);

        int batchSize = normalizeChatPageSize(request.getPageSize());
        List<ChatHistory> rows = fetchBatchDesc(appId, request.getBeforeCreateTime(), request.getBeforeId(), batchSize + 1);
        return toCursorVo(rows, batchSize);
    }

    @Override
    public Page<ChatHistoryAdminVO> listByPageAdmin(ChatHistoryAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = Math.max(1, request.getPageNum());
        long pageSize = Math.min(Math.max(1, request.getPageSize()), AppConstant.CHAT_HISTORY_ADMIN_PAGE_SIZE_MAX);
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (request.getAppId() != null) {
            queryWrapper.eq("appId", request.getAppId());
        }
        if (request.getUserId() != null) {
            queryWrapper.eq("userId", request.getUserId());
        }
        if (StrUtil.isNotBlank(request.getMessageType())) {
            queryWrapper.eq("messageType", request.getMessageType());
        }
        queryWrapper.orderBy("createTime", false).orderBy("id", false);
        Page<ChatHistory> entityPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<ChatHistoryAdminVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotalRow());
        List<ChatHistory> records = entityPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            voPage.setRecords(List.of());
            return voPage;
        }
        List<Long> appIds = records.stream().map(ChatHistory::getAppId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<App> apps = appMapper.selectListByIds(appIds);
        Map<Long, App> appMap = apps.stream().collect(Collectors.toMap(App::getId, a -> a, (a, b) -> a));
        List<ChatHistoryAdminVO> voList = new ArrayList<>(records.size());
        for (ChatHistory ch : records) {
            ChatHistoryAdminVO vo = new ChatHistoryAdminVO();
            BeanUtil.copyProperties(ch, vo);
            App ref = appMap.get(ch.getAppId());
            if (ref != null) {
                vo.setAppName(ref.getAppName());
            }
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    private App requireApp(Long appId) {
        App app = appMapper.selectOneById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return app;
    }

    private static void checkCanViewAppChat(App app, User loginUser) {
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return;
        }
        if (app.getUserId().equals(loginUser.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用对话历史");
    }

    private List<ChatHistory> fetchBatchDesc(Long appId, LocalDateTime beforeTime, Long beforeId, int limitPlusOne) {
        QueryWrapper qw = QueryWrapper.create().eq("appId", appId);
        if (beforeTime != null && beforeId != null) {
            qw.and("(createTime < ? OR (createTime = ? AND id < ?))", beforeTime, beforeTime, beforeId);
        }
        qw.orderBy("createTime", false).orderBy("id", false);
        Page<ChatHistory> page = this.page(Page.of(1, limitPlusOne), qw);
        return page.getRecords() == null ? List.of() : page.getRecords();
    }

    private ChatHistoryCursorVO toCursorVo(List<ChatHistory> rowsDesc, int batchSize) {
        boolean hasMore = rowsDesc.size() > batchSize;
        List<ChatHistory> trimmed = hasMore ? rowsDesc.subList(0, batchSize) : rowsDesc;
        List<ChatHistory> chronological = new ArrayList<>(trimmed);
        Collections.reverse(chronological);
        List<ChatHistoryVO> vos = chronological.stream().map(this::toVo).collect(Collectors.toList());
        ChatHistoryCursorVO.ChatHistoryCursorVOBuilder b = ChatHistoryCursorVO.builder()
                .records(vos)
                .hasMore(hasMore);
        if (!chronological.isEmpty()) {
            ChatHistory oldest = chronological.get(0);
            b.nextBeforeCreateTime(oldest.getCreateTime()).nextBeforeId(oldest.getId());
        }
        return b.build();
    }

    private ChatHistoryVO toVo(ChatHistory entity) {
        ChatHistoryVO vo = new ChatHistoryVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    private static int normalizeChatPageSize(Integer pageSize) {
        int size = pageSize != null && pageSize > 0 ? pageSize : AppConstant.CHAT_HISTORY_PAGE_SIZE_DEFAULT;
        return Math.min(size, AppConstant.CHAT_HISTORY_PAGE_SIZE_MAX);
    }

    private static String truncateForDb(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= MESSAGE_MAX_LEN) {
            return text;
        }
        return text.substring(0, MESSAGE_MAX_LEN);
    }
}
