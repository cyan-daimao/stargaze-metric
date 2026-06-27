package com.cyan.stargaze.metric.domain.dimension.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.enums.MetricStatus;

import java.util.List;

/**
 * 维度仓储接口。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionRepository {

    Dimension findById(String id);

    Dimension findByName(String name);

    /**
     * 按业务编码查询维度。
     */
    Dimension findByCode(String code);

    List<Dimension> list(MetricStatus status);

    Page<Dimension> page(int current, int size, String keyword, MetricStatus status, String folder);

    Dimension save(Dimension dimension);

    Dimension update(Dimension dimension);

    void deleteById(String id);

    /** 列出所有不重复的目录名 */
    List<String> listDistinctFolders();
}
