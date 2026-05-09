package com.godlei.godleiaicodemother.service;

import com.godlei.godleiaicodemother.model.entity.App;

/**
 * 删除应用后清理磁盘上的 AI 生成代码与部署产物，避免残留占用空间。
 */
public interface AppArtifactCleanupService {

    /**
     * 根据删除前的应用快照清理关联目录（应在应用逻辑删除成功后调用）。
     *
     * @param app 删除前查询到的应用实体（须含 id、codeGenType、deployKey）
     */
    void cleanupArtifacts(App app);
}
