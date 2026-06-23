package com.cyan.stargaze.metric.infra.persistence;

import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import com.cyan.stargaze.metric.domain.metric.MetricDimensionCompat;
import com.cyan.stargaze.metric.domain.metric.MetricVersion;
import com.cyan.stargaze.metric.infra.persistence.dimension.dos.DimensionBindingDO;
import com.cyan.stargaze.metric.infra.persistence.dimension.dos.DimensionDO;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricBindingDO;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricDO;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricDimensionCompatDO;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricVersionDO;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 指标平台统一 DO <-> Domain 转换。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public abstract class MetricInfraConvert {

    @Mapping(target = "id", source = "id", qualifiedByName = "l2s")
    @Mapping(target = "workspaceId", source = "workspaceId", qualifiedByName = "l2s")
    @Mapping(target = "ownerId", source = "ownerId", qualifiedByName = "l2s")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "l2s")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "l2s")
    public abstract Metric toMetric(MetricDO doObj);

    @Mapping(target = "id", source = "id", qualifiedByName = "s2l")
    @Mapping(target = "workspaceId", source = "workspaceId", qualifiedByName = "s2l")
    @Mapping(target = "ownerId", source = "ownerId", qualifiedByName = "s2l")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "s2l")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "s2l")
    public abstract MetricDO toMetricDO(Metric metric);

    @Mapping(target = "id", source = "id", qualifiedByName = "l2s")
    @Mapping(target = "metricId", source = "metricId", qualifiedByName = "l2s")
    @Mapping(target = "datasetId", source = "datasetId", qualifiedByName = "l2s")
    @Mapping(target = "fieldId", source = "fieldId", qualifiedByName = "l2s")
    public abstract MetricBinding toMetricBinding(MetricBindingDO doObj);

    @Mapping(target = "id", source = "id", qualifiedByName = "s2l")
    @Mapping(target = "metricId", source = "metricId", qualifiedByName = "s2l")
    @Mapping(target = "datasetId", source = "datasetId", qualifiedByName = "s2l")
    @Mapping(target = "fieldId", source = "fieldId", qualifiedByName = "s2l")
    public abstract MetricBindingDO toMetricBindingDO(MetricBinding binding);

    @Mapping(target = "id", source = "id", qualifiedByName = "l2s")
    @Mapping(target = "workspaceId", source = "workspaceId", qualifiedByName = "l2s")
    @Mapping(target = "dictionaryId", source = "dictionaryId", qualifiedByName = "l2s")
    @Mapping(target = "ownerId", source = "ownerId", qualifiedByName = "l2s")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "l2s")
    public abstract Dimension toDimension(DimensionDO doObj);

    @Mapping(target = "id", source = "id", qualifiedByName = "s2l")
    @Mapping(target = "workspaceId", source = "workspaceId", qualifiedByName = "s2l")
    @Mapping(target = "dictionaryId", source = "dictionaryId", qualifiedByName = "s2l")
    @Mapping(target = "ownerId", source = "ownerId", qualifiedByName = "s2l")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "s2l")
    public abstract DimensionDO toDimensionDO(Dimension dimension);

    @Mapping(target = "id", source = "id", qualifiedByName = "l2s")
    @Mapping(target = "dimensionId", source = "dimensionId", qualifiedByName = "l2s")
    @Mapping(target = "datasetId", source = "datasetId", qualifiedByName = "l2s")
    @Mapping(target = "fieldId", source = "fieldId", qualifiedByName = "l2s")
    public abstract DimensionBinding toDimensionBinding(DimensionBindingDO doObj);

    @Mapping(target = "id", source = "id", qualifiedByName = "s2l")
    @Mapping(target = "dimensionId", source = "dimensionId", qualifiedByName = "s2l")
    @Mapping(target = "datasetId", source = "datasetId", qualifiedByName = "s2l")
    @Mapping(target = "fieldId", source = "fieldId", qualifiedByName = "s2l")
    public abstract DimensionBindingDO toDimensionBindingDO(DimensionBinding binding);

    @Mapping(target = "id", source = "id", qualifiedByName = "l2s")
    @Mapping(target = "metricId", source = "metricId", qualifiedByName = "l2s")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "l2s")
    public abstract MetricVersion toMetricVersion(MetricVersionDO doObj);

    @Mapping(target = "id", source = "id", qualifiedByName = "s2l")
    @Mapping(target = "metricId", source = "metricId", qualifiedByName = "s2l")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "s2l")
    public abstract MetricVersionDO toMetricVersionDO(MetricVersion version);

    @Mapping(target = "metricId", source = "metricId", qualifiedByName = "l2s")
    @Mapping(target = "dimensionId", source = "dimensionId", qualifiedByName = "l2s")
    public abstract MetricDimensionCompat toCompat(MetricDimensionCompatDO doObj);

    @Mapping(target = "metricId", source = "metricId", qualifiedByName = "s2l")
    @Mapping(target = "dimensionId", source = "dimensionId", qualifiedByName = "s2l")
    public abstract MetricDimensionCompatDO toCompatDO(MetricDimensionCompat compat);

    public abstract List<MetricDimensionCompat> toCompatList(List<MetricDimensionCompatDO> doList);

    @Named("l2s")
    protected String l2s(Long v) {
        return IdUtil.toString(v);
    }

    @Named("s2l")
    protected Long s2l(String v) {
        return IdUtil.toLong(v);
    }
}
