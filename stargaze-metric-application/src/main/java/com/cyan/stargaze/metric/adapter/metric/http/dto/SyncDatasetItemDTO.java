package com.cyan.stargaze.metric.adapter.metric.http.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 一键同步可选择的数据集列表项。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SyncDatasetItemDTO {

    /** 数据集 ID */
    private String id;

    /** 数据集名称 */
    private String name;

    /** 数据集编码(回退到名称) */
    private String code;

    /** 数据集类型 */
    private String type;

    /** 数据源名称 */
    private String datasource;

    /** 字段数 */
    private Integer fields;

    /** 已同步指标数 */
    private Integer metricCount;

    /** 已同步维度数 */
    private Integer dimensionCount;

    /** 状态 */
    private String status;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private OffsetDateTime updateTime;
}
