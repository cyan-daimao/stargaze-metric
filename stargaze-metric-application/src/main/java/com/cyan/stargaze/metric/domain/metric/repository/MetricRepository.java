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

    Metric findByCode(String workspaceId, String code);

    /**
     * 按主数据集与表达式查找已存在的指标（一键同步去重用）
     */
    Metric findByDatasetAndExpression(String workspaceId, String primaryDatasetId, String expression);

    List<Metric> listByWorkspace(String workspaceId, MetricStatus status);

    List<Metric> listByWorkspace(String workspaceId, String keyword, MetricStatus status, String folder);

    Metric save(Metric metric);

    Metric update(Metric metric);

    void deleteById(String id);
}
