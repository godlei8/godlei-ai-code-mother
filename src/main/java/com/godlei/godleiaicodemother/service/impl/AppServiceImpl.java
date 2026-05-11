package com.godlei.godleiaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.godlei.godleiaicodemother.core.AiCodeGeneratorFacade;
import com.godlei.godleiaicodemother.core.handler.StreamHandlerExecutor;
import com.godlei.godleiaicodemother.model.enums.CodeGenTypeEnum;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.godlei.godleiaicodemother.constant.AppConstant;
import com.godlei.godleiaicodemother.constant.UserConstant;
import com.godlei.godleiaicodemother.exception.BusinessException;
import com.godlei.godleiaicodemother.exception.ErrorCode;
import com.godlei.godleiaicodemother.exception.ThrowUtils;
import com.godlei.godleiaicodemother.mapper.AppMapper;
import com.godlei.godleiaicodemother.model.dto.app.*;
import com.godlei.godleiaicodemother.model.entity.App;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.AppVO;
import com.godlei.godleiaicodemother.service.AppArtifactCleanupService;
import com.godlei.godleiaicodemother.service.AppService;
import com.godlei.godleiaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/godlei8">Godlei</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private AppArtifactCleanupService appArtifactCleanupService;

    private static final String DEFAULT_APP_NAME = "未命名应用";

    private static final String COL_CREATE_TIME = "createTime";

    @Value("${code.deploy-host:http://localhost}")
    private String deployHost;

    /**
     * 管理员列表排序字段白名单（与实体列名一致，避免 sortField 注入）
     */
    private static final Set<String> ADMIN_SORT_COLUMNS = Set.of(
            "id",
            "appName",
            "cover",
            "initPrompt",
            "codeGenType",
            "deployKey",
            "deployedTime",
            "priority",
            "userId",
            "editTime",
            "createTime",
            "updateTime"
    );

    @Override
    public long addApp(AppAddRequest appAddRequest, User loginUser) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        String initPrompt = appAddRequest.getInitPrompt();
        if (StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        // 根据 用户描述 生成 应用name
        String appName = StrUtil.isBlank(appAddRequest.getAppName()) ? DEFAULT_APP_NAME : aiCodeGeneratorFacade.generateAppName(appAddRequest.getInitPrompt());
        App app = App.builder()
                .appName(appName)
                .initPrompt(initPrompt)
                .priority(AppConstant.DEFAULT_APP_PRIORITY)
                .codeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue())
                .userId(loginUser.getId())
                .build();
        boolean ok = this.save(app);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，仅本人可以部署自己的应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 如果没有，则生成 6 位 deployKey（字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，获取原始代码生成路径（应用访问目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码路径不存在，请先生成应用");
        }
        // 7. Vue 项目特殊处理：执行构建
