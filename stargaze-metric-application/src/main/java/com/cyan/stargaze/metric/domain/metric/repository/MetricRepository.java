package com.cyan.stargaze.metric.domain.metric.repository;

import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.enums.MetricStatus;

import java.util.List;

/**
 * 指标仓储接口(聚合 MetricBinding)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricRepository {

    Metric findById(String id);

    Metric findByName(String workspaceId, String name);

    List<Metric> listByWorkspace(String workspaceId, MetricStatus status);

    Metric save(Metric metric);

    Metric update(Metric metric);

    void deleteById(String id);
}
