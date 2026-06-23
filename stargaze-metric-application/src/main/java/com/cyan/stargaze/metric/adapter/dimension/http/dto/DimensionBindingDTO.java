package com.cyan.stargaze.metric.adapter.dimension.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 维度绑定 DTO。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DimensionBindingDTO {

    /** 主键 */
    private String id;

    /** 维度 ID */
    private String dimensionId;

    /** 数据集 ID */
    private String datasetId;

    /** 字段 ID */
    private String fieldId;

    /** 维度计算表达式 */
    private String expr;
}
