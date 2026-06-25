package com.cyan.stargaze.metric.domain.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 指标-维度绑定（指标关联哪些维度字段）。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDimensionBinding {

    /** 主键 */
    private String id;

    /** 指标 ID */
    private String metricId;

    /** 维度 ID */
    private String dimensionId;

    /** 维度字段名（跨数据集统一命名） */
    private String dimensionName;

    /** 来源数据集 ID */
    private String datasetId;

    /** 来源字段 ID */
    private String fieldId;

    /** 创建时间 */
    private OffsetDateTime createdAt;

    /** 更新时间 */
    private OffsetDateTime updatedAt;

    /** 逻辑删除时间 */
    private OffsetDateTime deletedAt;
}
