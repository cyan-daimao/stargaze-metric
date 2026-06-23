package com.cyan.stargaze.metric.domain.dimension.repository;

import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;

import java.util.List;

/**
 * 维度绑定仓储接口。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionBindingRepository {

    DimensionBinding findByDimensionAndDataset(String dimensionId, String datasetId);

    List<DimensionBinding> listByDimension(String dimensionId);

    DimensionBinding save(DimensionBinding binding);

    void deleteById(String id);

    void deleteByDimension(String dimensionId);
}
