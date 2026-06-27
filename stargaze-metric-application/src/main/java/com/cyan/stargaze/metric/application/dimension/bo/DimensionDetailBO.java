package com.cyan.stargaze.metric.application.dimension.bo;

import com.cyan.stargaze.metric.domain.dimension.Dimension;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 维度详情业务对象(含关联数据集与关联指标)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DimensionDetailBO {

    /** 维度领域对象 */
    private Dimension dimension;

    /** 维度显示名 */
    private String dimName;

    /** 维度源字段名 */
    private String dimCode;

    /** 关联数据集 ID 列表 */
    private List<String> relatedDatasets;

    /** 关联指标 ID 列表 */
    private List<String> relatedMetrics;

    /** 关联指标数量 */
    private Integer relatedMetricCount;
}
