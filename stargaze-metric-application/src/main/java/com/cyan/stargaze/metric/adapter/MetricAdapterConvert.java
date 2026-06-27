package com.cyan.stargaze.metric.adapter;

import com.alibaba.fastjson2.JSON;
import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionBindingDTO;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricBindingDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricDetailDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricListItemDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricSourceBindingDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 指标平台适配层转换(Domain -> DTO)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricAdapterConvert {

    MetricAdapterConvert INSTANCE = Mappers.getMapper(MetricAdapterConvert.class);

    @Mapping(target = "relatedDimensions", ignore = true)
    @Mapping(target = "sourceTypeLabel", ignore = true)
    @Mapping(target = "logicSummary", ignore = true)
    @Mapping(target = "sqlPreview", ignore = true)
    @Mapping(target = "apiLookupPlan", ignore = true)
    MetricDTO toMetricDTO(Metric metric);

    List<MetricDTO> toMetricDTOList(List<Metric> metrics);

    @Mapping(target = "metricName", source = "name")
    MetricListItemDTO toListItem(MetricDTO dto);

    List<MetricListItemDTO> toListItemList(List<MetricDTO> dtos);

    default MetricDetailDTO toDetail(MetricDTO dto) {
        if (dto == null) {
            return null;
        }
        MetricDetailDTO detail = new MetricDetailDTO()
                .setMetricCode(dto.getMetricCode())
                .setMetricName(dto.getName())
                .setDescription(dto.getDescription())
                .setFolder(dto.getFolder())
                .setFormat(dto.getFormat())
                .setStatus(dto.getStatus())
                .setSourceType(dto.getSourceType())
                .setSourceCode(dto.getSourceCode())
                .setSourceName(dto.getSourceName())
                .setSourceTypeLabel(dto.getSourceTypeLabel())
                .setQueryMode(dto.getQueryMode())
                .setFreshness(dto.getFreshness())
                .setDslKind(dto.getDslKind())
                .setSourceSnapshot(dto.getSourceSnapshot())
                .setSupports(dto.getSupports())
                .setPrecision(dto.getPrecision())
                .setRelatedDimensions(dto.getRelatedDimensions())
                .setLogicSummary(dto.getLogicSummary())
                .setSqlPreview(dto.getSqlPreview())
                .setCreatedBy(dto.getCreatedBy())
                .setUpdatedBy(dto.getUpdatedBy())
                .setCreatedAt(dto.getCreatedAt())
                .setUpdatedAt(dto.getUpdatedAt());

        detail.setSourceBinding(new MetricSourceBindingDTO()
                .setSourceType(dto.getSourceType())
                .setSourceCode(dto.getSourceCode())
                .setSourceName(dto.getSourceName())
                .setQueryMode(dto.getQueryMode())
                .setFreshness(dto.getFreshness())
                .setSnapshot(parseJsonMap(dto.getSourceSnapshot())));

        detail.setDsl(parseJsonMap(dto.getDsl()));
        detail.setApiLookupPlan(parseJsonMap(dto.getApiLookupPlan()));
        return detail;
    }

    List<MetricDetailDTO> toDetailList(List<MetricDTO> dtos);

    MetricBindingDTO toMetricBindingDTO(MetricBinding binding);

    List<MetricBindingDTO> toMetricBindingDTOList(List<MetricBinding> bindings);

    @Mapping(target = "relatedMetrics", ignore = true)
    @Mapping(target = "relatedMetricCount", ignore = true)
    @Mapping(target = "relatedDatasets", ignore = true)
    DimensionDTO toDimensionDTO(Dimension dimension);

    List<DimensionDTO> toDimensionDTOList(List<Dimension> dimensions);

    DimensionBindingDTO toDimensionBindingDTO(DimensionBinding binding);

    List<DimensionBindingDTO> toDimensionBindingDTOList(List<DimensionBinding> bindings);

    default Map<String, Object> parseJsonMap(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return JSON.parseObject(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
