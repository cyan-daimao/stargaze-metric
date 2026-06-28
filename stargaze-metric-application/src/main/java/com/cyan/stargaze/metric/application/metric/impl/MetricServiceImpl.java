package com.cyan.stargaze.metric.application.metric.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.Page;
import com.cyan.arch.common.api.Response;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.dataset.client.dto.DatasetFieldDTO;
import com.cyan.stargaze.dataset.client.dto.DatasetListItemDTO;
import com.cyan.stargaze.dataset.enums.FieldType;
import com.cyan.stargaze.query.client.QueryClient;
import com.cyan.stargaze.query.client.dto.QueryPreviewRequest;
import com.cyan.stargaze.query.client.dto.QueryResult;
import com.cyan.stargaze.metric.application.MetricAppConvert;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.bo.MetricBO;
import com.cyan.stargaze.metric.application.metric.bo.SyncDatasetItemBO;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricDimensionRef;
import com.cyan.stargaze.metric.client.dto.AggregationDTO;
import com.cyan.stargaze.metric.client.dto.BindableSourceDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.DimensionDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncRequestDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncResultDTO;
import com.cyan.stargaze.metric.client.dto.PreviewRequestDTO;
import com.cyan.stargaze.metric.client.dto.PreviewResponseDTO;
import com.cyan.stargaze.metric.client.dto.ResolveBatchRequestDTO;
import com.cyan.stargaze.metric.client.dto.ResolvedFieldDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionBindingRepository;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricDimensionBinding;
import com.cyan.stargaze.metric.domain.metric.MetricVersion;
import com.cyan.stargaze.metric.domain.metric.repository.MetricDimensionBindingRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricVersionRepository;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 指标应用服务实现(语义资产重构后)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MetricRepository metricRepository;
    private final MetricDimensionBindingRepository metricDimensionBindingRepository;
    private final MetricVersionRepository metricVersionRepository;
    private final DimensionRepository dimensionRepository;
    private final DimensionBindingRepository dimensionBindingRepository;
    private final MetricAppConvert appConvert;
    private final DatasetClient datasetClient;
    private final QueryClient queryClient;

    /** 数据集物理表中需要排除的系统字段(不应同步为指标或维度) */
    private static final Set<String> SYSTEM_FIELD_NAMES = Set.of(
            "id", "deleted_at", "created_at", "updated_at", "created_by", "updated_by", "dt"
    );

    /** 判断是否为系统字段 */
    private static boolean isSystemField(String fieldName) {
        return fieldName != null && SYSTEM_FIELD_NAMES.contains(fieldName.toLowerCase());
    }

    /**
     * 解析指标绑定的维度字段码列表(用于预览 SQL 的 GROUP BY)。
     */
    private List<String> resolveDimensionFields(String metricId) {
        return metricDimensionBindingRepository.listByMetric(metricId).stream()
                .filter(b -> StringUtils.hasText(b.getDimensionId()))
                .map(b -> {
                    Dimension dim = dimensionRepository.findById(b.getDimensionId());
                    return dim != null ? dim.extractFieldCode() : null;
                })
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public MetricBO create(MetricCmd cmd) {
        Metric metric = appConvert.toMetric(cmd);
        metric.setCreatedBy(cmd.getCreatedBy());
        metric.setUpdatedBy(cmd.getUpdatedBy());
        metric = metric.save(metricRepository);
        saveDimensionBindings(metric, cmd.getRelatedDimensions());
        return buildMetricBO(metric);
    }

    @Override
    @Transactional
    public MetricBO update(String metricCode, MetricCmd cmd) {
        Metric existing = loadMetric(metricCode);
        Metric metric = appConvert.toMetric(cmd);
        metric.setId(existing.getId());
        metric.setStatus(existing.getStatus());
        metric.setCreatedBy(existing.getCreatedBy());
        metric.setCreatedAt(existing.getCreatedAt());
        metric.setUpdatedBy(cmd.getUpdatedBy());
        metric = metric.update(metricRepository);
        saveDimensionBindings(metric, cmd.getRelatedDimensions());
        return buildMetricBO(metric);
    }

    @Override
    public MetricBO findByCode(String metricCode) {
        Metric metric = loadMetric(metricCode);
        return buildDetailBO(metric);
    }

    @Override
    public Page<MetricBO> list(Integer page, Integer size, String keyword, String status, String folder) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : size;
        MetricStatus metricStatus = MetricStatus.fromCode(status);
        Page<Metric> result = metricRepository.page(p, s, keyword, metricStatus, folder);
        List<MetricBO> records = result.getData().stream()
                .map(this::buildMetricBO)
                .toList();
        return new Page<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    @Transactional
    public void delete(String metricCode) {
        Metric metric = loadMetric(metricCode);
        metric.delete(metricRepository);
        metricDimensionBindingRepository.deleteByMetric(metric.getId());
    }

    @Override
    @Transactional
    public MetricBO publish(String metricCode) {
        Metric metric = loadMetric(metricCode);
        metric = metric.publish(metricRepository);
        saveVersion(metric, "发布");
        log.info("指标发布 metricCode={}, version={}", metricCode, metric.getMetricCode());
        return buildMetricBO(metric);
    }

    @Override
    @Transactional
    public MetricBO offline(String metricCode) {
        Metric metric = loadMetric(metricCode);
        metric = metric.offline(metricRepository);
        return buildMetricBO(metric);
    }

    @Override
    public PreviewResponseDTO preview(String metricCode, PreviewRequestDTO request) {
        Metric metric = loadMetric(metricCode);
        Assert.isTrue(metric.isAvailable(), new SilentException("指标当前状态不可预览"));
        long start = System.currentTimeMillis();

        List<String> dimFields = resolveDimensionFields(metric.getId());

        PreviewResponseDTO response = new PreviewResponseDTO()
                .setMetricCode(metric.getMetricCode())
                .setMetricName(metric.getName());

        if (metric.getSourceType() == MetricSourceType.HTTP_API) {
            response.setPlanType("ApiLookupPlan")
                    .setApiLookupPlan(metric.apiLookupPlan())
                    .setValue(0.0)
                    .setFormattedValue(metric.formatValue(0.0));
        } else {
            String sql = metric.previewSql(request == null ? null : request.getBizDate(), dimFields);
            response.setPlanType("SqlPlan").setSql(sql);

            // 通过 query 网关执行预览 SQL
            if (metric.getSourceType() == MetricSourceType.DATASET) {
                String fieldCode = metric.aggregateFieldCode();
                String aggFunc = metric.aggregateFunction();
                log.info("预览执行 metricCode={}, datasetId={}, fieldCode={}, aggFunc={}, dimFields={}",
                        metricCode, metric.getSourceCode(), fieldCode, aggFunc, dimFields);
                QueryPreviewRequest qReq = new QueryPreviewRequest()
                        .setMetricCode(metricCode)
                        .setDatasetId(metric.getSourceCode())
                        .setFieldCode(StringUtils.hasText(fieldCode) ? fieldCode : "value")
                        .setAggFunction(StringUtils.hasText(aggFunc) ? aggFunc : "sum")
                        .setDimensionFields(dimFields)
                        .setLimit(5);
                Response<QueryResult> qResp = queryClient.preview(qReq);
                Assert.isTrue(qResp != null && qResp.getCode() == 200 && qResp.getData() != null,
                        new SilentException("指标预览查询失败: " + (qResp == null ? "无响应" : qResp.getMessage())));
                QueryResult data = qResp.getData();
                response.setColumns(data.getColumns())
                        .setRows(data.getRows())
                        .setExecutionTime(data.getCostMs() != null ? data.getCostMs() / 1000.0 : null);
                // 提取首行末尾列(聚合结果)作为单值
                if (data.getColumns() != null && !data.getColumns().isEmpty()
                        && data.getRows() != null && !data.getRows().isEmpty()) {
                    String lastCol = data.getColumns().get(data.getColumns().size() - 1);
                    Object firstVal = data.getRows().get(0).get(lastCol);
                    if (firstVal instanceof Number num) {
                        response.setValue(num.doubleValue());
                    }
                }
            }
            if (response.getValue() == null) {
                response.setValue(0.0);
            }
            response.setFormattedValue(metric.formatValue(response.getValue()));
        }
        response.setElapsedMs(System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public List<BindableSourceDTO> bindableSources(MetricSourceType sourceType) {
        if (sourceType == null) {
            return Collections.emptyList();
        }
        if (sourceType == MetricSourceType.DATASET) {
            return listBindableDatasets();
        }
        return defaultBindableSources(sourceType);
    }

    @Override
    public Page<SyncDatasetItemBO> syncDatasets(Integer page, Integer size, String keyword, String type) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : size;
        Response<Page<DatasetListItemDTO>> resp = datasetClient.page(p, s, keyword, type, null);
        if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
            return new Page<>(Collections.emptyList(), p, s, 0L);
        }
        Page<DatasetListItemDTO> data = resp.getData();
        List<SyncDatasetItemBO> records = data.getData().stream()
                .map(appConvert::toSyncDatasetItemBO)
                .toList();
        return new Page<>(records, data.getCurrent(), data.getSize(), data.getTotal());
    }

    @Override
    @Transactional
    public MetricSyncResultDTO sync(MetricSyncRequestDTO request, String operator) {
        String datasetId = request == null ? null : request.getDatasetId();
        Assert.isTrue(StringUtils.hasText(datasetId), new SilentException("数据集 ID 不能为空"));

        DatasetListItemDTO dataset = findDatasetById(datasetId);
        Assert.notNull(dataset, new SilentException("数据集不存在"));
        String datasetCode = StringUtils.hasText(dataset.getName()) ? dataset.getName() : datasetId;
        String datasetSourceCode = datasetId;

        Response<List<DatasetFieldDTO>> fieldsResp = datasetClient.listFields(datasetId);
        Assert.isTrue(fieldsResp != null && fieldsResp.getCode() == 200 && fieldsResp.getData() != null,
                new SilentException("获取数据集字段失败"));
        List<DatasetFieldDTO> fields = fieldsResp.getData();
        if (CollectionUtils.isEmpty(fields)) {
            return emptySyncResult(datasetId);
        }

        List<com.cyan.stargaze.metric.client.dto.MetricDTO> createdMetrics = new ArrayList<>();
        List<DimensionDTO> createdDimensions = new ArrayList<>();
        List<MetricSyncResultDTO.DuplicateMetricDTO> duplicateMetrics = new ArrayList<>();
        List<MetricSyncResultDTO.DuplicateDimensionDTO> duplicateDimensions = new ArrayList<>();
        int dimensionBindingCount = 0;

        for (DatasetFieldDTO field : fields) {
            if (field == null || field.getFieldType() == null) {
                continue;
            }
            // 排除系统字段(如 deleted_at / created_at / id 等)
            if (isSystemField(field.getFieldName())) {
                continue;
            }
            if (field.getFieldType() == FieldType.MEASURE) {
                MetricSyncResultDTO.DuplicateMetricDTO duplicate = syncMetric(dataset, datasetCode, datasetSourceCode, field, operator, createdMetrics);
                if (duplicate != null) {
                    duplicateMetrics.add(duplicate);
                }
            } else if (field.getFieldType() == FieldType.DIMENSION) {
                MetricSyncResultDTO.DuplicateDimensionDTO duplicate = syncDimension(dataset, datasetId, datasetCode, datasetSourceCode, field, operator, createdDimensions);
                if (duplicate != null) {
                    duplicateDimensions.add(duplicate);
                }
                if (duplicate == null) {
                    dimensionBindingCount++;
                }
            }
        }

        return new MetricSyncResultDTO()
                .setDatasetId(datasetId)
                .setCreated(createdMetrics.size())
                .setDimensionCreated(createdDimensions.size())
                .setDimensionBindingCreated(dimensionBindingCount)
                .setDuplicates(duplicateMetrics)
                .setDimensionDuplicates(duplicateDimensions)
                .setMetrics(createdMetrics)
                .setDimensions(createdDimensions);
    }

    @Override
    public MetricResolveDTO resolve(String metricCode, String datasetCode) {
        Metric metric = loadMetric(metricCode);
        Assert.isTrue(metric.isPublished(), new SilentException("指标未发布,不可解析"));
        return buildResolveDTO(metric, datasetCode);
    }

    @Override
    public List<MetricResolveDTO> resolveBatch(ResolveBatchRequestDTO request) {
        if (request == null || CollectionUtils.isEmpty(request.getMetricCodes())) {
            return Collections.emptyList();
        }
        return request.getMetricCodes().stream()
                .map(code -> resolve(code, request.getDatasetCode()))
                .toList();
    }

    @Override
    public ValidationResultDTO validate(List<String> metricCodes, List<String> dimCodes) {
        if (CollectionUtils.isEmpty(metricCodes)) {
            return new ValidationResultDTO().setValid(true);
        }
        java.util.Set<String> dimSet = CollectionUtils.isEmpty(dimCodes) ? Collections.emptySet() : new java.util.HashSet<>(dimCodes);
        for (String metricCode : metricCodes) {
            Metric metric = metricRepository.findByMetricCode(metricCode);
            if (metric == null) {
                return new ValidationResultDTO().setValid(false)
                        .setReason("指标不存在: " + metricCode);
            }
            if (!metric.isPublished()) {
                return new ValidationResultDTO().setValid(false)
                        .setReason("指标未发布: " + metricCode);
            }
            List<String> related = metricDimensionBindingRepository.listByMetric(metric.getId()).stream()
                    .map(MetricDimensionBinding::getDimensionCode)
                    .filter(StringUtils::hasText)
                    .toList();
            for (String dimCode : dimSet) {
                if (!related.contains(dimCode)) {
                    return new ValidationResultDTO().setValid(false)
                            .setReason("指标 " + metricCode + " 未关联维度 " + dimCode);
                }
                Dimension dimension = dimensionRepository.findByCode(dimCode);
                if (dimension == null) {
                    return new ValidationResultDTO().setValid(false)
                            .setReason("维度不存在: " + dimCode);
                }
                if (!dimension.isPublished()) {
                    return new ValidationResultDTO().setValid(false)
                            .setReason("维度未发布: " + dimCode);
                }
            }
        }
        return new ValidationResultDTO().setValid(true);
    }

    @Override
    public CheckNameResultDTO checkName(String name, String excludeMetricCode) {
        if (!StringUtils.hasText(name)) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("指标名称不能为空");
        }
        Metric existing = metricRepository.findByName(name.trim());
        if (existing != null && (excludeMetricCode == null || !excludeMetricCode.equals(existing.getMetricCode()))) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("该指标名称已存在");
        }
        return new CheckNameResultDTO().setAvailable(true);
    }

    @Override
    public CheckNameResultDTO checkCode(String code, String excludeMetricCode) {
        if (!StringUtils.hasText(code)) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("指标标识不能为空");
        }
        Metric existing = metricRepository.findByCode(code.trim());
        if (existing != null && (excludeMetricCode == null || !excludeMetricCode.equals(existing.getMetricCode()))) {
            return new CheckNameResultDTO().setAvailable(false).setMessage("该指标标识已存在");
        }
        return new CheckNameResultDTO().setAvailable(true);
    }

    @Override
    public List<String> listFolders() {
        return metricRepository.listDistinctFolders();
    }

    @Override
    public List<MetricBO> listPublished() {
        return metricRepository.list(MetricStatus.PUBLISHED).stream()
                .map(this::buildMetricBO)
                .toList();
    }

    @Override
    public Boolean isPublished(String metricCode) {
        Metric metric = metricRepository.findByMetricCode(metricCode);
        return metric != null && metric.isPublished();
    }

    private Metric loadMetric(String metricCode) {
        Metric metric = metricRepository.findByMetricCode(metricCode);
        Assert.notNull(metric, new SilentException("指标不存在"));
        return metric;
    }

    private MetricBO buildMetricBO(Metric metric) {
        if (metric == null) {
            return null;
        }
        List<String> dims = metricDimensionBindingRepository.listByMetric(metric.getId()).stream()
                .map(b -> StringUtils.hasText(b.getDimensionName()) ? b.getDimensionName() : b.getDimensionCode())
                .filter(StringUtils::hasText)
                .toList();
        return new MetricBO()
                .setMetric(metric)
                .setRelatedDimensions(dims)
                .setSourceTypeLabel(metric.sourceTypeLabel())
                .setLogicSummary(metric.logicSummary());
    }

    private MetricBO buildDetailBO(Metric metric) {
        MetricBO bo = buildMetricBO(metric);
        if (bo == null) {
            return null;
        }
        if (metric.getSourceType() == MetricSourceType.HTTP_API) {
            bo.setApiLookupPlan(JSON.toJSONString(metric.apiLookupPlan()));
        } else {
            bo.setSqlPreview(metric.previewSql("latest", resolveDimensionFields(metric.getId())));
        }
        return bo;
    }

    private void saveDimensionBindings(Metric metric, List<MetricDimensionRef> refs) {
        String metricId = metric.getId();
        if (CollectionUtils.isEmpty(refs)) {
            metricDimensionBindingRepository.deleteByMetric(metricId);
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        List<MetricDimensionBinding> bindings = refs.stream()
                .map(ref -> new MetricDimensionBinding()
                        .setMetricId(metricId)
                        .setMetricCode(metric.getMetricCode())
                        .setDimensionId(ref.getDimensionId())
                        .setDimensionCode(ref.getDimensionCode())
                        .setDimensionName(ref.getDimensionName())
                        .setSourceType(ref.getSourceType())
                        .setSourceCode(ref.getSourceCode())
                        .setCreatedAt(now)
                        .setUpdatedAt(now))
                .toList();
        metricDimensionBindingRepository.saveBatch(metricId, bindings);
    }

    private void saveVersion(Metric metric, String changeLog) {
        int nextVersion = metricVersionRepository.listByMetric(metric.getId()).size() + 1;
        MetricVersion version = new MetricVersion()
                .setMetricId(metric.getId())
                .setMetricCode(metric.getMetricCode())
                .setVersion(nextVersion)
                .setDsl(metric.getDsl())
                .setSourceSnapshot(metric.getSourceSnapshot())
                .setChangeLog(changeLog)
                .setCreatedBy(metric.getUpdatedBy())
                .setCreatedAt(OffsetDateTime.now())
                .setUpdatedAt(OffsetDateTime.now());
        metricVersionRepository.save(version);
    }

    private List<BindableSourceDTO> listBindableDatasets() {
        Response<Page<DatasetListItemDTO>> resp = datasetClient.page(1, 100, null, null, null);
        if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
            return Collections.emptyList();
        }
        return resp.getData().getData().stream()
                .map(item -> new BindableSourceDTO()
                        .setSourceType(MetricSourceType.DATASET)
                        .setSourceCode(item.getId())
                        .setSourceName(StringUtils.hasText(item.getDisplayName()) ? item.getDisplayName() : item.getName())
                        .setExtra(datasetSourceExtra(item)))
                .collect(Collectors.toList());
    }

    private Map<String, Object> datasetSourceExtra(DatasetListItemDTO item) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("status", item.getStatus());
        extra.put("fieldCount", item.getFieldCount());
        extra.put("datasetName", item.getName());
        return extra;
    }

    private List<BindableSourceDTO> defaultBindableSources(MetricSourceType sourceType) {
        return switch (sourceType) {
            case PORTRAIT_FEATURE -> List.of(
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("FEAT_30D_CONSUME_AMOUNT")
                            .setSourceName("用户近30天消费金额"),
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("FEAT_7D_VISIT_COUNT")
                            .setSourceName("用户近7天访问次数"));
            case PORTRAIT_TAG -> List.of(
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("TAG_USER_LEVEL")
                            .setSourceName("用户价值分层"),
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("TAG_LIFE_CYCLE")
                            .setSourceName("生命周期阶段"));
            case PORTRAIT_CROWD -> List.of(
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("CROWD_HIGH_VALUE_USER")
                            .setSourceName("高价值用户人群"),
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("CROWD_CHURN_RISK")
                            .setSourceName("潜在流失人群"));
            case REALTIME_TABLE -> List.of(
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("RT_EVENT_CLICK_1MIN")
                            .setSourceName("实时点击事件分钟表"),
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("RT_ORDER_PAY_1MIN")
                            .setSourceName("实时支付事件分钟表"));
            case HTTP_API -> List.of(
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("API_RISK_SCORE")
                            .setSourceName("实时风控分"),
                    new BindableSourceDTO().setSourceType(sourceType)
                            .setSourceCode("API_USER_PROFILE")
                            .setSourceName("用户画像服务"));
            default -> Collections.emptyList();
        };
    }

    private MetricResolveDTO buildResolveDTO(Metric metric, String datasetCode) {
        MetricResolveDTO dto = new MetricResolveDTO()
                .setMetricCode(metric.getMetricCode())
                .setMetricId(metric.getMetricCode())
                .setMetricName(metric.getName())
                .setStatus(metric.getStatus())
                .setSourceType(metric.getSourceType())
                .setSourceCode(metric.getSourceCode())
                .setQueryMode(metric.getQueryMode())
                .setFreshness(metric.getFreshness())
                .setRequiredFields(new ArrayList<>());
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("type", metric.getSourceType() == null ? null : metric.getSourceType().getCode());
        source.put("sourceCode", metric.getSourceCode());
        source.put("sourceName", metric.getSourceName());
        if (StringUtils.hasText(metric.getSourceSnapshot())) {
            try {
                source.putAll(JSON.parseObject(metric.getSourceSnapshot()));
            } catch (Exception ignored) {
            }
        }
        if (metric.getSourceType() == MetricSourceType.DATASET && StringUtils.hasText(datasetCode)) {
            source.put("datasetCode", datasetCode);
        }
        dto.setSource(source);
        JSONObject dsl = metric.dslObject();
        if (dsl != null && !dsl.isEmpty()) {
            dto.setDsl(new LinkedHashMap<>(dsl));
            dto.getRequiredFields().addAll(metric.requiredFields());
            buildResolveFieldsAndAgg(dto, metric);
        }
        return dto;
    }

    private void buildResolveFieldsAndAgg(MetricResolveDTO dto, Metric metric) {
        String fieldCode = metric.aggregateFieldCode();
        String func = metric.aggregateFunction();
        if (StringUtils.hasText(fieldCode)) {
            dto.getFields().add(new ResolvedFieldDTO()
                    .setFieldId(fieldCode)
                    .setOriginName(fieldCode)
                    .setAlias(fieldCode));
        }
        if (StringUtils.hasText(func)) {
            dto.setAgg(new AggregationDTO().setCode(func));
        }
    }

    // ==================== 一键同步辅助方法 ====================

    private DatasetListItemDTO findDatasetById(String datasetId) {
        Response<Page<DatasetListItemDTO>> resp = datasetClient.page(1, 1000, null, null, null);
        if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
            return null;
        }
        return resp.getData().getData().stream()
                .filter(item -> datasetId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    private MetricSyncResultDTO emptySyncResult(String datasetId) {
        return new MetricSyncResultDTO()
                .setDatasetId(datasetId)
                .setCreated(0)
                .setDimensionCreated(0)
                .setDimensionBindingCreated(0)
                .setDuplicates(Collections.emptyList())
                .setDimensionDuplicates(Collections.emptyList())
                .setMetrics(Collections.emptyList())
                .setDimensions(Collections.emptyList());
    }

    private MetricSyncResultDTO.DuplicateMetricDTO syncMetric(DatasetListItemDTO dataset,
                                                              String datasetCode,
                                                              String datasetSourceCode,
                                                              DatasetFieldDTO field,
                                                              String operator,
                                                              List<com.cyan.stargaze.metric.client.dto.MetricDTO> createdMetrics) {
        String metricCode = field.getFieldName();
        String metricName = field.getDisplayName();
        String uniqueCode = Metric.generateCode(datasetCode, field.getFieldName());

        Metric existingName = metricRepository.findByName(metricName);
        if (existingName != null) {
            return new MetricSyncResultDTO.DuplicateMetricDTO()
                    .setNewName(metricName)
                    .setExistingName(existingName.getName())
                    .setExistingId(existingName.getId());
        }
        Metric existingMetricCode = metricRepository.findByMetricCode(metricCode);
        if (existingMetricCode != null) {
            return new MetricSyncResultDTO.DuplicateMetricDTO()
                    .setNewName(metricName)
                    .setExistingName(existingMetricCode.getName())
                    .setExistingId(existingMetricCode.getId());
        }
        Metric existingCode = metricRepository.findByCode(uniqueCode);
        if (existingCode != null) {
            return new MetricSyncResultDTO.DuplicateMetricDTO()
                    .setNewName(metricName)
                    .setExistingName(existingCode.getName())
                    .setExistingId(existingCode.getId());
        }

        String datasetDisplayName = StringUtils.hasText(dataset.getDisplayName()) ? dataset.getDisplayName() : dataset.getName();
        Metric metric = new Metric()
                .setMetricCode(metricCode)
                .setName(metricName)
                .setCode(uniqueCode)
                .setDescription("从数据集 " + datasetDisplayName + " 同步生成")
                .setSourceType(MetricSourceType.DATASET)
                .setSourceCode(datasetSourceCode)
                .setSourceName(datasetDisplayName)
                .setQueryMode(QueryMode.OLAP)
                .setDslKind(MetricDslKind.ATOMIC)
                .setDsl(Metric.buildAtomicDsl(datasetCode, field))
                .setFormat(Metric.inferFormat(field.getDataType()))
                .setCreatedBy(operator)
                .setUpdatedBy(operator);
        metric = metric.save(metricRepository);
        createdMetrics.add(appConvert.toMetricDTO(metric));
        return null;
    }

    private MetricSyncResultDTO.DuplicateDimensionDTO syncDimension(DatasetListItemDTO dataset,
                                                                    String datasetId,
                                                                    String datasetCode,
                                                                    String datasetSourceCode,
                                                                    DatasetFieldDTO field,
                                                                    String operator,
                                                                    List<DimensionDTO> createdDimensions) {
        String dimCode = Dimension.generateCode(datasetCode, field.getFieldName());
        String dimName = field.getDisplayName();

        Dimension existingName = dimensionRepository.findByName(dimName);
        if (existingName != null) {
            return new MetricSyncResultDTO.DuplicateDimensionDTO()
                    .setNewName(dimName)
                    .setExistingName(existingName.getName())
                    .setExistingId(existingName.getId());
        }

        String datasetDisplayName = StringUtils.hasText(dataset.getDisplayName()) ? dataset.getDisplayName() : dataset.getName();
        Dimension dimension = new Dimension()
                .setName(dimName)
                .setCode(dimCode)
                .setSemanticType(Dimension.inferSemanticType(field.getSemanticType()))
                .setSourceType(MetricSourceType.DATASET)
                .setSourceCode(datasetSourceCode)
                .setSourceName(datasetDisplayName)
                .setQueryMode(QueryMode.OLAP)
                .setDslKind(MetricDslKind.FIELD)
                .setDsl(Dimension.buildFieldDsl(datasetCode, field))
                .setCreatedBy(operator)
                .setUpdatedBy(operator);
        dimension = dimension.save(dimensionRepository);

        DimensionBinding binding = new DimensionBinding()
                .setDimensionId(dimension.getId())
                .setDatasetId(datasetId)
                .setFieldId(field.getId())
                .setCreatedAt(OffsetDateTime.now())
                .setUpdatedAt(OffsetDateTime.now());
        dimensionBindingRepository.save(binding);

        createdDimensions.add(appConvert.toClientDimensionDTO(dimension));
        return null;
    }
}
