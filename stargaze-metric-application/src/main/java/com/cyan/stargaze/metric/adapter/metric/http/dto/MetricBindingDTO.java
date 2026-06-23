package com.cyan.stargaze.metric.adapter.metric.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标绑定 DTO。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricBindingDTO {

    /** 主键 */
    private String id;

    /** 指标 ID */
    private String metricId;

    /** 数据集 ID */
    private String datasetId;

    /** 字段 ID */
    private String fieldId;

    /** 该数据集下 DSL 覆盖 */
    private String dslOverride;
}
