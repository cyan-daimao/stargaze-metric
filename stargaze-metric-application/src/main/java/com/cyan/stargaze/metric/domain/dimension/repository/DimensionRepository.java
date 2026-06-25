package com.cyan.stargaze.metric.domain.dimension.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    Dimension findByName(String workspaceId, String name);

    List<Dimension> listByWorkspace(String workspaceId, MetricStatus status);

    IPage<Dimension> pageByWorkspace(IPage<Dimension> page, String workspaceId, String keyword, MetricStatus status, String folder);

    Dimension save(Dimension dimension);

    Dimension update(Dimension dimension);

    void deleteById(String id);
}
