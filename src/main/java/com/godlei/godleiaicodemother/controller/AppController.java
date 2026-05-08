package com.godlei.godleiaicodemother.controller;

import com.godlei.godleiaicodemother.annotation.AuthCheck;
import com.godlei.godleiaicodemother.common.BaseResponse;
import com.godlei.godleiaicodemother.common.DeleteRequest;
import com.godlei.godleiaicodemother.common.ResultUtils;
import com.godlei.godleiaicodemother.constant.UserConstant;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.exception.ThrowUtils;
import com.godlei.godleiaicodemother.model.dto.app.*;
import com.godlei.godleiaicodemother.model.entity.App;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.AppVO;
import com.godlei.godleiaicodemother.service.AppService;
import com.godlei.godleiaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/godlei8">Godlei</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 【用户】创建应用（须填写 initPrompt）
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long id = appService.addApp(appAddRequest, loginUser);
        return ResultUtils.success(id);
    }

    /**
     * 【用户】根据 id 修改自己的应用（仅应用名称）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUserUpdateRequest appUserUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUserUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean ok = appService.updateMyApp(appUserUpdateRequest, loginUser);
        return ResultUtils.success(ok);
    }

    /**
     * 【用户】根据 id 删除自己的应用
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean ok = appService.deleteMyApp(deleteRequest.getId(), loginUser);
        return ResultUtils.success(ok);
    }

    /**
     * 【用户】根据 id 查看应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVO(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        AppVO appVO = appService.getAppVOByUser(id, loginUser);
        return ResultUtils.success(appVO);
    }

    /**
     * 【用户】分页查询自己的应用列表（支持应用名称查询，每页最多 20 条）
     */
    @PostMapping("/list/page/vo/mine")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppListPageRequest appListPageRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appListPageRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<AppVO> page = appService.listMyAppVOByPage(appListPageRequest, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 【用户】分页查询精选的应用列表（支持应用名称查询，每页最多 20 条）
     */
    @PostMapping("/list/page/vo/featured")
    public BaseResponse<Page<AppVO>> listFeaturedAppVOByPage(@RequestBody AppListPageRequest appListPageRequest) {
        ThrowUtils.throwIf(appListPageRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> page = appService.listFeaturedAppVOByPage(appListPageRequest);
        return ResultUtils.success(page);
    }

    /**
     * 【管理员】根据 id 删除任意应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean ok = appService.deleteAppAdmin(deleteRequest.getId());
        return ResultUtils.success(ok);
    }

    /**
     * 【管理员】根据 id 更新任意应用（应用名称、应用封面、优先级）
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean ok = appService.updateAppAdmin(appAdminUpdateRequest);
        return ResultUtils.success(ok);
    }

    /**
     * 【管理员】分页查询应用列表（支持按除时间字段外的条件筛选，每页数量不限）
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppByPageAdmin(@RequestBody AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> page = appService.listAppByPageAdmin(appAdminQueryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 【管理员】根据 id 查看应用详情
     */
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppByIdAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getAppByIdAdmin(id);
        return ResultUtils.success(app);
    }
}
