package com.cyan.stargaze.metric.domain.metric.repository;

import com.cyan.stargaze.metric.domain.metric.MetricDimensionBinding;

import java.util.List;

/**
 * 指标维度绑定仓储接口。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricDimensionBindingRepository {

    /** 查询指标全部维度绑定 */
    List<MetricDimensionBinding> listByMetric(String metricId);

    /** 批量保存 */
    void saveBatch(String metricId, List<MetricDimensionBinding> bindings);

    /** 删除指标全部维度绑定 */
    void deleteByMetric(String metricId);

    /** 查询维度被哪些指标绑定 */
    List<MetricDimensionBinding> listByDimensionId(String dimensionId);
}
