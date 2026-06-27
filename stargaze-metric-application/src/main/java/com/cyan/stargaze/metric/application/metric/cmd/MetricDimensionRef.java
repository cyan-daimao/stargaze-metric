package com.cyan.stargaze.metric.application.metric.cmd;

import com.cyan.stargaze.metric.enums.MetricSourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标关联维度引用。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDimensionRef {

    /** 维度 ID */
    private String dimensionId;

    /** 维度业务编码 */
    private String dimensionCode;

    /** 维度名称 */
    private String dimensionName;

    /** 维度来源类型 */
    private MetricSourceType sourceType;

    /** 维度来源编码 */
    private String sourceCode;
}
