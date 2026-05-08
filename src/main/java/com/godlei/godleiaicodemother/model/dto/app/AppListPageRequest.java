package com.godlei.godleiaicodemother.model.dto.app;

import com.godlei.godleiaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户分页查询自己的应用列表 / 分页查询精选应用列表（支持应用名称模糊查询）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppListPageRequest extends PageRequest implements Serializable {

    /**
     * 应用名称（模糊）
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
