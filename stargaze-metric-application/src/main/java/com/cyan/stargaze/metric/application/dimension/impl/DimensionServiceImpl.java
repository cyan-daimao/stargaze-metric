package com.cyan.stargaze.metric.application.dimension.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.metric.application.MetricAppConvert;
import com.cyan.stargaze.metric.application.dimension.DimensionService;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionBindingRepository;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return dimension.update(dimensionRepository);
    }

    @Override
    public Dimension findById(String id) {
        Dimension dimension = dimensionRepository.findById(id);
        Assert.notNull(dimension, new SilentException("维度不存在"));
        return dimension;
    }

    @Override
    public List<Dimension> list(String workspaceId, boolean publishedOnly) {
        MetricStatus status = publishedOnly ? MetricStatus.PUBLISHED : null;
        return dimensionRepository.listByWorkspace(workspaceId, status);
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
                new SilentException("字段校验失败:" + resp.getMessage()));
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
}
