package com.cyan.stargaze.metric.domain.metric.repository;

import com.cyan.stargaze.metric.domain.metric.MetricVersion;

import java.util.List;

/**
 * 指标版本仓储接口。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricVersionRepository {

    List<MetricVersion> listByMetric(String metricId);

    MetricVersion save(MetricVersion version);
}
