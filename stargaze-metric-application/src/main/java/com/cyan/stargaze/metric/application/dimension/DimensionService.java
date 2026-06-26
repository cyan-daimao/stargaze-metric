package com.cyan.stargaze.metric.application.dimension;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;

import java.util.List;

/**
 * 维度应用服务。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionService {

    Dimension create(DimensionCmd cmd);

    Dimension update(DimensionCmd cmd);

    Dimension findById(String id);

    List<Dimension> list(boolean publishedOnly);

    IPage<Dimension> page(Integer page, Integer size, String keyword, String folder, String status);

    void delete(String id);

    Dimension publish(String id);

    DimensionBinding addBinding(DimensionBindingCmd cmd);

    void removeBinding(String bindingId);

    List<DimensionBinding> listBindings(String dimensionId);
}
