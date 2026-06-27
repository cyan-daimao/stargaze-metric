package com.cyan.stargaze.metric.client.dto;

import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 指标解析结果(AST,供 query 编译期调用)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricResolveDTO {

    /** 指标业务编码 */
    private String metricCode;

    /** 指标 ID(供 query 作为别名,与 metricCode 一致) */
    private String metricId;

    /** 指标名称 */
    private String metricName;

    /** 指标状态 */
    private MetricStatus status;

    /** 来源类型 */
    private MetricSourceType sourceType;

    /** 来源编码 */
    private String sourceCode;

    /** 来源描述对象 */
    private Map<String, Object> source;

    /** 指标 DSL AST */
    private Map<String, Object> dsl;

    /** 查询能力 */
    private QueryMode queryMode;

    /** 数据新鲜度 */
    private Freshness freshness;

    /** 本次计算涉及的物理/逻辑字段 */
    private List<String> requiredFields;

    /** 解析后的物理字段列表(供 query 编译期使用) */
    private List<ResolvedFieldDTO> fields = new ArrayList<>();

    /** 聚合方式(供 query 编译期使用) */
    private AggregationDTO agg;
}