//        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
//        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
//            // Vue 项目需要构建
//            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
//            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请重试");
//            // 检查 dist 目录是否存在
//            File distDir = new File(sourceDirPath, "dist");
//            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
//            // 构建完成后，需要将构建后的文件复制到部署目录
//            sourceDir = distDir;
//        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
        }
        // 9. 更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 构建应用访问 URL
        return String.format("%s/%s/", deployHost, deployKey);

        // 11. 异步生成截图并且更新应用封面
        // generateAppScreenshotAsync(appId, appDeployUrl);
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
        App app = requireOwnedApp(id, loginUser);
        app.setAppName(appName);
        return this.updateById(app);
    }

    @Override
    public boolean deleteMyApp(long id, User loginUser) {
        App app = requireOwnedApp(id, loginUser);
        boolean removed = this.removeById(id);
        if (removed) {
            chatHistoryService.removeAllByAppId(id);
            appArtifactCleanupService.cleanupArtifacts(app);
        }
        return removed;
    }

    @Override
    public AppVO getAppVOByUser(long id, User loginUser) {
        if (isAdminUser(loginUser)) {
            return getAppVO(getAppByIdAdmin(id));
        }
        return getAppVO(requireOwnedApp(id, loginUser));
    }

    @Override
    public Page<AppVO> listMyAppVOByPage(AppListPageRequest appListPageRequest, User loginUser) {
        ThrowUtils.throwIf(appListPageRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = Math.max(1, appListPageRequest.getPageNum());
        long pageSize = clampUserPageSize(appListPageRequest.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create().eq("userId", loginUser.getId());
        applyOptionalAppNameLike(queryWrapper, appListPageRequest.getAppName());
        queryWrapper.orderBy(COL_CREATE_TIME, false);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        return toVoPage(appPage, pageNum, pageSize);
    }

    @Override
    public Page<AppVO> listFeaturedAppVOByPage(AppListPageRequest appListPageRequest) {
        ThrowUtils.throwIf(appListPageRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = Math.max(1, appListPageRequest.getPageNum());
        long pageSize = clampUserPageSize(appListPageRequest.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create().eq("priority", AppConstant.GOOD_APP_PRIORITY);
        applyOptionalAppNameLike(queryWrapper, appListPageRequest.getAppName());
        queryWrapper.orderBy("priority", false).orderBy(COL_CREATE_TIME, false);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        return toVoPage(appPage, pageNum, pageSize);
    }

    @Override
    public boolean deleteAppAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        boolean removed = this.removeById(id);
        ThrowUtils.throwIf(!removed, ErrorCode.NOT_FOUND_ERROR);
        chatHistoryService.removeAllByAppId(id);
        appArtifactCleanupService.cleanupArtifacts(app);
        return true;
    }

    @Override
    public boolean updateAppAdmin(AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = appAdminUpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        applyAdminUpdate(app, appAdminUpdateRequest);
        boolean ok = this.updateById(app);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public Page<AppVO> listAppByPageAdmin(AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = Math.max(1, appAdminQueryRequest.getPageNum());
        long pageSize = Math.max(1, appAdminQueryRequest.getPageSize());
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), buildAdminQueryWrapper(appAdminQueryRequest));
        return toVoPage(appPage, pageNum, pageSize);
    }

    @Override
    public App getAppByIdAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return app;
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

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，仅本人可以和自己的应用对话
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        }
        // 获取应用名称
        String appName = app.getAppName();
        // 5. 调用 AI（持久化用户消息、成功后的 AI 全文、或失败时的错误信息）
        chatHistoryService.saveUserMessage(appId, app.getUserId(), message);
        // 6. 调用 AI（持久化成功后的 AI 全文、或失败时的错误信息）
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message + "，应用名称就叫" + appName, codeGenTypeEnum, appId);
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }

    /**
     * 校验当前用户是否拥有该应用，并返回实体。
     */
    private App requireOwnedApp(long id, User loginUser) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return app;
    }

    private static void applyOptionalAppNameLike(QueryWrapper queryWrapper, String appName) {
        if (StrUtil.isNotBlank(appName)) {
            queryWrapper.like("appName", appName);
        }
    }

    /**
     * 用户侧列表分页大小限制在 [1, USER_APP_MAX_PAGE_SIZE]。
     */
    private static int clampUserPageSize(int pageSize) {
        int size = pageSize > 0 ? pageSize : 10;
        return Math.min(size, AppConstant.USER_APP_MAX_PAGE_SIZE);
    }

    private Page<AppVO> toVoPage(Page<App> appPage, long pageNum, long pageSize) {
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        voPage.setRecords(getAppVOList(appPage.getRecords()));
        return voPage;
    }

    private static void applyAdminUpdate(App entity, AppAdminUpdateRequest request) {
        if (StrUtil.isNotBlank(request.getAppName())) {
            entity.setAppName(request.getAppName());
        }
        if (request.getCover() != null) {
            entity.setCover(request.getCover());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
    }

    private QueryWrapper buildAdminQueryWrapper(AppAdminQueryRequest request) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (request.getId() != null) {
            queryWrapper.eq("id", request.getId());
        }
        if (StrUtil.isNotBlank(request.getAppName())) {
            queryWrapper.like("appName", request.getAppName());
        }
        if (StrUtil.isNotBlank(request.getCover())) {
            queryWrapper.like("cover", request.getCover());
        }
        if (StrUtil.isNotBlank(request.getInitPrompt())) {
            queryWrapper.like("initPrompt", request.getInitPrompt());
        }
        if (StrUtil.isNotBlank(request.getCodeGenType())) {
            queryWrapper.eq("codeGenType", request.getCodeGenType());
        }
        if (StrUtil.isNotBlank(request.getDeployKey())) {
            queryWrapper.like("deployKey", request.getDeployKey());
        }
        if (request.getPriority() != null) {
            queryWrapper.eq("priority", request.getPriority());
        }
        if (request.getUserId() != null) {
            queryWrapper.eq("userId", request.getUserId());
        }
        applyAdminOrder(queryWrapper, request.getSortField(), request.getSortOrder());
        return queryWrapper;
    }

    private static void applyAdminOrder(QueryWrapper queryWrapper, String sortField, String sortOrder) {
        if (StrUtil.isNotBlank(sortField) && ADMIN_SORT_COLUMNS.contains(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy(COL_CREATE_TIME, false);
        }
    }

    private static boolean isAdminUser(User loginUser) {
        return loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
    }
}
