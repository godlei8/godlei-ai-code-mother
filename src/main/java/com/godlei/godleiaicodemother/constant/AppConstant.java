package com.godlei.godleiaicodemother.constant;

/**
 * 应用常量
 */
public interface AppConstant {

    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 用户侧分页列表单页最大条数
     */
    int USER_APP_MAX_PAGE_SIZE = 20;

    /**
     * 对话历史「最新一页 / 向前加载」默认条数
     */
    int CHAT_HISTORY_PAGE_SIZE_DEFAULT = 10;

    /**
     * 对话历史单批最大条数（与前端约定最多一次加载 10 条）
     */
    int CHAT_HISTORY_PAGE_SIZE_MAX = 10;

    /**
     * 管理员查看全站对话历史时单页最大条数
     */
    int CHAT_HISTORY_ADMIN_PAGE_SIZE_MAX = 100;

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost";
}