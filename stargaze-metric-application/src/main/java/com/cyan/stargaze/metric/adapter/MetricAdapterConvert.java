package com.cyan.stargaze.metric.adapter;

import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionBindingDTO;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricBindingDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 指标平台适配层转换(Domain -> DTO)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricAdapterConvert {

    MetricAdapterConvert INSTANCE = Mappers.getMapper(MetricAdapterConvert.class);

    @Mapping(target = "dsl", ignore = true)
    @Mapping(target = "caliber", ignore = true)
    MetricDTO toMetricDTO(Metric metric);

    List<MetricDTO> toMetricDTOList(List<Metric> metrics);

    MetricBindingDTO toMetricBindingDTO(MetricBinding binding);

    List<MetricBindingDTO> toMetricBindingDTOList(List<MetricBinding> bindings);

    DimensionDTO toDimensionDTO(Dimension dimension);

    List<DimensionDTO> toDimensionDTOList(List<Dimension> dimensions);

    DimensionBindingDTO toDimensionBindingDTO(DimensionBinding binding);

    List<DimensionBindingDTO> toDimensionBindingDTOList(List<DimensionBinding> bindings);
}
