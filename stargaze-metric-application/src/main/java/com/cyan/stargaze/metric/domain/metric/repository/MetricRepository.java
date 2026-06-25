package com.cyan.stargaze.metric.domain.metric.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    List<Metric> listByWorkspace(String workspaceId, MetricStatus status);

    IPage<Metric> pageByWorkspace(IPage<Metric> page, String workspaceId, String keyword, MetricStatus status, String folder);

    Metric save(Metric metric);

    Metric update(Metric metric);

    void deleteById(String id);
}
