package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标维度引用（包含维度来源数据集）。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDimensionRefDTO {

    /** 维度 ID 或维度名称 */
    private String dimensionId;

    /** 来源数据集 ID */
    private String datasetId;
}
