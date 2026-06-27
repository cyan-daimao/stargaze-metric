package com.cyan.stargaze.metric.domain.metric;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.dto.DatasetFieldDTO;
import com.cyan.stargaze.dataset.enums.DataType;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 业务指标(充血模型,语义资产)。
 * <p>
 * 状态机:draft → published(口径冻结)→ offline; sourceError 为来源异常态。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Metric {

    /** 主键 */
    private String id;

    /** 指标业务编码(全局唯一) */
    private String metricCode;

    /** 指标名称(全局唯一) */
    private String name;

    /** 指标标识(英文代码,全局唯一) */
    private String code;

    /** 业务定义/口径说明 */
    private String description;

    /** 所属目录 */
    private String folder;

    /** 数据格式 */
    private MetricFormat format;

    /** 状态 */
    private MetricStatus status;

    /** 来源类型 */
    private MetricSourceType sourceType;

    /** 来源编码 */
    private String sourceCode;

    /** 来源名称 */
    private String sourceName;

    /** 查询能力 */
    private QueryMode queryMode;

    /** 数据新鲜度 */
    private Freshness freshness;

    /** DSL 类型 */
    private MetricDslKind dslKind;

    /** 指标 DSL JSON */
    private String dsl;

    /** 来源解析快照 JSON */
    private String sourceSnapshot;

    /** 来源能力声明 JSON */
    private String supports;

    /** 小数位精度 */
    private Integer precision;

    /** 负责人 ID */
    private String ownerId;

    /** 创建人工号 */
    private String createdBy;

    /** 更新人工号 */
    private String updatedBy;

    /** 创建时间 */
    private OffsetDateTime createdAt;

    /** 更新时间 */
    private OffsetDateTime updatedAt;

    /** 逻辑删除时间 */
    private OffsetDateTime deletedAt;

    private void validate() {
        Assert.notBlank(this.metricCode, new SilentException("指标业务编码不能为空"));
        Assert.notBlank(this.name, new SilentException("指标名称不能为空"));
        Assert.notBlank(this.code, new SilentException("指标标识不能为空"));
        Assert.notNull(this.sourceType, new SilentException("来源类型不能为空"));
        Assert.notBlank(this.sourceCode, new SilentException("来源编码不能为空"));
        Assert.notNull(this.queryMode, new SilentException("查询能力不能为空"));
        Assert.notNull(this.dslKind, new SilentException("DSL 类型不能为空"));
        Assert.notBlank(this.dsl, new SilentException("指标 DSL 不能为空"));
    }

    /**
     * 保存(新建)
     */
    public Metric save(MetricRepository repository) {
        validate();
        Assert.isNull(repository.findByMetricCode(this.metricCode), new SilentException("指标业务编码已存在"));
        Assert.isNull(repository.findByName(this.name), new SilentException("指标名称已存在"));
        Assert.isNull(repository.findByCode(this.code), new SilentException("指标标识已存在"));
        this.status = MetricStatus.DRAFT;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新(仅 draft 可改核心口径)
     */
    public Metric update(MetricRepository repository) {
        Assert.notBlank(this.id, new SilentException("指标 ID 不能为空"));
        Assert.isTrue(this.status == MetricStatus.DRAFT, new SilentException("仅草稿指标可直接修改"));
        validate();
        Metric existingName = repository.findByName(this.name);
        Assert.isTrue(existingName == null || existingName.getId().equals(this.id),
                new SilentException("指标名称已存在"));
        Metric existingCode = repository.findByCode(this.code);
        Assert.isTrue(existingCode == null || existingCode.getId().equals(this.id),
                new SilentException("指标标识已存在"));
        Metric existingMetricCode = repository.findByMetricCode(this.metricCode);
        Assert.isTrue(existingMetricCode == null || existingMetricCode.getId().equals(this.id),
                new SilentException("指标业务编码已存在"));
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    /**
     * 发布:draft → published
     */
    public Metric publish(MetricRepository repository) {
        Assert.isTrue(this.status == MetricStatus.DRAFT, new SilentException("仅草稿状态可发布"));
        this.status = MetricStatus.PUBLISHED;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    /**
     * 下线:published → offline
     */
    public Metric offline(MetricRepository repository) {
        Assert.isTrue(this.status == MetricStatus.PUBLISHED, new SilentException("仅已发布指标可下线"));
        this.status = MetricStatus.OFFLINE;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    /**
     * 是否已发布(消费红线)
     */
    public boolean isPublished() {
        return this.status == MetricStatus.PUBLISHED;
    }

    /**
     * 是否可用(draft/published)
     */
    public boolean isAvailable() {
        return this.status == MetricStatus.DRAFT || this.status == MetricStatus.PUBLISHED;
    }

    public void delete(MetricRepository repository) {
        Assert.notBlank(this.id, new SilentException("指标 ID 不能为空"));
        Assert.isTrue(this.status != MetricStatus.PUBLISHED, new SilentException("已发布指标不可删除,请先下线"));
        repository.deleteById(this.id);
    }

    // ==================== DSL 与展示语义(充血模型) ====================

    /**
     * 解析 DSL 为 JSON 对象。
     */
    public JSONObject dslObject() {
        if (isBlank(this.dsl)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(this.dsl);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    /**
     * 从 DSL 中提取两层嵌套字符串,如 source.featureCode。
     */
    public String dslField(String path1, String path2) {
        JSONObject root = dslObject();
        if (root == null) {
            return null;
        }
        JSONObject node = root.getJSONObject(path1);
        if (node == null) {
            return null;
        }
        return node.getString(path2);
    }

    /**
     * 提取 DSL 中的过滤条件列表。
     */
    public List<JSONObject> dslFilters() {
        JSONObject root = dslObject();
        if (root == null) {
            return Collections.emptyList();
        }
        try {
            return root.getJSONArray("filters").toList(JSONObject.class);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * 计算逻辑摘要,如"SUM(order_amount)"。
     */
    public String logicSummary() {
        if (this.dslKind == MetricDslKind.API_METRIC) {
            return "IMPORT " + (this.sourceType == null ? "" : this.sourceType.getCode());
        }
        JSONObject dsl = dslObject();
        if (dsl == null || dsl.isEmpty()) {
            return this.dslKind == null ? "" : this.dslKind.getCode();
        }
        JSONObject expr = dsl.getJSONObject("expr");
        if (expr == null) {
            return this.dslKind == null ? "" : this.dslKind.getCode();
        }
        String op = expr.getString("op");
        if ("featureValue".equals(op)) {
            return "IMPORT portraitFeature";
        }
        String func = expr.getString("func");
        String fieldCode = expr.getString("fieldCode");
        if (!isBlank(func) && !isBlank(fieldCode)) {
            return func.toUpperCase() + "(" + fieldCode + ")";
        }
        return this.dslKind == null ? "" : this.dslKind.getCode();
    }

    /**
     * 来源类型展示标签。
     */
    public String sourceTypeLabel() {
        if (this.sourceType == null) {
            return "";
        }
        return switch (this.sourceType) {
            case DATASET -> "数据集";
            case PORTRAIT_FEATURE, PORTRAIT_TAG, PORTRAIT_CROWD -> "画像平台";
            case REALTIME_TABLE -> "实时表";
            case HTTP_API -> "HTTP API";
        };
    }

    /**
     * 构造单指标预览 SQL。
     */
    public String previewSql(String bizDate) {
        String date = isBlank(bizDate) ? "latest" : bizDate;
        String metricCode = this.metricCode;
        StringBuilder sql = new StringBuilder();
        if (this.sourceType == MetricSourceType.PORTRAIT_FEATURE) {
            String featureCode = dslField("source", "featureCode");
            if (isBlank(featureCode)) {
                featureCode = this.sourceCode;
            }
            sql.append("SELECT SUM(CAST(feature_value_decimal AS DECIMAL(18,2))) AS ").append(metricCode)
                    .append(" FROM portrait_feature_value_store")
                    .append(" WHERE entity_type = 'user'")
                    .append(" AND feature_code = '").append(featureCode).append("'")
                    .append(" AND dt = ${bizDate}");
        } else if (this.sourceType == MetricSourceType.REALTIME_TABLE) {
            String func = dslField("expr", "func");
            String fieldCode = dslField("expr", "fieldCode");
            if (isBlank(func)) {
                func = "SUM";
            }
            if (isBlank(fieldCode)) {
                fieldCode = "value";
            }
            sql.append("SELECT ").append(func.toUpperCase())
                    .append("(").append(fieldCode).append(") AS ").append(metricCode)
                    .append(" FROM ").append(this.sourceCode)
                    .append(" WHERE dt = ${bizDate}");
        } else {
            String func = dslField("expr", "func");
            String fieldCode = dslField("expr", "fieldCode");
            if (isBlank(func)) {
                func = "SUM";
            }
            if (isBlank(fieldCode)) {
                fieldCode = "value";
            }
            sql.append("SELECT ").append(func.toUpperCase())
                    .append("(CAST(").append(fieldCode).append(" AS DECIMAL(18,2))) AS ").append(metricCode)
                    .append(" FROM ").append(this.sourceCode);
            List<String> clauses = new ArrayList<>(filterClauses());
            clauses.add("dt = ${bizDate}");
            sql.append(" WHERE ").append(String.join(" AND ", clauses));
        }
        sql.append("\n-- bizDate=").append(date);
        return sql.toString();
    }

    /**
     * 构造 API 点查计划。
     */
    public Map<String, Object> apiLookupPlan() {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("planType", "apiLookup");
        plan.put("sourceCode", this.sourceCode);
        plan.put("sourceType", this.sourceType == null ? null : this.sourceType.getCode());
        plan.put("queryMode", this.queryMode == null ? null : this.queryMode.getCode());
        String endpoint = "/" + (isBlank(this.sourceCode) ? "" : this.sourceCode).toLowerCase().replace("_", "/") + "/batch";
        plan.put("endpoint", endpoint);
        plan.put("method", "POST");
        Map<String, Object> batch = new LinkedHashMap<>();
        batch.put("maxBatchSize", 500);
        batch.put("timeoutMs", 3000);
        plan.put("batch", batch);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("path", endpoint);
        request.put("bodyMapping", Map.of("entityIds", "$.entityIds"));
        plan.put("request", request);
        plan.put("responseMapping", Map.of("valuePath", "$.value"));
        plan.put("entityKey", "userId");
        return plan;
    }

    /**
     * 提取指标查询所需的物理字段列表。
     */
    public List<String> requiredFields() {
        Set<String> fields = new LinkedHashSet<>();
        JSONObject dsl = dslObject();
        if (dsl == null) {
            return new ArrayList<>(fields);
        }
        JSONObject expr = dsl.getJSONObject("expr");
        if (expr != null) {
            String fieldCode = expr.getString("fieldCode");
            if (!isBlank(fieldCode)) {
                fields.add(fieldCode);
            }
            String featureValueField = expr.getString("featureValueField");
            if (!isBlank(featureValueField)) {
                fields.add(featureValueField);
            }
        }
        if (this.sourceType == MetricSourceType.PORTRAIT_FEATURE) {
            String featureCode = dslField("source", "featureCode");
            if (!isBlank(featureCode)) {
                fields.add("feature_code");
                fields.add("entity_type");
                fields.add("entity_id");
            }
        }
        for (JSONObject f : dslFilters()) {
            JSONObject target = f.getJSONObject("target");
            if (target != null) {
                String fc = target.getString("fieldCode");
                if (!isBlank(fc)) {
                    fields.add(fc);
                }
            }
        }
        return new ArrayList<>(fields);
    }

    /**
     * 提取聚合函数名(小写)。
     */
    public String aggregateFunction() {
        String func = dslField("expr", "func");
        return isBlank(func) ? null : func.toLowerCase();
    }

    /**
     * 提取聚合字段编码。
     */
    public String aggregateFieldCode() {
        return dslField("expr", "fieldCode");
    }

    /**
     * 将 DSL 过滤条件转换为 SQL WHERE 子句片段(占位符形式)。
     */
    public List<String> filterClauses() {
        List<String> clauses = new ArrayList<>();
        for (JSONObject f : dslFilters()) {
            JSONObject target = f.getJSONObject("target");
            String op = f.getString("op");
            if (target == null || isBlank(op)) {
                continue;
            }
            String fieldCode = target.getString("fieldCode");
            if (isBlank(fieldCode)) {
                continue;
            }
            clauses.add(fieldCode + " " + sqlOp(op) + " ?");
        }
        return clauses;
    }

    /**
     * 格式化数值。
     */
    public String formatValue(double value) {
        int p = this.precision == null ? 2 : this.precision;
        BigDecimal v = BigDecimal.valueOf(value).setScale(p, RoundingMode.HALF_UP);
        if (this.format == MetricFormat.CURRENCY) {
            return "¥ " + v.toPlainString();
        }
        if (this.format == MetricFormat.PERCENT) {
            return v.toPlainString() + "%";
        }
        return v.toPlainString();
    }

    /**
     * 根据数据集字段构造原子指标 DSL。
     */
    public static String buildAtomicDsl(String datasetCode, DatasetFieldDTO field) {
        Map<String, Object> dsl = new LinkedHashMap<>();
        dsl.put("version", "metric.dsl.v1");
        dsl.put("kind", MetricDslKind.ATOMIC.getCode());
        dsl.put("source", Map.of("type", MetricSourceType.DATASET.getCode(), "datasetCode", datasetCode));
        dsl.put("expr", Map.of("op", "agg", "func", "SUM", "fieldCode", field.getFieldName()));
        return JSON.toJSONString(dsl);
    }

    /**
     * 根据数据集编码和字段名生成全局唯一指标编码。
     *
     * @param datasetCode 数据集编码
     * @param fieldName   字段名
     * @return 指标编码(如 "sales_order.amount")
     */
    public static String generateCode(String datasetCode, String fieldName) {
        return datasetCode + "." + fieldName;
    }

    /**
     * 根据数据类型推断指标格式。
     */
    public static MetricFormat inferFormat(DataType dataType) {
        if (dataType == DataType.INT) {
            return MetricFormat.INT;
        }
        if (dataType == DataType.DECIMAL) {
            return MetricFormat.CURRENCY;
        }
        return MetricFormat.NUMBER;
    }

    private static String sqlOp(String op) {
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

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
