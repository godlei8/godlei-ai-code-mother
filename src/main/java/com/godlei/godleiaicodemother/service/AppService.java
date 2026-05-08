package com.godlei.godleiaicodemother.service;

import com.godlei.godleiaicodemother.model.dto.app.*;
import com.godlei.godleiaicodemother.model.entity.App;
import com.godlei.godleiaicodemother.model.entity.User;
import com.godlei.godleiaicodemother.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/godlei8">Godlei</a>
 */
public interface AppService extends IService<App> {

    /**
     * 用户创建应用（须填写 initPrompt）
     */
    long addApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 应用部署
     *
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User loginUser);
    /**
     * 用户更新自己的应用（仅名称）
     */
    boolean updateMyApp(AppUserUpdateRequest appUserUpdateRequest, User loginUser);

    /**
     * 用户删除自己的应用
     */
    boolean deleteMyApp(long id, User loginUser);

    /**
     * 用户查看自己的应用详情
     */
    AppVO getAppVOByUser(long id, User loginUser);

    /**
     * 用户分页查询自己的应用列表
     */
    Page<AppVO> listMyAppVOByPage(AppListPageRequest appListPageRequest, User loginUser);

    /**
     * 分页查询精选应用列表
     */
    Page<AppVO> listFeaturedAppVOByPage(AppListPageRequest appListPageRequest);

    /**
     * 管理员删除应用
     */
    boolean deleteAppAdmin(long id);

    /**
     * 管理员更新应用（名称、封面、优先级）
     */
    boolean updateAppAdmin(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页查询应用列表
     */
    Page<AppVO> listAppByPageAdmin(AppAdminQueryRequest appAdminQueryRequest);

    /**
     * 管理员查看应用详情
     */
    App getAppByIdAdmin(long id);

    /**
     * 实体转 VO
     */
    AppVO getAppVO(App app);

    /**
     * 列表实体转 VO
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据应用ID、用户消息和登录用户信息生成代码的异步方法
     *
     * @param appId     应用ID，用于标识特定的应用
     * @param message   用户输入的消息内容，将用于生成代码
     * @param loginUser 当前登录用户的信息，可能用于权限验证或个性化处理
     * @return 返回一个Flux<String>类型的响应流，包含生成的代码内容
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);
}
