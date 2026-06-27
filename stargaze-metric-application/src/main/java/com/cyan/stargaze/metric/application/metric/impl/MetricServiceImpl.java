package com.cyan.stargaze.metric.application.metric.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.Response;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.dataset.client.dto.DatasetListItemDTO;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.application.MetricAppConvert;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricDimensionRef;
import com.cyan.stargaze.metric.client.dto.BindableSourceDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.PageDTO;
import com.cyan.stargaze.metric.client.dto.PreviewRequestDTO;
import com.cyan.stargaze.metric.client.dto.PreviewResponseDTO;
import com.cyan.stargaze.metric.client.dto.ResolveBatchRequestDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.MetricDimensionBinding;
import com.cyan.stargaze.metric.domain.metric.MetricVersion;
import com.cyan.stargaze.metric.domain.metric.repository.MetricDimensionBindingRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.domain.metric.repository.MetricVersionRepository;
import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private final MetricAppConvert appConvert;
    private final MetricAdapterConvert adapterConvert;
    private final DatasetClient datasetClient;

    @Override
    @Transactional
    public MetricDTO create(MetricCmd cmd) {
        Metric metric = appConvert.toMetric(cmd);
        metric.setCreatedBy(cmd.getCreatedBy());
        metric.setUpdatedBy(cmd.getUpdatedBy());
        metric = metric.save(metricRepository);
        saveDimensionBindings(metric, cmd.getRelatedDimensions());
        return enrichDTO(adapterConvert.toMetricDTO(metric));
    }

    @Override
    @Transactional
    public MetricDTO update(String metricCode, MetricCmd cmd) {
        Metric existing = loadMetric(metricCode);
        Metric metric = appConvert.toMetric(cmd);
        metric.setId(existing.getId());
        metric.setStatus(existing.getStatus());
        metric.setCreatedBy(existing.getCreatedBy());
        metric.setCreatedAt(existing.getCreatedAt());
        metric.setUpdatedBy(cmd.getUpdatedBy());
        metric = metric.update(metricRepository);
        saveDimensionBindings(metric, cmd.getRelatedDimensions());
        return enrichDTO(adapterConvert.toMetricDTO(metric));
    }

    @Override
    public MetricDTO findByCode(String metricCode) {
        Metric metric = loadMetric(metricCode);
        return enrichDetail(adapterConvert.toMetricDTO(metric));
    }

    @Override
    public PageDTO<MetricDTO> list(Integer page, Integer size, String keyword, String status, String folder) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : size;
        MetricStatus metricStatus = MetricStatus.fromCode(status);
        IPage<Metric> result = metricRepository.page(new Page<>(p, s), keyword, metricStatus, folder);
        List<MetricDTO> records = result.getRecords().stream()
                .map(m -> enrichDTO(adapterConvert.toMetricDTO(m)))
                .toList();
        return new PageDTO<MetricDTO>()
                .setData(records)
                .setTotal(result.getTotal())
                .setPage(result.getCurrent())
                .setSize(result.getSize());
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
    public MetricDTO publish(String metricCode) {
        Metric metric = loadMetric(metricCode);
        metric = metric.publish(metricRepository);
        saveVersion(metric, "发布");
        log.info("指标发布 metricCode={}, version={}", metricCode, metric.getMetricCode());
        return enrichDTO(adapterConvert.toMetricDTO(metric));
    }

    @Override
    @Transactional
    public MetricDTO offline(String metricCode) {
        Metric metric = loadMetric(metricCode);
        metric = metric.offline(metricRepository);
        return enrichDTO(adapterConvert.toMetricDTO(metric));
    }

    @Override
    public PreviewResponseDTO preview(String metricCode, PreviewRequestDTO request) {
        Metric metric = loadMetric(metricCode);
        Assert.isTrue(metric.isAvailable(), new SilentException("指标当前状态不可预览"));
        long start = System.currentTimeMillis();
        PreviewResponseDTO response = new PreviewResponseDTO()
                .setMetricCode(metric.getMetricCode())
                .setMetricName(metric.getName())
                .setValue(0.0)
                .setElapsedMs(System.currentTimeMillis() - start);
        if (metric.getSourceType() == MetricSourceType.HTTP_API) {
            response.setPlanType("ApiLookupPlan")
                    .setApiLookupPlan(buildApiLookupPlan(metric))
                    .setFormattedValue(formatValue(0.0, metric.getFormat(), metric.getPrecision()));
        } else {
            response.setPlanType("SqlPlan")
                    .setSql(buildPreviewSql(metric, request))
                    .setFormattedValue(formatValue(0.0, metric.getFormat(), metric.getPrecision()));
        }
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
        if (CollectionUtils.isEmpty(metricCodes) || CollectionUtils.isEmpty(dimCodes)) {
            return new ValidationResultDTO().setValid(true);
        }
        Set<String> dimSet = new HashSet<>(dimCodes);
        for (String metricCode : metricCodes) {
            Metric metric = metricRepository.findByMetricCode(metricCode);
            if (metric == null) {
                return new ValidationResultDTO().setValid(false)
                        .setReason("指标不存在: " + metricCode);
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

    private Metric loadMetric(String metricCode) {
        Metric metric = metricRepository.findByMetricCode(metricCode);
        Assert.notNull(metric, new SilentException("指标不存在"));
        return metric;
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
        Response<com.cyan.arch.common.api.Page<DatasetListItemDTO>> resp =
                datasetClient.page(1, 100, null, null, null);
        if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
            return Collections.emptyList();
        }
        return resp.getData().getData().stream()
                .map(item -> new BindableSourceDTO()
                        .setSourceType(MetricSourceType.DATASET)
                        .setSourceCode(item.getName())
                        .setSourceName(item.getName())
                        .setExtra(Map.<String, Object>of("status", String.valueOf(item.getStatus()),
                                "fieldCount", item.getFieldCount())))
                .collect(Collectors.toList());
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

    private MetricDTO enrichDTO(MetricDTO dto) {
        if (dto == null) {
            return null;
        }
        List<MetricDimensionBinding> bindings = metricDimensionBindingRepository.listByMetric(dto.getId());
        dto.setRelatedDimensions(bindings.stream()
                .map(b -> StringUtils.hasText(b.getDimensionName()) ? b.getDimensionName() : b.getDimensionCode())
                .filter(StringUtils::hasText)
                .toList());
        dto.setSourceTypeLabel(sourceTypeLabel(dto.getSourceType()));
        dto.setLogicSummary(logicSummary(dto));
        return dto;
    }

    private MetricDTO enrichDetail(MetricDTO dto) {
        enrichDTO(dto);
        if (dto == null) {
            return null;
        }
        Metric metric = metricRepository.findById(dto.getId());
        if (metric == null) {
            return dto;
        }
        if (metric.getSourceType() == MetricSourceType.HTTP_API) {
            dto.setApiLookupPlan(JSON.toJSONString(buildApiLookupPlan(metric)));
        } else {
            dto.setSqlPreview(buildPreviewSql(metric, new PreviewRequestDTO().setBizDate("latest")));
        }
        return dto;
    }

    private String sourceTypeLabel(MetricSourceType sourceType) {
        if (sourceType == null) {
            return "";
        }
        return switch (sourceType) {
            case DATASET -> "数据集";
            case PORTRAIT_FEATURE, PORTRAIT_TAG, PORTRAIT_CROWD -> "画像平台";
            case REALTIME_TABLE -> "实时表";
            case HTTP_API -> "HTTP API";
        };
    }

    private String logicSummary(MetricDTO dto) {
        if (dto.getDslKind() == MetricDslKind.API_METRIC) {
            return "IMPORT " + (dto.getSourceType() == null ? "" : dto.getSourceType().getCode());
        }
        if (!StringUtils.hasText(dto.getDsl())) {
            return "";
        }
        try {
            JSONObject dsl = JSON.parseObject(dto.getDsl());
            JSONObject expr = dsl.getJSONObject("expr");
            if (expr == null) {
                return dto.getDslKind() == null ? "" : dto.getDslKind().getCode();
            }
            String op = expr.getString("op");
            if ("featureValue".equals(op)) {
                return "IMPORT portraitFeature";
            }
            String func = expr.getString("func");
            String fieldCode = expr.getString("fieldCode");
            if (func != null && fieldCode != null) {
                return func.toUpperCase() + "(" + fieldCode + ")";
            }
        } catch (Exception e) {
            log.warn("解析 DSL 摘要失败 metricCode={}", dto.getMetricCode(), e);
        }
        return dto.getDslKind() == null ? "" : dto.getDslKind().getCode();
    }

    private String buildPreviewSql(Metric metric, PreviewRequestDTO request) {
        String bizDate = request == null || !StringUtils.hasText(request.getBizDate()) ? "latest" : request.getBizDate();
        String metricCode = metric.getMetricCode();
        StringBuilder sql = new StringBuilder();
        if (metric.getSourceType() == MetricSourceType.PORTRAIT_FEATURE) {
            JSONObject dsl = parseDsl(metric.getDsl());
            String featureCode = extractString(dsl, "source", "featureCode");
            if (!StringUtils.hasText(featureCode)) {
                featureCode = metric.getSourceCode();
            }
            sql.append("SELECT SUM(CAST(feature_value_decimal AS DECIMAL(18,2))) AS ").append(metricCode)
                    .append(" FROM portrait_feature_value_store")
                    .append(" WHERE entity_type = 'user'")
                    .append(" AND feature_code = '").append(featureCode).append("'")
                    .append(" AND dt = ${bizDate}");
        } else if (metric.getSourceType() == MetricSourceType.REALTIME_TABLE) {
            JSONObject dsl = parseDsl(metric.getDsl());
            String func = extractString(dsl, "expr", "func");
            String fieldCode = extractString(dsl, "expr", "fieldCode");
            if (!StringUtils.hasText(func)) func = "SUM";
            if (!StringUtils.hasText(fieldCode)) fieldCode = "value";
            sql.append("SELECT ").append(func.toUpperCase())
                    .append("(").append(fieldCode).append(") AS ").append(metricCode)
                    .append(" FROM ").append(metric.getSourceCode())
                    .append(" WHERE dt = ${bizDate}");
        } else {
            // dataset / default
            JSONObject dsl = parseDsl(metric.getDsl());
            String func = extractString(dsl, "expr", "func");
            String fieldCode = extractString(dsl, "expr", "fieldCode");
            if (!StringUtils.hasText(func)) func = "SUM";
            if (!StringUtils.hasText(fieldCode)) fieldCode = "value";
            sql.append("SELECT ").append(func.toUpperCase())
                    .append("(CAST(").append(fieldCode).append(" AS DECIMAL(18,2))) AS ").append(metricCode)
                    .append(" FROM ").append(metric.getSourceCode());
            List<String> clauses = new ArrayList<>(buildFilterClauses(dsl));
            clauses.add("dt = ${bizDate}");
            sql.append(" WHERE ").append(String.join(" AND ", clauses));
        }
        sql.append("\n-- bizDate=").append(bizDate);
        return sql.toString();
    }

    private Map<String, Object> buildApiLookupPlan(Metric metric) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("planType", "apiLookup");
        plan.put("sourceCode", metric.getSourceCode());
        plan.put("sourceType", metric.getSourceType() == null ? null : metric.getSourceType().getCode());
        plan.put("queryMode", metric.getQueryMode() == null ? null : metric.getQueryMode().getCode());
        plan.put("endpoint", "/" + metric.getSourceCode().toLowerCase().replace("_", "/") + "/batch");
        plan.put("method", "POST");
        Map<String, Object> batch = new LinkedHashMap<>();
        batch.put("maxBatchSize", 500);
        batch.put("timeoutMs", 3000);
        plan.put("batch", batch);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("path", "/" + metric.getSourceCode().toLowerCase().replace("_", "/") + "/batch");
        request.put("bodyMapping", Map.of("entityIds", "$.entityIds"));
        plan.put("request", request);
        plan.put("responseMapping", Map.of("valuePath", "$.value"));
        plan.put("entityKey", "userId");
        return plan;
    }

    private MetricResolveDTO buildResolveDTO(Metric metric, String datasetCode) {
        MetricResolveDTO dto = new MetricResolveDTO()
                .setMetricCode(metric.getMetricCode())
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
        JSONObject dsl = parseDsl(metric.getDsl());
        if (dsl != null) {
            dto.setDsl(new LinkedHashMap<>(dsl));
            dto.getRequiredFields().addAll(extractRequiredFields(dsl, metric.getSourceType()));
        }
        return dto;
    }

    private List<String> extractRequiredFields(JSONObject dsl, MetricSourceType sourceType) {
        Set<String> fields = new LinkedHashSet<>();
        if (dsl == null) {
            return new ArrayList<>(fields);
        }
        JSONObject expr = dsl.getJSONObject("expr");
        if (expr != null) {
            String fieldCode = expr.getString("fieldCode");
            if (StringUtils.hasText(fieldCode)) {
                fields.add(fieldCode);
            }
            String featureValueField = expr.getString("featureValueField");
            if (StringUtils.hasText(featureValueField)) {
                fields.add(featureValueField);
            }
        }
        if (sourceType == MetricSourceType.PORTRAIT_FEATURE) {
            String featureCode = extractString(dsl, "source", "featureCode");
            if (StringUtils.hasText(featureCode)) {
                fields.add("feature_code");
                fields.add("entity_type");
                fields.add("entity_id");
            }
        }
        List<JSONObject> filters = parseArray(dsl, "filters");
        for (JSONObject f : filters) {
            JSONObject target = f.getJSONObject("target");
            if (target != null) {
                String fc = target.getString("fieldCode");
                if (StringUtils.hasText(fc)) {
                    fields.add(fc);
                }
            }
        }
        return new ArrayList<>(fields);
    }

    private List<String> buildFilterClauses(JSONObject dsl) {
        List<String> clauses = new ArrayList<>();
        if (dsl == null) {
            return clauses;
        }
        List<JSONObject> filters = parseArray(dsl, "filters");
        for (JSONObject f : filters) {
            JSONObject target = f.getJSONObject("target");
            String op = f.getString("op");
            Object value = f.get("value");
            if (target == null || !StringUtils.hasText(op)) {
                continue;
            }
            String fieldCode = target.getString("fieldCode");
            if (!StringUtils.hasText(fieldCode)) {
                continue;
            }
            clauses.add(fieldCode + " " + toSqlOp(op) + " ?");
        }
        return clauses;
    }

    private String toSqlOp(String op) {
        return switch (op.toLowerCase()) {
            case "eq" -> "=";
            case "neq" -> "<>";
            case "gt" -> ">";
            case "gte" -> ">=";
            case "lt" -> "<";
            case "lte" -> "<=";
            case "like" -> "LIKE";
            default -> "=";
        };
    }

    private JSONObject parseDsl(String dsl) {
        if (!StringUtils.hasText(dsl)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(dsl);
        } catch (Exception e) {
            log.warn("解析 DSL 失败 dsl={}", dsl, e);
            return new JSONObject();
        }
    }

    private String extractString(JSONObject root, String path1, String path2) {
        if (root == null) {
            return null;
        }
        JSONObject node = root.getJSONObject(path1);
        if (node == null) {
            return null;
        }
        return node.getString(path2);
    }

    private List<JSONObject> parseArray(JSONObject root, String key) {
        if (root == null) {
            return Collections.emptyList();
        }
        try {
            return root.getJSONArray(key).toList(JSONObject.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String formatValue(double value, MetricFormat format, Integer precision) {
        int p = precision == null ? 2 : precision;
        BigDecimal v = BigDecimal.valueOf(value).setScale(p, RoundingMode.HALF_UP);
        if (format == MetricFormat.CURRENCY) {
            return "¥ " + v.toPlainString();
        }
        if (format == MetricFormat.PERCENT) {
            return v.toPlainString() + "%";
        }
        return v.toPlainString();
    }
}
