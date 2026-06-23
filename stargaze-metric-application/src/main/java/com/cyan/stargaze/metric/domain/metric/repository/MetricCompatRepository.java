package com.cyan.stargaze.metric.domain.metric.repository;

import com.cyan.stargaze.metric.domain.metric.MetricDimensionCompat;

import java.util.List;

/**
 * 指标×维度组合合法性仓储接口。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricCompatRepository {

    /** 查询指标的全部组合规则 */
    List<MetricDimensionCompat> listByMetric(String metricId);

    /** 查询指标×维度组合是否允许(无记录默认允许) */
    boolean isAllowed(String metricId, String dimensionId);

    MetricDimensionCompat save(MetricDimensionCompat compat);
}
