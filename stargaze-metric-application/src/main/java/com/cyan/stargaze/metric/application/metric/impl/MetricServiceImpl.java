package com.cyan.stargaze.metric.application.metric.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.dataset.client.dto.ResolveFieldDTO;
import com.cyan.stargaze.metric.application.MetricAppConvert;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.FieldRefDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import com.cyan.stargaze.metric.domain.metric.MetricVersion;
import com.cyan.stargaze.metric.domain.metric.repository.MetricBindingRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricCompatRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricVersionRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 指标应用服务实现。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MetricRepository metricRepository;
    private final MetricBindingRepository metricBindingRepository;
    private final MetricVersionRepository metricVersionRepository;
    private final MetricCompatRepository metricCompatRepository;
    private final MetricAppConvert convert;
    private final DatasetClient datasetClient;

    @Override
    @Transactional
    public MetricDTO create(MetricCmd cmd) {
        Metric metric = convert.toMetric(cmd);
        metric = metric.save(metricRepository);
        return toDTO(metric);
    }

    @Override
    @Transactional
    public MetricDTO update(MetricCmd cmd) {
        Metric existing = metricRepository.findById(cmd.getId());
        Assert.notNull(existing, new SilentException("指标不存在"));
        Metric metric = convert.toMetric(cmd);
        metric.setId(existing.getId());
        metric.setStatus(existing.getStatus());
        metric.setVersion(existing.getVersion());
        metric = metric.update(metricRepository);
        return toDTO(metric);
    }

    @Override
    public MetricDTO findById(String id) {
        return toDTO(loadMetric(id));
    }

    @Override
    public List<MetricDTO> list(String workspaceId, boolean publishedOnly) {
        MetricStatus status = publishedOnly ? MetricStatus.PUBLISHED : null;
        return metricRepository.listByWorkspace(workspaceId, status).stream()
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public void delete(String id) {
        Metric metric = loadMetric(id);
        metric.delete(metricRepository);
        metricBindingRepository.deleteByMetric(id);
    }

    @Override
    @Transactional
    public MetricDTO publish(String id) {
        Metric metric = loadMetric(id);
        metric = metric.publish(metricRepository);
        // 记录版本快照
        MetricVersion version = new MetricVersion()
                .setMetricId(metric.getId())
                .setVersion(metric.getVersion())
                .setDsl(metric.getDsl())
                .setCaliber(metric.getCaliber())
                .setChangeLog("发布")
                .setCreatedBy(metric.getUpdatedBy());
        metricVersionRepository.save(version);
        log.info("指标发布 metricId={}, version={}", id, metric.getVersion());
        return toDTO(metric);
    }

    @Override
    @Transactional
    public MetricDTO deprecate(String id) {
        Metric metric = loadMetric(id);
        metric = metric.deprecate(metricRepository);
        return toDTO(metric);
    }

    @Override
    @Transactional
    public MetricBinding addBinding(MetricBindingCmd cmd) {
        MetricBinding binding = convert.toMetricBinding(cmd);
        binding.validate();
        // 跨服务校验字段存在性与类型(dataset resolveField)
        Assert.notNull(datasetClient, new SilentException("数据集客户端未初始化"));
        var resp = datasetClient.resolveField(cmd.getDatasetId(), cmd.getFieldId());
        Assert.notNull(resp, new SilentException("字段校验失败:数据集服务无响应"));
        Assert.isTrue(resp.getCode() == 0 && resp.getData() != null,
                new SilentException("字段校验失败:" + resp.getMessage()));
        return metricBindingRepository.save(binding);
    }

    @Override
    @Transactional
    public void removeBinding(String bindingId) {
        metricBindingRepository.deleteById(bindingId);
    }

    @Override
    public List<MetricBinding> listBindings(String metricId) {
        return metricBindingRepository.listByMetric(metricId);
    }

    @Override
    public MetricResolveDTO resolve(String metricId, String datasetId) {
        Metric metric = loadMetric(metricId);
        // 消费红线:resolve 仅对已发布指标有效
        Assert.isTrue(metric.isPublished(), new SilentException("指标未发布,不可解析"));
        MetricBinding binding = metricBindingRepository.findByMetricAndDataset(metricId, datasetId);
        Assert.notNull(binding, new SilentException("指标未绑定该数据集"));
        // DSL:binding 覆盖优先
        String dsl = (binding.getDslOverride() != null && !binding.getDslOverride().isBlank())
                ? binding.getDslOverride() : metric.getDsl();
        // 字段解析(跨服务)
        List<FieldRefDTO> fields = new ArrayList<>();
        var resp = datasetClient.resolveField(datasetId, binding.getFieldId());
        if (resp != null && resp.getData() != null) {
            ResolveFieldDTO resolved = resp.getData();
            fields.add(new FieldRefDTO()
                    .setFieldId(resolved.getId())
                    .setOriginName(resolved.getOriginName())
                    .setDatasetId(datasetId));
        }
        return new MetricResolveDTO()
                .setMetricId(metricId)
                .setDatasetId(datasetId)
                .setDsl(dsl)
                .setFields(fields)
                .setAgg(metric.getMeasureKind());
    }

    @Override
    public ValidationResultDTO validate(List<String> metricIds, List<String> dimensionIds) {
        if (metricIds == null || dimensionIds == null || metricIds.isEmpty() || dimensionIds.isEmpty()) {
            return new ValidationResultDTO().setValid(true);
        }
        // 校验每个指标×维度组合是否被允许(无记录默认允许)
        for (String metricId : metricIds) {
            for (String dimensionId : dimensionIds) {
                if (!metricCompatRepository.isAllowed(metricId, dimensionId)) {
                    return new ValidationResultDTO().setValid(false)
                            .setReason("指标 " + metricId + " 与维度 " + dimensionId + " 组合不被允许");
                }
            }
        }
        return new ValidationResultDTO().setValid(true);
    }

    private Metric loadMetric(String id) {
        Metric metric = metricRepository.findById(id);
        Assert.notNull(metric, new SilentException("指标不存在"));
        return metric;
    }

    private MetricDTO toDTO(Metric metric) {
        return new MetricDTO()
                .setId(metric.getId())
                .setWorkspaceId(metric.getWorkspaceId())
                .setName(metric.getName())
                .setBusinessName(metric.getBusinessName())
                .setType(metric.getType())
                .setMeasureKind(metric.getMeasureKind())
                .setDsl(metric.getDsl())
                .setCaliber(metric.getCaliber())
                .setStatus(metric.getStatus())
                .setVersion(metric.getVersion())
                .setOwnerId(metric.getOwnerId());
    }
}
