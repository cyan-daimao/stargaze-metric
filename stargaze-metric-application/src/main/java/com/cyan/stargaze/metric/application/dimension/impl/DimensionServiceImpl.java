package com.cyan.stargaze.metric.application.dimension.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.Page;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.metric.application.MetricAppConvert;
import com.cyan.stargaze.metric.application.dimension.DimensionService;
import com.cyan.stargaze.metric.application.dimension.bo.DimensionDetailBO;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionBindingRepository;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricDimensionBindingRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 维度应用服务实现。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DimensionServiceImpl implements DimensionService {

    private final DimensionRepository dimensionRepository;
    private final DimensionBindingRepository dimensionBindingRepository;
    private final MetricDimensionBindingRepository metricDimensionBindingRepository;
    private final MetricAppConvert convert;
    private final DatasetClient datasetClient;

    @Override
    @Transactional
    public Dimension create(DimensionCmd cmd) {
        Dimension dimension = convert.toDimension(cmd);
        return dimension.save(dimensionRepository);
    }

    @Override
    @Transactional
    public Dimension update(DimensionCmd cmd) {
        Dimension existing = dimensionRepository.findById(cmd.getId());
        Assert.notNull(existing, new SilentException("维度不存在"));
        Dimension dimension = convert.toDimension(cmd);
        dimension.setId(existing.getId());
        dimension.setStatus(existing.getStatus());
        dimension.setCreatedAt(existing.getCreatedAt());
        dimension.setCreatedBy(existing.getCreatedBy());
        return dimension.update(dimensionRepository);
    }

    @Override
    public Dimension findById(String id) {
        Dimension dimension = dimensionRepository.findById(id);
        Assert.notNull(dimension, new SilentException("维度不存在"));
        return dimension;
    }

    @Override
    public DimensionDetailBO findDetail(String id) {
        Dimension dimension = findById(id);
        return buildDetail(dimension);
    }

    @Override
    public List<Dimension> list(boolean publishedOnly) {
        MetricStatus status = publishedOnly ? MetricStatus.PUBLISHED : null;
        return dimensionRepository.list(status);
    }

    @Override
    public List<DimensionDetailBO> listDetail(boolean publishedOnly) {
        return list(publishedOnly).stream().map(this::buildDetail).toList();
    }

    @Override
    public Page<Dimension> page(Integer page, Integer size, String keyword, String folder, String status) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : size;
        MetricStatus metricStatus = MetricStatus.fromCode(status);
        return dimensionRepository.page(p, s, keyword, metricStatus, folder);
    }

    @Override
    public Page<DimensionDetailBO> pageDetail(Integer page, Integer size, String keyword, String folder, String status) {
        Page<Dimension> pageResult = page(page, size, keyword, folder, status);
        List<DimensionDetailBO> records = pageResult.getData().stream()
                .map(this::buildDetail)
                .toList();
        return new Page<>(records, pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
    }

    @Override
    @Transactional
    public void delete(String id) {
        Dimension dimension = findById(id);
        dimension.delete(dimensionRepository);
        dimensionBindingRepository.deleteByDimension(id);
    }

    @Override
    @Transactional
    public Dimension publish(String id) {
        Dimension dimension = findById(id);
        return dimension.publish(dimensionRepository);
    }

    @Override
    @Transactional
    public DimensionBinding addBinding(DimensionBindingCmd cmd) {
        DimensionBinding binding = convert.toDimensionBinding(cmd);
        binding.validate();
        var resp = datasetClient.resolveField(cmd.getDatasetId(), cmd.getFieldId());
        Assert.notNull(resp, new SilentException("字段校验失败:数据集服务无响应"));
        Assert.isTrue(resp.getCode() == 200 && resp.getData() != null,
                new SilentException("字段校验失败:[" + resp.getCode() + "] " + resp.getMessage()));
        return dimensionBindingRepository.save(binding);
    }

    @Override
    @Transactional
    public void removeBinding(String bindingId) {
        dimensionBindingRepository.deleteById(bindingId);
    }

    @Override
    public List<DimensionBinding> listBindings(String dimensionId) {
        return dimensionBindingRepository.listByDimension(dimensionId);
    }

    @Override
    public List<String> listFolders() {
        return dimensionRepository.listDistinctFolders();
    }

    private DimensionDetailBO buildDetail(Dimension dimension) {
        List<DimensionBinding> bindings = dimensionBindingRepository.listByDimension(dimension.getId());
        List<String> datasets = bindings.stream()
                .map(DimensionBinding::getDatasetId)
                .distinct()
                .collect(Collectors.toList());
        var metricBindings = metricDimensionBindingRepository.listByDimensionId(dimension.getId());
        List<String> metrics = metricBindings.stream()
                .map(com.cyan.stargaze.metric.domain.metric.MetricDimensionBinding::getMetricId)
                .distinct()
                .collect(Collectors.toList());
        return new DimensionDetailBO()
                .setDimension(dimension)
                .setDimName(StringUtils.hasText(dimension.getBusinessName()) ? dimension.getBusinessName() : dimension.getName())
                .setDimCode(extractFieldCode(dimension.getDsl()))
                .setRelatedDatasets(datasets)
                .setRelatedMetrics(metrics)
                .setRelatedMetricCount(metrics.size());
    }

    private String extractFieldCode(String dslJson) {
        if (!StringUtils.hasText(dslJson)) {
            return null;
        }
        try {
            JSONObject obj = JSON.parseObject(dslJson);
            JSONObject expr = obj.getJSONObject("expr");
            if (expr != null) {
                return expr.getString("fieldCode");
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
