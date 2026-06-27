package com.cyan.stargaze.metric.application;

import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.stargaze.dataset.client.dto.DatasetListItemDTO;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.application.metric.bo.SyncDatasetItemBO;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;

/**
 * 指标平台应用层转换(Cmd -> Domain / 跨服务 DTO -> BO)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricAppConvert {

    MetricAppConvert INSTANCE = Mappers.getMapper(MetricAppConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "save", ignore = true)
    @Mapping(target = "update", ignore = true)
    @Mapping(target = "publish", ignore = true)
    @Mapping(target = "offline", ignore = true)
    Metric toMetric(MetricCmd cmd);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    MetricBinding toMetricBinding(MetricBindingCmd cmd);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "businessName", source = "businessName")
    @Mapping(target = "semanticType", source = "semanticType")
    @Mapping(target = "dictionaryId", source = "dictionaryId")
    @Mapping(target = "format", source = "format")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "save", ignore = true)
    @Mapping(target = "update", ignore = true)
    @Mapping(target = "publish", ignore = true)
    @Mapping(target = "offline", ignore = true)
    Dimension toDimension(DimensionCmd cmd);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    DimensionBinding toDimensionBinding(DimensionBindingCmd cmd);

    /**
     * Domain Metric -> 客户端契约 MetricDTO(供应用层组装同步结果使用)。
     */
    @Mapping(target = "relatedDimensions", ignore = true)
    @Mapping(target = "sourceTypeLabel", ignore = true)
    @Mapping(target = "logicSummary", ignore = true)
    @Mapping(target = "sqlPreview", ignore = true)
    @Mapping(target = "apiLookupPlan", ignore = true)
    MetricDTO toMetricDTO(Metric metric);

    /**
     * Domain Dimension -> 客户端契约 DimensionDTO(供应用层组装同步结果使用)。
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

    /**
     * 数据集客户端列表项 -> 一键同步 BO(字段回退逻辑收敛在 Convert)。
     */
    default SyncDatasetItemBO toSyncDatasetItemBO(DatasetListItemDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SyncDatasetItemBO()
                .setId(dto.getId())
                .setName(StringUtils.hasText(dto.getDisplayName()) ? dto.getDisplayName() : dto.getName())
                .setCode(StringUtils.hasText(dto.getName()) ? dto.getName() : dto.getId())
                .setType(dto.getSourceType())
                .setDatasource(dto.getDatasourceName())
                .setFields(dto.getFieldCount() == null ? 0 : dto.getFieldCount())
                .setMetricCount(dto.getMeasureCount() == null ? 0 : dto.getMeasureCount())
                .setDimensionCount(dto.getDimensionCount() == null ? 0 : dto.getDimensionCount())
                .setStatus(dto.getStatus())
                .setUpdateTime(dto.getUpdatedAt());
    }
}
