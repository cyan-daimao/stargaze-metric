package com.cyan.stargaze.metric.application;

import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 指标平台应用层转换(Cmd -> Domain)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricAppConvert {

    MetricAppConvert INSTANCE = Mappers.getMapper(MetricAppConvert.class);

    Metric toMetric(MetricCmd cmd);

    MetricBinding toMetricBinding(MetricBindingCmd cmd);

    Dimension toDimension(DimensionCmd cmd);

    DimensionBinding toDimensionBinding(DimensionBindingCmd cmd);
}
