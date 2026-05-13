package com.godlei.godleiaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.godlei.godleiaicodemother.constant.AppConstant;
import com.godlei.godleiaicodemother.model.entity.App;
import com.godlei.godleiaicodemother.model.enums.CodeGenTypeEnum;
import com.godlei.godleiaicodemother.service.AppArtifactCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

/**
 * 应用 AI 产物与部署目录清理实现。
 */
@Service
@Slf4j
public class AppArtifactCleanupServiceImpl implements AppArtifactCleanupService {

    /**
     * deployKey 仅允许字母数字，防止路径穿越（用户侧随机生成，仍做校验）。
     */
    private static final java.util.regex.Pattern DEPLOY_KEY_SAFE = java.util.regex.Pattern.compile("^[a-zA-Z0-9]+$");
    private static final int DELETE_RETRY_TIMES = 5;
    private static final long DELETE_RETRY_DELAY_MILLIS = 300L;

    @Override
    public void cleanupArtifacts(App app) {
        if (app == null || app.getId() == null || app.getId() <= 0) {
            return;
        }
        Long appId = app.getId();
        // 1. 生成目录：各类型的 {value}_{appId}（覆盖历史上曾切换过的 codeGenType）
        for (CodeGenTypeEnum type : CodeGenTypeEnum.values()) {
            String dirName = type.getValue() + "_" + appId;
            deleteUnderRoot(AppConstant.CODE_OUTPUT_ROOT_DIR, dirName);
        }
        // 2. 部署目录与静态预览可能使用的 code_output/{deployKey}
        String deployKey = app.getDeployKey();
        if (isSafeDeployKeySegment(deployKey)) {
            deleteUnderRoot(AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey);
            deleteUnderRoot(AppConstant.CODE_OUTPUT_ROOT_DIR, deployKey);
        }
    }

    private static boolean isSafeDeployKeySegment(String deployKey) {
        return StrUtil.isNotBlank(deployKey) && DEPLOY_KEY_SAFE.matcher(deployKey).matches();
    }

    /**
     * 仅删除 root 下的直接子路径 root/childName，并校验解析后的路径仍在 root 内。
     */
    private void deleteUnderRoot(String rootPath, String childName) {
        if (StrUtil.isBlank(rootPath) || StrUtil.isBlank(childName)) {
            return;
        }
        if (childName.contains("..") || childName.indexOf('/') >= 0 || childName.indexOf('\\') >= 0) {
            log.warn("跳过非法子路径名: {}", childName);
            return;
        }
        File root = new File(rootPath);
        File target = new File(root, childName);
        try {
            Path rootCanon = root.getCanonicalFile().toPath();
            Path targetCanon = target.getCanonicalFile().toPath();
            if (!targetCanon.startsWith(rootCanon)) {
                log.warn("路径不在允许根目录内，跳过删除: {}", targetCanon);
                return;
            }
        } catch (IOException e) {
            log.warn("解析路径失败，跳过删除: {}", target.getAbsolutePath(), e);
            return;
        }
        try {
            if (target.exists()) {
                deleteWithRetry(target.toPath());
                log.info("已删除应用产物路径: {}", target.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("删除应用产物路径失败: {}", target.getAbsolutePath(), e);
        }
    }

    private void deleteWithRetry(Path targetPath) throws IOException, InterruptedException {
        IOException lastException = null;
        for (int attempt = 1; attempt <= DELETE_RETRY_TIMES; attempt++) {
            try {
                deleteRecursively(targetPath);
                return;
            } catch (NoSuchFileException ignored) {
                return;
            } catch (IOException e) {
                lastException = e;
                if (attempt == DELETE_RETRY_TIMES) {
                    break;
                }
                log.warn("删除路径第 {} 次失败，准备重试: {}", attempt, targetPath, e);
                TimeUnit.MILLISECONDS.sleep(DELETE_RETRY_DELAY_MILLIS);
            }
        }
        throw lastException == null ? new IOException("删除路径失败: " + targetPath) : lastException;
    }

    private void deleteRecursively(Path targetPath) throws IOException {
        Files.walkFileTree(targetPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                clearReadonly(file);
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                clearReadonly(dir);
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void clearReadonly(Path path) {
        try {
            File file = path.toFile();
            if (!file.canWrite()) {
                file.setWritable(true);
            }
        } catch (Exception e) {
            log.debug("清理只读属性失败，继续尝试删除: {}", path, e);
        }
    }
}
