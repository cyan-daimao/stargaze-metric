package com.cyan.stargaze.metric.application.metric.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.Response;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.dataset.client.dto.DatasetFieldDTO;
import com.cyan.stargaze.dataset.client.dto.ResolveFieldDTO;
import com.cyan.stargaze.dataset.enums.FieldType;
import com.cyan.stargaze.metric.application.MetricAppConvert;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.CheckDimensionRequestDTO;
import com.cyan.stargaze.metric.client.dto.CheckDimensionResultDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.DatasetListItemDTO;
import com.cyan.stargaze.metric.client.dto.DimensionDTO;
import com.cyan.stargaze.metric.client.dto.FieldRefDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricDimensionRefDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncRequestDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncResultDTO;
import com.cyan.stargaze.metric.client.dto.PageDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionBindingRepository;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
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
import com.cyan.stargaze.metric.enums.SemanticType;
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
    private final DimensionRepository dimensionRepository;
    private final DimensionBindingRepository dimensionBindingRepository;
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
    public PageDTO<MetricDTO> list(Integer page, Integer size, String keyword, String status, String folder) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : size;
        MetricStatus metricStatus = MetricStatus.fromCode(status);
        IPage<Metric> result = metricRepository.page(new Page<>(p, s), keyword, metricStatus, folder);
        List<MetricDTO> records = result.getRecords().stream()
                .map(m -> enrichDTO(toDTO(m), m.getId()))
                .toList();
        return new PageDTO<MetricDTO>()
                .setData(records)
                .setTotal(result.getTotal())
                .setPage(result.getCurrent())
                .setSize(result.getSize());
    }

    @Override
    public List<MetricDTO> list(boolean publishedOnly) {
        MetricStatus status = publishedOnly ? MetricStatus.PUBLISHED : null;
        return metricRepository.list(status).stream()
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
        MetricVersion version = new MetricVersion()
                .setMetricId(metric.getId())
                .setVersion(metric.getVersion())
                .setDsl(metric.getDsl())
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
    public MetricBinding addBinding(MetricBindingCmd cmd) {
        MetricBinding binding = convert.toMetricBinding(cmd);
        binding.validate();
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
    public CheckNameResultDTO checkName(String name, String excludeId) {
        if (name == null || name.isBlank()) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("指标名称不能为空");
        }
        Metric existing = metricRepository.findByName(name.trim());
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            return new CheckNameResultDTO()
                    .setAvailable(false)
                    .setMessage("该指标名称已存在，建议修改以避免混淆");
        }
        return new CheckNameResultDTO().setAvailable(true);
    }

    @Override
    public CheckNameResultDTO checkCode(String code, String excludeId) {
        if (code == null || code.isBlank()) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("指标标识不能为空");
        }
        Metric existing = metricRepository.findByCode(code.trim());
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            return new CheckNameResultDTO()
                    .setAvailable(false)
                    .setMessage("该指标标识已存在，建议修改以避免混淆");
        }
        return new CheckNameResultDTO().setAvailable(true);
    }

    @Override
    public CheckDimensionResultDTO checkDimensions(CheckDimensionRequestDTO request) {
        List<String> datasetIds = new ArrayList<>();
        if (request.getPrimaryDatasetId() != null) {
            datasetIds.add(request.getPrimaryDatasetId());
        }
        if (!CollectionUtils.isEmpty(request.getSecondaryDatasetIds())) {
            datasetIds.addAll(request.getSecondaryDatasetIds());
        }
        if (datasetIds.isEmpty()) {
            return new CheckDimensionResultDTO().setDuplicates(Collections.emptyList());
        }

        Map<String, Set<String>> nameToDatasets = new HashMap<>();
        Map<String, String> datasetNameMap = new HashMap<>();
        for (String datasetId : datasetIds) {
            var resp = datasetClient.listFields(datasetId);
            if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
                log.warn("维度查重跳过不可用数据集 datasetId={}", datasetId);
                continue;
            }
            String datasetName = resp.getData().isEmpty() ? datasetId : datasetId;
            datasetNameMap.put(datasetId, datasetName);
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

        List<CheckDimensionResultDTO.DuplicateDimensionDTO> duplicates = new ArrayList<>();
        nameToDatasets.forEach((dimName, ids) -> {
            if (ids.size() > 1) {
                List<CheckDimensionResultDTO.DuplicateDimensionDTO.DatasetDTO> datasets = ids.stream()
                        .map(id -> new CheckDimensionResultDTO.DuplicateDimensionDTO.DatasetDTO()
                                .setId(id)
                                .setName(datasetNameMap.getOrDefault(id, id)))
                        .toList();
                duplicates.add(new CheckDimensionResultDTO.DuplicateDimensionDTO()
                        .setDimensionName(dimName)
                        .setDatasets(datasets));
            }
        });
        return new CheckDimensionResultDTO().setDuplicates(duplicates);
    }

    @Override
    public PageDTO<DatasetListItemDTO> listSyncDatasets(Integer page, Integer size, String keyword, String type, String datasource) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : size;
        Response<com.cyan.arch.common.api.Page<com.cyan.stargaze.dataset.client.dto.DatasetListItemDTO>> resp =
                datasetClient.page(p, s, keyword, type, null);
        Assert.notNull(resp, new SilentException("数据集服务无响应"));
        Assert.isTrue(resp.getCode() == 200 && resp.getData() != null,
                new SilentException("数据集服务返回错误:[" + resp.getCode() + "] " + resp.getMessage()));

        List<com.cyan.stargaze.dataset.client.dto.DatasetListItemDTO> sourceList = resp.getData().getData();
        List<DatasetListItemDTO> list = sourceList.stream()
                .map(item -> new DatasetListItemDTO()
                        .setId(item.getId())
                        .setName(item.getName())
                        .setCode(item.getName())
                        .setType(item.getSourceType())
                        .setDatasource(item.getDatasourceName())
                        .setSchema("")
                        .setFields(item.getFieldCount())
                        .setRows("")
                        .setStatus(item.getStatus())
                        .setMetricCount(item.getMeasureCount())
                        .setDimensionCount(item.getDimensionCount())
                        .setUpdateTime(item.getUpdatedAt() == null ? null : item.getUpdatedAt().toString()))
                .toList();
        return new PageDTO<DatasetListItemDTO>()
                .setData(list)
                .setTotal(resp.getData().getTotal())
                .setPage(resp.getData().getCurrent())
                .setSize(resp.getData().getSize());
    }

    @Override
    @Transactional
    public MetricSyncResultDTO syncFromDataset(MetricSyncRequestDTO request, String createdBy) {
        String datasetId = request.getDatasetId();
        MetricSyncResultDTO result = new MetricSyncResultDTO()
                .setDatasetId(datasetId)
                .setCreated(0)
                .setDimensionCreated(0)
                .setDimensionBindingCreated(0)
                .setDuplicates(new ArrayList<>())
                .setDimensionDuplicates(new ArrayList<>())
                .setMetrics(new ArrayList<>())
                .setDimensions(new ArrayList<>());

        var resp = datasetClient.listFields(datasetId);
        Assert.notNull(resp, new SilentException("数据集服务无响应"));
        Assert.isTrue(resp.getCode() == 200 && resp.getData() != null,
                new SilentException("数据集服务返回错误:[" + resp.getCode() + "] " + resp.getMessage()));

        List<MetricDimensionRefDTO> dimensionRefs = syncDimensions(datasetId, resp.getData(), createdBy, result);
        syncMetrics(request, resp.getData(), createdBy, result, dimensionRefs);
        return result;
    }

    private List<MetricDimensionRefDTO> syncDimensions(String datasetId, List<DatasetFieldDTO> fields, String createdBy, MetricSyncResultDTO result) {
        List<MetricDimensionRefDTO> refs = new ArrayList<>();
        for (DatasetFieldDTO field : fields) {
            if (field.getFieldType() != FieldType.DIMENSION) {
                continue;
            }
            String dimensionName = fieldDisplayName(field);
            if (dimensionName == null || dimensionName.isBlank()) {
                continue;
            }
            Dimension dimension = dimensionRepository.findByName(dimensionName);
            if (dimension == null) {
                try {
                    dimension = new Dimension()
                            .setName(dimensionName)
                            .setCode(toCode(dimensionName))
                            .setBusinessName(dimensionName)
                            .setFolder("自动同步")
                            .setSemanticType(resolveSemanticType(field))
                            .setCreatedBy(createdBy)
                            .save(dimensionRepository);
                    result.setDimensionCreated(result.getDimensionCreated() + 1);
                    result.getDimensions().add(toDimensionDTO(dimension));
                } catch (Exception e) {
                    log.warn("同步维度失败 datasetId={}, field={}", datasetId, field.getOriginName(), e);
                    continue;
                }
            }
            // 收集维度引用，供后续指标绑定使用（无论新建还是已有维度都需纳入）
            refs.add(new MetricDimensionRefDTO()
                    .setDimensionId(dimension.getId())
                    .setDatasetId(datasetId));
            DimensionBinding existingBinding = dimensionBindingRepository.findByDimensionAndDataset(dimension.getId(), datasetId);
            if (existingBinding != null) {
                result.getDimensionDuplicates().add(new MetricSyncResultDTO.DuplicateDimensionDTO()
                        .setNewName(dimensionName)
                        .setExistingName(dimension.getName())
                        .setExistingId(dimension.getId()));
                continue;
            }
            try {
                DimensionBinding binding = new DimensionBinding()
                        .setDimensionId(dimension.getId())
                        .setDatasetId(datasetId)
                        .setFieldId(field.getId())
                        .setCreatedAt(OffsetDateTime.now())
                        .setUpdatedAt(OffsetDateTime.now());
                binding.validate();
                dimensionBindingRepository.save(binding);
                result.setDimensionBindingCreated(result.getDimensionBindingCreated() + 1);
            } catch (Exception e) {
                log.warn("同步维度绑定失败 datasetId={}, dimensionId={}, field={}",
                        datasetId, dimension.getId(), field.getOriginName(), e);
            }
        }
        return refs;
    }

    private void syncMetrics(MetricSyncRequestDTO request, List<DatasetFieldDTO> fields, String createdBy,
                             MetricSyncResultDTO result, List<MetricDimensionRefDTO> dimensionRefs) {
        String datasetId = request.getDatasetId();
        List<String> targetNames = request.getMetricNames();
        for (DatasetFieldDTO field : fields) {
            if (field.getFieldType() != FieldType.MEASURE) {
                continue;
            }
            String metricName = fieldDisplayName(field);
            if (!CollectionUtils.isEmpty(targetNames) && !targetNames.contains(metricName)) {
                continue;
            }
            String code = toCode(metricName);
            String dsl = "SUM([" + field.getOriginName() + "])";
            Metric existingByName = metricRepository.findByName(metricName);
            Metric existingByCode = metricRepository.findByCode(code);
            if (existingByName != null) {
                result.getDuplicates().add(new MetricSyncResultDTO.DuplicateMetricDTO()
                        .setNewName(metricName)
                        .setExistingName(existingByName.getName())
                        .setExistingId(existingByName.getId()));
                continue;
            }
            if (existingByCode != null) {
                result.getDuplicates().add(new MetricSyncResultDTO.DuplicateMetricDTO()
                        .setNewName(metricName)
                        .setExistingName(existingByCode.getName())
                        .setExistingId(existingByCode.getId()));
                continue;
            }
            try {
                MetricCmd cmd = new MetricCmd()
                        .setName(metricName)
                        .setCode(code)
                        .setBusinessName(metricName)
                        .setDescription("从数据集 " + datasetId + " 一键同步生成")
                        .setFolder("自动同步")
                        .setFormat(MetricFormat.NUMBER)
                        .setType(MetricType.ATOMIC)
                        .setAggregation(MeasureKind.SUM)
                        .setDsl(dsl)
                        .setPrimaryDatasetId(datasetId)
                        .setPrimaryFieldId(field.getId())
                        .setSecondaryDatasetIds(Collections.emptyList())
                        .setDimensions(dimensionRefs)
                        .setCreatedBy(createdBy);
                MetricDTO dto = create(cmd);
                result.setCreated(result.getCreated() + 1);
                result.getMetrics().add(dto);
            } catch (Exception e) {
                log.warn("同步指标失败 datasetId={}, field={}", datasetId, field.getOriginName(), e);
            }
        }
    }

    @Override
    public MetricResolveDTO resolve(String metricId, String datasetId) {
        Metric metric = loadMetric(metricId);
        Assert.isTrue(metric.isPublished(), new SilentException("指标未发布,不可解析"));
        MetricBinding binding = metricBindingRepository.findByMetricAndDataset(metricId, datasetId);
        Assert.notNull(binding, new SilentException("指标未绑定该数据集"));
        String dsl = (binding.getDslOverride() != null && !binding.getDslOverride().isBlank())
                ? binding.getDslOverride() : metric.getDsl();
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
        MetricBinding primary = new MetricBinding()
                .setMetricId(metricId)
                .setDatasetId(cmd.getPrimaryDatasetId())
                .setFieldId(cmd.getPrimaryFieldId())
                .setPrimary(true)
                .setCreatedAt(OffsetDateTime.now())
                .setUpdatedAt(OffsetDateTime.now());
        metricBindingRepository.save(primary);
        if (!CollectionUtils.isEmpty(cmd.getSecondaryDatasetIds())) {
            for (String datasetId : cmd.getSecondaryDatasetIds()) {
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
                .map(ref -> new MetricDimensionBinding()
                        .setMetricId(metricId)
                        .setDimensionId(ref.getDimensionId())
                        .setDimensionName(ref.getDimensionId())
                        .setDatasetId(ref.getDatasetId()))
                .toList();
        metricDimensionBindingRepository.saveBatch(metricId, bindings);
    }

    private MetricDTO enrichDTO(MetricDTO dto, String metricId) {
        if (dto == null) {
            return null;
        }
        List<MetricBinding> bindings = metricBindingRepository.listByMetric(metricId);
        List<String> secondaryDatasetIds = bindings.stream()
                .filter(b -> !b.isPrimary())
                .map(MetricBinding::getDatasetId)
                .distinct()
                .toList();
        List<MetricDimensionBinding> dimBindings = metricDimensionBindingRepository.listByMetric(metricId);
        List<MetricDimensionRefDTO> dimensionRefs = dimBindings.stream()
                .map(b -> new MetricDimensionRefDTO()
                        .setDimensionId(b.getDimensionName())
                        .setDatasetId(b.getDatasetId()))
                .toList();
        dto.setSecondaryDatasetIds(secondaryDatasetIds);
        dto.setSecondaryDatasetCount(secondaryDatasetIds.size());
        dto.setDimensionCount(dimBindings.size());
        dto.setDimensions(dimensionRefs);
        dto.setAggregation(dto.getMeasureKind());
        // 主数据集名称由调用方补充（避免循环依赖）
        return dto;
    }

    private MetricDTO toDTO(Metric metric) {
        return new MetricDTO()
                .setId(metric.getId())
                .setName(metric.getName())
                .setCode(metric.getCode())
                .setBusinessName(metric.getBusinessName())
                .setDescription(metric.getDescription())
                .setFolder(metric.getFolder())
                .setFormat(metric.getFormat())
                .setType(metric.getType())
                .setMeasureKind(metric.getMeasureKind())
                .setAggregation(metric.getMeasureKind())
                .setDsl(metric.getDsl())
                .setFilterCondition(metric.getFilterCondition())
                .setPrecision(metric.getPrecision())
                .setPrimaryDatasetId(metric.getPrimaryDatasetId())
                .setStatus(metric.getStatus())
                .setVersion(metric.getVersion())
                .setOwnerId(metric.getOwnerId())
                .setCreatedBy(metric.getCreatedBy())
                .setUpdatedBy(metric.getUpdatedBy())
                .setCreateTime(metric.getCreatedAt())
                .setUpdateTime(metric.getUpdatedAt());
    }

    private DimensionDTO toDimensionDTO(Dimension dimension) {
        return new DimensionDTO()
                .setId(dimension.getId())
                .setName(dimension.getName())
                .setBusinessName(dimension.getBusinessName())
                .setFolder(dimension.getFolder())
                .setSemanticType(dimension.getSemanticType())
                .setDictionaryId(dimension.getDictionaryId())
                .setFormat(dimension.getFormat())
                .setStatus(dimension.getStatus())
                .setCreatedBy(dimension.getCreatedBy())
                .setCreatedAt(dimension.getCreatedAt())
                .setUpdatedAt(dimension.getUpdatedAt());
    }

    private String fieldDisplayName(DatasetFieldDTO field) {
        return field.getAlias() != null && !field.getAlias().isBlank()
                ? field.getAlias() : field.getOriginName();
    }

    private SemanticType resolveSemanticType(DatasetFieldDTO field) {
        String semanticType = field.getSemanticType();
        if (semanticType != null && !semanticType.isBlank()) {
            for (SemanticType type : SemanticType.values()) {
                if (type.getCode().equalsIgnoreCase(semanticType) || type.name().equalsIgnoreCase(semanticType)) {
                    return type;
                }
            }
        }
        if (field.getDataType() != null) {
            String code = field.getDataType().getCode();
            if ("date".equalsIgnoreCase(code) || "datetime".equalsIgnoreCase(code)) {
                return SemanticType.TIME;
            }
        }
        return inferSemanticType(field.getOriginName());
    }

    private SemanticType inferSemanticType(String fieldName) {
        if (fieldName == null) {
            return SemanticType.CATEGORY;
        }
        String lower = fieldName.toLowerCase();
        if (lower.contains("time") || lower.contains("date") || lower.contains("year")
                || lower.contains("month") || lower.contains("day") || lower.contains("hour")
                || lower.contains("minute") || lower.contains("second") || lower.contains("dt")) {
            return SemanticType.TIME;
        }
        if (lower.contains("province") || lower.contains("city") || lower.contains("region")
                || lower.contains("area") || lower.contains("country") || lower.contains("geo")
                || lower.contains("lat") || lower.contains("lng") || lower.contains("latitude")
                || lower.contains("longitude") || lower.contains("location")) {
            return SemanticType.GEO;
        }
        return SemanticType.CATEGORY;
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
