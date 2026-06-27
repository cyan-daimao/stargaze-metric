package com.cyan.stargaze.metric.domain.metric.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.enums.MetricStatus;

import java.util.List;

/**
 * 指标仓储接口。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricRepository {

    Metric findById(String id);

    Metric findByMetricCode(String metricCode);

    Metric findByName(String name);

    Metric findByCode(String code);

    List<Metric> list(MetricStatus status);

    IPage<Metric> page(IPage<Metric> page, String keyword, MetricStatus status, String folder);

    Metric save(Metric metric);

    Metric update(Metric metric);

    void deleteById(String id);

    /** 列出所有不重复的目录名 */
    List<String> listDistinctFolders();
}
