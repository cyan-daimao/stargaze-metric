package com.cyan.stargaze.metric.adapter;

import com.alibaba.fastjson2.JSON;
import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionBindingDTO;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricBindingDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricDetailDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricListItemDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricSourceBindingDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.SyncDatasetItemDTO;
import com.cyan.stargaze.metric.application.dimension.bo.DimensionDetailBO;
import com.cyan.stargaze.metric.application.metric.bo.MetricBO;
import com.cyan.stargaze.metric.application.metric.bo.SyncDatasetItemBO;
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
 * 指标平台适配层转换(Domain/BO -> DTO)。
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

    // ---- MetricBO -> 对外 DTO ----

    default MetricDTO toMetricDTO(MetricBO bo) {
        if (bo == null || bo.getMetric() == null) {
            return null;
        }
        MetricDTO dto = toMetricDTO(bo.getMetric());
        dto.setRelatedDimensions(bo.getRelatedDimensions());
        dto.setSourceTypeLabel(bo.getSourceTypeLabel());
        dto.setLogicSummary(bo.getLogicSummary());
        dto.setSqlPreview(bo.getSqlPreview());
        dto.setApiLookupPlan(bo.getApiLookupPlan());
        return dto;
    }

    List<MetricDTO> toMetricDTOListFromBO(List<MetricBO> bos);

    default MetricListItemDTO toListItem(MetricBO bo) {
        if (bo == null || bo.getMetric() == null) {
            return null;
        }
        Metric metric = bo.getMetric();
        MetricListItemDTO item = new MetricListItemDTO();
        item.setMetricCode(metric.getMetricCode());
        item.setMetricName(metric.getName());
        item.setDescription(metric.getDescription());
        item.setSourceType(metric.getSourceType());
        item.setSourceCode(metric.getSourceCode());
        item.setSourceName(metric.getSourceName());
        item.setSourceTypeLabel(bo.getSourceTypeLabel());
        item.setLogicSummary(bo.getLogicSummary());
        List<String> dims = bo.getRelatedDimensions();
        if (dims != null) {
            item.setRelatedDimensions(new java.util.ArrayList<>(dims));
        }
        item.setStatus(metric.getStatus());
        item.setUpdatedAt(metric.getUpdatedAt());
        return item;
    }

    List<MetricListItemDTO> toListItemList(List<MetricBO> bos);

    default MetricDetailDTO toDetail(MetricBO bo) {
        if (bo == null || bo.getMetric() == null) {
            return null;
        }
        Metric metric = bo.getMetric();
        MetricDetailDTO detail = new MetricDetailDTO()
                .setMetricCode(metric.getMetricCode())
                .setMetricName(metric.getName())
                .setDescription(metric.getDescription())
                .setFolder(metric.getFolder())
                .setFormat(metric.getFormat())
                .setStatus(metric.getStatus())
                .setSourceType(metric.getSourceType())
                .setSourceCode(metric.getSourceCode())
                .setSourceName(metric.getSourceName())
                .setSourceTypeLabel(bo.getSourceTypeLabel())
                .setQueryMode(metric.getQueryMode())
                .setFreshness(metric.getFreshness())
                .setDslKind(metric.getDslKind())
                .setSourceSnapshot(metric.getSourceSnapshot())
                .setSupports(metric.getSupports())
                .setPrecision(metric.getPrecision())
                .setRelatedDimensions(bo.getRelatedDimensions())
                .setLogicSummary(bo.getLogicSummary())
                .setSqlPreview(bo.getSqlPreview())
                .setCreatedBy(metric.getCreatedBy())
                .setUpdatedBy(metric.getUpdatedBy())
                .setCreatedAt(metric.getCreatedAt())
                .setUpdatedAt(metric.getUpdatedAt());

        detail.setSourceBinding(new MetricSourceBindingDTO()
                .setSourceType(metric.getSourceType())
                .setSourceCode(metric.getSourceCode())
                .setSourceName(metric.getSourceName())
                .setQueryMode(metric.getQueryMode())
                .setFreshness(metric.getFreshness())
                .setSnapshot(parseJsonMap(metric.getSourceSnapshot())));

        detail.setDsl(parseJsonMap(metric.getDsl()));
        detail.setApiLookupPlan(parseJsonMap(bo.getApiLookupPlan()));
        return detail;
    }

    List<MetricDetailDTO> toDetailList(List<MetricBO> bos);

    MetricBindingDTO toMetricBindingDTO(MetricBinding binding);

    List<MetricBindingDTO> toMetricBindingDTOList(List<MetricBinding> bindings);

    // ---- Dimension -> 前端/对外 DTO ----

    /**
     * Dimension -> 前端维度 DTO(名称取 code,显示名取 businessName 回退)。
     */
    default DimensionDTO toDimensionDTO(Dimension dimension) {
        if (dimension == null) {
            return null;
        }
        DimensionDTO dto = new DimensionDTO();
        dto.setId(dimension.getId());
        dto.setName(dimension.getCode());
        dto.setDimName(dimension.displayName());
        dto.setDimCode(dimension.extractFieldCode());
        dto.setFolder(dimension.getFolder());
        dto.setSemanticType(dimension.getSemanticType());
        dto.setDictionaryId(dimension.getDictionaryId());
        dto.setFormat(dimension.getFormat());
        dto.setStatus(dimension.getStatus());
        dto.setCreatedBy(dimension.getCreatedBy());
        dto.setCreatedAt(dimension.getCreatedAt());
        dto.setUpdatedAt(dimension.getUpdatedAt());
        return dto;
    }

    List<DimensionDTO> toDimensionDTOList(List<Dimension> dimensions);

    default DimensionDTO toDimensionDTO(DimensionDetailBO bo) {
        if (bo == null || bo.getDimension() == null) {
            return null;
        }
        DimensionDTO dto = toDimensionDTO(bo.getDimension());
        dto.setRelatedDatasets(bo.getRelatedDatasets());
        dto.setRelatedMetrics(bo.getRelatedMetrics());
        dto.setRelatedMetricCount(bo.getRelatedMetricCount());
        return dto;
    }

    List<DimensionDTO> toDimensionDTOListFromDetail(List<DimensionDetailBO> bos);

    /**
     * Dimension -> 客户端契约维度 DTO。
     */
    default com.cyan.stargaze.metric.client.dto.DimensionDTO toClientDimensionDTO(Dimension dimension) {
        if (dimension == null) {
            return null;
        }
        com.cyan.stargaze.metric.client.dto.DimensionDTO dto = new com.cyan.stargaze.metric.client.dto.DimensionDTO();
        dto.setId(dimension.getId());
        dto.setName(dimension.getCode());
        dto.setDimName(dimension.displayName());
        dto.setDimCode(dimension.extractFieldCode());
        dto.setFolder(dimension.getFolder());
        dto.setSemanticType(dimension.getSemanticType());
        dto.setDictionaryId(dimension.getDictionaryId());
        dto.setFormat(dimension.getFormat());
        dto.setStatus(dimension.getStatus());
        dto.setCreatedBy(dimension.getCreatedBy());
        dto.setCreatedAt(dimension.getCreatedAt());
        dto.setUpdatedAt(dimension.getUpdatedAt());
        return dto;
    }

    DimensionBindingDTO toDimensionBindingDTO(DimensionBinding binding);

    List<DimensionBindingDTO> toDimensionBindingDTOList(List<DimensionBinding> bindings);

    // ---- SyncDatasetItemBO -> DTO ----

    SyncDatasetItemDTO toSyncDatasetItemDTO(SyncDatasetItemBO bo);

    List<SyncDatasetItemDTO> toSyncDatasetItemDTOList(List<SyncDatasetItemBO> bos);

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
