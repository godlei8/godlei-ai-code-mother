package com.godlei.godleiaicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Build Vue projects by running npm install and npm run build.
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * Build Vue project asynchronously.
     *
     * @param projectPath project root path
     */
    public void buildProjectAsync(String projectPath) {
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        buildProject(projectPath);
                    } catch (Exception e) {
                        log.error("Async build failed: {}", e.getMessage(), e);
                    }
                });
    }

    /**
     * Build Vue project.
     *
     * @param projectPath project root path
     * @return true when build succeeds
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("Project directory not found: {}", projectPath);
            return false;
        }

        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("package.json not found: {}", projectPath);
            return false;
        }

        log.info("Start building Vue project: {}", projectPath);
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install failed: {}", projectPath);
            return false;
        }

        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build failed: {}", projectPath);
            return false;
        }

        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("Build finished but dist directory not found: {}", projectPath);
            return false;
        }

        log.info("Vue project built successfully: {}", projectPath);
        return true;
    }

    private boolean executeNpmInstall(File projectDir) {
        log.info("Running npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300);
    }

    private boolean executeNpmBuild(File projectDir) {
        log.info("Running npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180);
    }

    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        Process process = null;
        try {
            log.info("Run command in {}: {}", workingDir.getAbsolutePath(), command);
            process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+")
            );

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("Command timed out after {} seconds", timeoutSeconds);
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("Command finished successfully: {}", command);
                return true;
            }

            log.error("Command failed, exit code: {}", exitCode);
            return false;
        } catch (Exception e) {
            log.error("Command execution failed: {}, error: {}", command, e.getMessage());
            return false;
        } finally {
            closeProcessStreams(process);
        }
    }

    private void closeProcessStreams(Process process) {
        if (process == null) {
            return;
        }
        try {
            process.getInputStream().close();
        } catch (Exception ignored) {
        }
        try {
            process.getErrorStream().close();
        } catch (Exception ignored) {
        }
        try {
            process.getOutputStream().close();
        } catch (Exception ignored) {
        }
    }
}
