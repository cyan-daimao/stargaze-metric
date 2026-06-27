package com.cyan.stargaze.metric.application.metric.bo;

import com.cyan.stargaze.metric.domain.metric.Metric;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标业务对象(应用层组装,含列表/详情所需扩展字段)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricBO {

    /** 指标领域对象 */
    private Metric metric;

    /** 可关联维度名称列表 */
    private List<String> relatedDimensions;

    /** 来源类型展示标签 */
    private String sourceTypeLabel;

    /** 计算逻辑摘要 */
    private String logicSummary;

    /** SQL 预览(仅 SQL 来源) */
    private String sqlPreview;

    /** API 点查计划 JSON(仅 httpApi 来源) */
    private String apiLookupPlan;
}
