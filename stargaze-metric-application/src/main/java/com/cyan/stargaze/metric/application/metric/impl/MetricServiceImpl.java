package com.cyan.stargaze.metric.application.metric.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.dataset.client.dto.DatasetFieldDTO;
import com.cyan.stargaze.dataset.client.dto.ResolveFieldDTO;
import com.cyan.stargaze.dataset.enums.FieldType;
import com.cyan.stargaze.metric.application.MetricAppConvert;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.CheckDimensionResultDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.FieldRefDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncResultDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import com.cyan.stargaze.metric.domain.metric.MetricDimensionBinding;
import com.cyan.stargaze.metric.domain.metric.MetricVersion;
import com.cyan.stargaze.metric.domain.metric.repository.MetricBindingRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricCompatRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricDimensionBindingRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricVersionRepository;
import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final MetricDimensionBindingRepository metricDimensionBindingRepository;
    private final MetricVersionRepository metricVersionRepository;
    private final MetricCompatRepository metricCompatRepository;
    private final MetricAppConvert convert;
    private final DatasetClient datasetClient;

    @Override
    @Transactional
    public MetricDTO create(MetricCmd cmd) {
        Metric metric = convert.toMetric(cmd);
        metric = metric.save(metricRepository);
        saveBindings(metric.getId(), cmd);
        saveDimensionBindings(metric.getId(), cmd);
        return enrichDTO(toDTO(metric), metric.getId());
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
        saveBindings(metric.getId(), cmd);
        saveDimensionBindings(metric.getId(), cmd);
        return enrichDTO(toDTO(metric), metric.getId());
    }

    @Override
    public MetricDTO findById(String id) {
        return enrichDTO(toDTO(loadMetric(id)), id);
    }

    @Override
    public List<MetricDTO> list(String workspaceId, boolean publishedOnly) {
        MetricStatus status = publishedOnly ? MetricStatus.PUBLISHED : null;
        return metricRepository.listByWorkspace(workspaceId, status).stream()
                .map(m -> enrichDTO(toDTO(m), m.getId())).toList();
    }

    @Override
    public List<MetricDTO> list(String workspaceId, String keyword, String status, String folder) {
        MetricStatus metricStatus = null;
        if (status != null && !status.isBlank()) {
            metricStatus = MetricStatus.fromCode(status);
        }
        return metricRepository.listByWorkspace(workspaceId, keyword, metricStatus, folder).stream()
                .map(m -> enrichDTO(toDTO(m), m.getId())).toList();
    }

    @Override
    @Transactional
    public void delete(String id) {
        Metric metric = loadMetric(id);
        metric.delete(metricRepository);
        metricBindingRepository.deleteByMetric(id);
        metricDimensionBindingRepository.deleteByMetric(id);
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
                .setDsl(metric.getExpression())
                .setCaliber(metric.getDescription())
                .setChangeLog("发布")
                .setCreatedBy(metric.getUpdatedBy());
        metricVersionRepository.save(version);
        log.info("指标发布 metricId={}, version={}", id, metric.getVersion());
        return enrichDTO(toDTO(metric), id);
    }

    @Override
    @Transactional
    public MetricDTO offline(String id) {
        Metric metric = loadMetric(id);
        metric = metric.offline(metricRepository);
        return enrichDTO(toDTO(metric), id);
    }

    @Override
    @Transactional
    public MetricDTO deprecate(String id) {
        Metric metric = loadMetric(id);
        metric = metric.deprecate(metricRepository);
        return enrichDTO(toDTO(metric), id);
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
        Assert.isTrue(resp.getCode() == 200 && resp.getData() != null,
                new SilentException("字段校验失败:[" + resp.getCode() + "] " + resp.getMessage()));
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
    public CheckNameResultDTO checkName(String workspaceId, String name, String excludeId) {
        if (name == null || name.isBlank()) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("指标名称不能为空");
        }
        Metric existing = metricRepository.findByName(workspaceId, name.trim());
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            return new CheckNameResultDTO()
                    .setAvailable(false)
                    .setMessage("该指标名称已存在，建议修改以避免混淆");
        }
        return new CheckNameResultDTO().setAvailable(true);
    }

    @Override
    public CheckNameResultDTO checkCode(String workspaceId, String code, String excludeId) {
        if (code == null || code.isBlank()) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("指标标识不能为空");
        }
        Metric existing = metricRepository.findByCode(workspaceId, code.trim());
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            return new CheckNameResultDTO()
                    .setAvailable(false)
                    .setMessage("该指标标识已存在，建议修改以避免混淆");
        }
        return new CheckNameResultDTO().setAvailable(true);
    }

    @Override
    public CheckDimensionResultDTO checkDimensions(List<String> datasetIds) {
        if (CollectionUtils.isEmpty(datasetIds)) {
            return new CheckDimensionResultDTO().setHasDuplicate(false).setDuplicates(Collections.emptyList());
        }
        // 聚合各数据集维度字段（以 originName/alias 作为维度名）
        Map<String, Set<String>> nameToDatasets = new HashMap<>();
        for (String datasetId : datasetIds) {
            var resp = datasetClient.listFields(datasetId);
            if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
                log.warn("维度查重跳过不可用数据集 datasetId={}, code={}, message={}",
                        datasetId, resp == null ? "null" : resp.getCode(),
                        resp == null ? "null" : resp.getMessage());
                continue;
            }
            for (DatasetFieldDTO field : resp.getData()) {
                if (field.getFieldType() != FieldType.DIMENSION) {
                    continue;
                }
                String dimName = field.getAlias() != null && !field.getAlias().isBlank()
                        ? field.getAlias() : field.getOriginName();
                if (dimName == null) {
                    continue;
                }
                nameToDatasets.computeIfAbsent(dimName, k -> new HashSet<>()).add(datasetId);
            }
        }
        List<CheckDimensionResultDTO.DuplicateDimensionDTO> duplicates = nameToDatasets.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> new CheckDimensionResultDTO.DuplicateDimensionDTO()
                        .setDimensionName(e.getKey())
                        .setDatasetCount(e.getValue().size())
                        .setDatasetIds(new ArrayList<>(e.getValue())))
                .toList();
        return new CheckDimensionResultDTO()
                .setHasDuplicate(!duplicates.isEmpty())
                .setDuplicates(duplicates);
    }

    @Override
    @Transactional
    public List<MetricSyncResultDTO> syncFromDatasets(String workspaceId, List<String> datasetIds, String createdBy) {
        List<MetricSyncResultDTO> results = new ArrayList<>();
        for (String datasetId : datasetIds) {
            MetricSyncResultDTO result = new MetricSyncResultDTO()
                    .setDatasetId(datasetId)
                    .setCreated(new ArrayList<>())
                    .setSkippedDuplicates(new ArrayList<>())
                    .setErrors(new ArrayList<>());
            var resp = datasetClient.listFields(datasetId);
            if (resp == null) {
                log.error("同步指标失败: 数据集服务无响应 datasetId={}", datasetId);
                result.getErrors().add("数据集服务无响应(datasetClient.listFields 返回 null)");
                results.add(result);
                continue;
            }
            if (resp.getCode() != 200) {
                log.error("同步指标失败: 数据集服务返回非成功码 datasetId={}, code={}, message={}",
                        datasetId, resp.getCode(), resp.getMessage());
                result.getErrors().add("数据集服务返回错误: [" + resp.getCode() + "] " + resp.getMessage());
                results.add(result);
                continue;
            }
            if (resp.getData() == null) {
                log.error("同步指标失败: 数据集服务返回 data 为空 datasetId={}", datasetId);
                result.getErrors().add("数据集字段数据为空");
                results.add(result);
                continue;
            }
            List<DatasetFieldDTO> fields = resp.getData();
            for (DatasetFieldDTO field : fields) {
                if (field.getFieldType() != FieldType.MEASURE) {
                    continue;
                }
                String metricName = field.getAlias() != null && !field.getAlias().isBlank()
                        ? field.getAlias() : field.getOriginName();
                String code = toCode(metricName);
                if (metricRepository.findByName(workspaceId, metricName) != null
                        || metricRepository.findByCode(workspaceId, code) != null) {
                    result.getSkippedDuplicates().add(metricName);
                    continue;
                }
                try {
                    MetricCmd cmd = new MetricCmd()
                            .setWorkspaceId(workspaceId)
                            .setName(metricName)
                            .setCode(code)
                            .setBusinessName(metricName)
                            .setDescription("从数据集 " + datasetId + " 一键同步生成")
                            .setFolder("自动同步")
                            .setFormat(MetricFormat.NUMBER)
                            .setType(MetricType.ATOMIC)
                            .setMeasureKind(MeasureKind.SUM)
                            .setExpression("SUM([" + field.getOriginName() + "])")
                            .setPrimaryDatasetId(datasetId)
                            .setBoundDatasetIds(Collections.emptyList())
                            .setCreatedBy(createdBy);
                    MetricDTO dto = create(cmd);
                    // 自动绑定主数据集字段
                    MetricBindingCmd bindingCmd = new MetricBindingCmd()
                            .setMetricId(dto.getId())
                            .setDatasetId(datasetId)
                            .setFieldId(field.getId())
                            .setPrimary(true);
                    addBinding(bindingCmd);
                    result.getCreated().add(dto);
                } catch (Exception e) {
                    log.warn("同步指标失败 datasetId={}, field={}", datasetId, field.getOriginName(), e);
                    result.getErrors().add(metricName + ": " + e.getMessage());
                }
            }
            results.add(result);
        }
        return results;
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
                ? binding.getDslOverride() : metric.getExpression();
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

    private void saveBindings(String metricId, MetricCmd cmd) {
        metricBindingRepository.deleteByMetric(metricId);
        if (cmd.getPrimaryDatasetId() == null || cmd.getPrimaryDatasetId().isBlank()) {
            return;
        }
        // 主数据集绑定
        MetricBinding primary = new MetricBinding()
                .setMetricId(metricId)
                .setDatasetId(cmd.getPrimaryDatasetId())
                .setFieldId(cmd.getPrimaryFieldId())
                .setPrimary(true)
                .setCreatedAt(OffsetDateTime.now())
                .setUpdatedAt(OffsetDateTime.now());
        metricBindingRepository.save(primary);
        // 辅助数据集绑定：目前仅记录数据集关系（字段需后续通过 addBinding 补充）
        if (!CollectionUtils.isEmpty(cmd.getBoundDatasetIds())) {
            for (String datasetId : cmd.getBoundDatasetIds()) {
                if (datasetId.equals(cmd.getPrimaryDatasetId())) {
                    continue;
                }
                MetricBinding binding = new MetricBinding()
                        .setMetricId(metricId)
                        .setDatasetId(datasetId)
                        .setPrimary(false)
                        .setCreatedAt(OffsetDateTime.now())
                        .setUpdatedAt(OffsetDateTime.now());
                metricBindingRepository.save(binding);
            }
        }
    }

    private void saveDimensionBindings(String metricId, MetricCmd cmd) {
        metricDimensionBindingRepository.deleteByMetric(metricId);
        if (CollectionUtils.isEmpty(cmd.getDimensions())) {
            return;
        }
        List<MetricDimensionBinding> bindings = cmd.getDimensions().stream()
                .distinct()
                .map(name -> new MetricDimensionBinding()
                        .setMetricId(metricId)
                        .setDimensionName(name))
                .toList();
        metricDimensionBindingRepository.saveBatch(metricId, bindings);
    }

    private MetricDTO enrichDTO(MetricDTO dto, String metricId) {
        if (dto == null) {
            return null;
        }
        List<MetricBinding> bindings = metricBindingRepository.listByMetric(metricId);
        List<String> boundDatasetIds = bindings.stream()
                .filter(b -> !b.isPrimary())
                .map(MetricBinding::getDatasetId)
                .distinct()
                .toList();
        List<MetricDimensionBinding> dimBindings = metricDimensionBindingRepository.listByMetric(metricId);
        dto.setBoundDatasetIds(boundDatasetIds);
        dto.setDimensions(dimBindings.stream().map(MetricDimensionBinding::getDimensionName).toList());
        dto.setPrimaryDatasetId(dto.getPrimaryDatasetId());
        return dto;
    }

    private MetricDTO toDTO(Metric metric) {
        return new MetricDTO()
                .setId(metric.getId())
                .setWorkspaceId(metric.getWorkspaceId())
                .setName(metric.getName())
                .setCode(metric.getCode())
                .setBusinessName(metric.getBusinessName())
                .setDescription(metric.getDescription())
                .setFolder(metric.getFolder())
                .setFormat(metric.getFormat())
                .setType(metric.getType())
                .setMeasureKind(metric.getMeasureKind())
                .setExpression(metric.getExpression())
                .setDsl(metric.getDsl())
                .setCaliber(metric.getCaliber())
                .setPrimaryDatasetId(metric.getPrimaryDatasetId())
                .setStatus(metric.getStatus())
                .setVersion(metric.getVersion())
                .setOwnerId(metric.getOwnerId())
                .setCreatedBy(metric.getCreatedBy())
                .setUpdatedBy(metric.getUpdatedBy())
                .setCreateTime(metric.getCreatedAt())
                .setUpdateTime(metric.getUpdatedAt());
    }

    private String toCode(String name) {
        if (name == null) {
            return "";
        }
        String code = name.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (code.isEmpty()) {
            code = "metric_" + System.currentTimeMillis();
        }
        return code;
    }
}
