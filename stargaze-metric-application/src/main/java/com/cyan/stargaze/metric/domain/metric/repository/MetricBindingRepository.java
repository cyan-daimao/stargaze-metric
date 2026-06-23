package com.cyan.stargaze.metric.domain.metric.repository;

import com.cyan.stargaze.metric.domain.metric.MetricBinding;

import java.util.List;

/**
 * 指标绑定仓储接口。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricBindingRepository {

    /** 查询指标在某数据集的绑定 */
    MetricBinding findByMetricAndDataset(String metricId, String datasetId);

    /** 查询指标全部绑定 */
    List<MetricBinding> listByMetric(String metricId);

    MetricBinding save(MetricBinding binding);

    void deleteById(String id);

    void deleteByMetric(String metricId);
}
