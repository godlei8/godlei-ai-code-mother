package com.godlei.godleiaicodemother.manager;

import com.godlei.godleiaicodemother.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Pattern;

/**
 * COS object storage manager.
 */
@Component
@Slf4j
public class CosManager {

    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z\\d+\\-.]*://.*$");

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    public String uploadFile(String key, File file) {
        PutObjectResult result = putObject(key, file);
        if (result == null) {
            log.error("Failed to upload file to COS, fileName: {}", file.getName());
            return null;
        }

        String url = buildFileUrl(cosClientConfig.getHost(), key);
        log.info("Uploaded file to COS successfully, fileName: {}, url: {}", file.getName(), url);
        return url;
    }

    static String buildFileUrl(String host, String key) {
        return normalizeHost(host) + normalizeKey(key);
    }

    private static String normalizeHost(String host) {
        String trimmedHost = host == null ? "" : host.trim();
        if (trimmedHost.isEmpty()) {
            return "";
        }

        String hostWithoutTrailingSlash = trimmedHost.replaceAll("/+$", "");
        if (URL_SCHEME_PATTERN.matcher(hostWithoutTrailingSlash).matches()) {
            return hostWithoutTrailingSlash;
        }

        return "https://" + hostWithoutTrailingSlash.replaceAll("^/+", "");
    }

    private static String normalizeKey(String key) {
        String trimmedKey = key == null ? "" : key.trim();
        if (trimmedKey.isEmpty()) {
            return "";
        }

        return trimmedKey.startsWith("/") ? trimmedKey : "/" + trimmedKey;
    }
}
