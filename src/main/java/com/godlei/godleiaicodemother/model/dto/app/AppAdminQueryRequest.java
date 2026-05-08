package com.godlei.godleiaicodemother.model.dto.app;

import com.godlei.godleiaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员分页查询应用（支持按除时间字段外的条件筛选）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppAdminQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String appName;

    private String cover;

    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private Integer priority;

    private Long userId;

    private static final long serialVersionUID = 1L;
}
