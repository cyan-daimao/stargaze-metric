package com.cyan.stargaze.metric.client.dto;

import com.cyan.stargaze.metric.enums.MeasureKind;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标解析结果(纯函数,query 编译期调用)。
 * <p>
 * 给定指标与数据集,返回该数据集下的 DSL、涉及物理字段、聚合方式。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricResolveDTO {

    /** 指标 ID */
    private String metricId;

    /** 数据集 ID */
    private String datasetId;

    /** 该数据集下的 DSL(含 dsl_override 覆盖) */
    private String dsl;

    /** 涉及的物理字段 */
    private List<FieldRefDTO> fields;

    /** 聚合方式 */
    private MeasureKind agg;
}
