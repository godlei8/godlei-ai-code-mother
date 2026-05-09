package com.godlei.godleiaicodemother.controller;

import com.godlei.godleiaicodemother.annotation.AuthCheck;
import com.godlei.godleiaicodemother.common.BaseResponse;
import com.godlei.godleiaicodemother.common.ResultUtils;
import com.godlei.godleiaicodemother.constant.UserConstant;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.exception.ThrowUtils;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryAdminQueryRequest;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryLatestRequest;
import com.godlei.godleiaicodemother.model.dto.chat.ChatHistoryOlderRequest;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.ChatHistoryAdminVO;
import com.godlei.godleiaicodemother.model.vo.ChatHistoryCursorVO;
import com.godlei.godleiaicodemother.service.ChatHistoryService;
import com.godlei.godleiaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/godlei8">Godlei</a>
 */
@RestController
@RequestMapping("/chat/history")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 【应用创建者 / 管理员】加载某应用最近一页对话（默认最新 10 条，时间升序）
     */
    @PostMapping("/list/latest")
    public BaseResponse<ChatHistoryCursorVO> listLatest(@RequestBody ChatHistoryLatestRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        ChatHistoryCursorVO vo = chatHistoryService.listLatest(request, loginUser);
        return ResultUtils.success(vo);
    }

    /**
     * 【应用创建者 / 管理员】向前加载更早的对话
     */
    @PostMapping("/list/older")
    public BaseResponse<ChatHistoryCursorVO> listOlder(@RequestBody ChatHistoryOlderRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        ChatHistoryCursorVO vo = chatHistoryService.listOlder(request, loginUser);
        return ResultUtils.success(vo);
    }

    /**
     * 【管理员】分页查看全站对话历史（按消息时间降序）
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryAdminVO>> listByPageAdmin(@RequestBody ChatHistoryAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Page<ChatHistoryAdminVO> page = chatHistoryService.listByPageAdmin(request);
        return ResultUtils.success(page);
    }
}
