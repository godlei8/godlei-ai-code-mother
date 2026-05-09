package com.godlei.godleiaicodemother.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员对话历史视图（附带应用名称）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryAdminVO extends ChatHistoryVO implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
