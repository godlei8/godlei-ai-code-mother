package com.godlei.godleiaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.godlei.godleiaicodemother.constant.AppConstant;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.exception.ThrowUtils;
import com.godlei.godleiaicodemother.mapper.AppMapper;
import com.godlei.godleiaicodemother.model.dto.app.*;
import com.godlei.godleiaicodemother.model.entity.App;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.AppVO;
import com.godlei.godleiaicodemother.service.AppService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/godlei8">Godlei</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private static final String DEFAULT_APP_NAME = "未命名应用";

    @Override
    public long addApp(AppAddRequest appAddRequest, User loginUser) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        String initPrompt = appAddRequest.getInitPrompt();
        if (StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        App app = App.builder()
                .appName(StrUtil.isBlank(appAddRequest.getAppName()) ? DEFAULT_APP_NAME : appAddRequest.getAppName())
                .initPrompt(initPrompt)
                .priority(AppConstant.DEFAULT_APP_PRIORITY)
                .userId(loginUser.getId())
                .build();
        boolean ok = this.save(app);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public boolean updateMyApp(AppUserUpdateRequest appUserUpdateRequest, User loginUser) {
        ThrowUtils.throwIf(appUserUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = appUserUpdateRequest.getId();
        String appName = appUserUpdateRequest.getAppName();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        if (StrUtil.isBlank(appName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        }
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR);
        app.setAppName(appName);
        return this.updateById(app);
    }

    @Override
    public boolean deleteMyApp(long id, User loginUser) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return this.removeById(id);
    }

    @Override
    public AppVO getAppVOByUser(long id, User loginUser) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return getAppVO(app);
    }

    @Override
    public Page<AppVO> listMyAppVOByPage(AppListPageRequest appListPageRequest, User loginUser) {
        ThrowUtils.throwIf(appListPageRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appListPageRequest.getPageNum();
        long pageSize = Math.min(appListPageRequest.getPageSize(), AppConstant.USER_APP_MAX_PAGE_SIZE);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", loginUser.getId());
        if (StrUtil.isNotBlank(appListPageRequest.getAppName())) {
            queryWrapper.like("appName", appListPageRequest.getAppName());
        }
        queryWrapper.orderBy("createTime", false);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        voPage.setRecords(getAppVOList(appPage.getRecords()));
        return voPage;
    }

    @Override
    public Page<AppVO> listFeaturedAppVOByPage(AppListPageRequest appListPageRequest) {
        ThrowUtils.throwIf(appListPageRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appListPageRequest.getPageNum();
        long pageSize = Math.min(appListPageRequest.getPageSize(), AppConstant.USER_APP_MAX_PAGE_SIZE);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("priority", AppConstant.GOOD_APP_PRIORITY);
        if (StrUtil.isNotBlank(appListPageRequest.getAppName())) {
            queryWrapper.like("appName", appListPageRequest.getAppName());
        }
        queryWrapper.orderBy("priority", false).orderBy("createTime", false);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        voPage.setRecords(getAppVOList(appPage.getRecords()));
        return voPage;
    }

    @Override
    public boolean deleteAppAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        return this.removeById(id);
    }

    @Override
    public boolean updateAppAdmin(AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = appAdminUpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        if (StrUtil.isNotBlank(appAdminUpdateRequest.getAppName())) {
            app.setAppName(appAdminUpdateRequest.getAppName());
        }
        if (appAdminUpdateRequest.getCover() != null) {
            app.setCover(appAdminUpdateRequest.getCover());
        }
        if (appAdminUpdateRequest.getPriority() != null) {
            app.setPriority(appAdminUpdateRequest.getPriority());
        }
        return this.updateById(app);
    }

    @Override
    public Page<AppVO> listAppByPageAdmin(AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appAdminQueryRequest.getPageNum();
        long pageSize = appAdminQueryRequest.getPageSize();
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), getAdminQueryWrapper(appAdminQueryRequest));
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        voPage.setRecords(getAppVOList(appPage.getRecords()));
        return voPage;
    }

    @Override
    public App getAppByIdAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return app;
    }

    @Override
    public QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest appAdminQueryRequest) {
        if (appAdminQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        Long id = appAdminQueryRequest.getId();
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        if (StrUtil.isNotBlank(appAdminQueryRequest.getAppName())) {
            queryWrapper.like("appName", appAdminQueryRequest.getAppName());
        }
        if (StrUtil.isNotBlank(appAdminQueryRequest.getCover())) {
            queryWrapper.like("cover", appAdminQueryRequest.getCover());
        }
        if (StrUtil.isNotBlank(appAdminQueryRequest.getInitPrompt())) {
            queryWrapper.like("initPrompt", appAdminQueryRequest.getInitPrompt());
        }
        if (StrUtil.isNotBlank(appAdminQueryRequest.getCodeGenType())) {
            queryWrapper.eq("codeGenType", appAdminQueryRequest.getCodeGenType());
        }
        if (StrUtil.isNotBlank(appAdminQueryRequest.getDeployKey())) {
            queryWrapper.like("deployKey", appAdminQueryRequest.getDeployKey());
        }
        if (appAdminQueryRequest.getPriority() != null) {
            queryWrapper.eq("priority", appAdminQueryRequest.getPriority());
        }
        if (appAdminQueryRequest.getUserId() != null) {
            queryWrapper.eq("userId", appAdminQueryRequest.getUserId());
        }
        String sortField = appAdminQueryRequest.getSortField();
        String sortOrder = appAdminQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream().map(this::getAppVO).collect(Collectors.toList());
    }
}
