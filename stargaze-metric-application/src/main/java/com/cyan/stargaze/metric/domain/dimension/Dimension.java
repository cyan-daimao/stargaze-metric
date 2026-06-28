package com.cyan.stargaze.metric.domain.dimension;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.dataset.client.dto.DatasetFieldDTO;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
import com.cyan.stargaze.metric.enums.SemanticType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 业务维度(充血模型,可跨数据集)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Dimension {

    /** 主键 */
    private String id;

    /** 维度名(唯一) */
    private String name;

    /** 维度业务编码(全局唯一) */
    private String code;

    /** 业务名 */
    private String businessName;

    /** 描述 */
    private String description;

    /** 所属目录 */
    private String folder;

    /** 语义类型(geo/time/category) */
    private SemanticType semanticType;

    /** 字典 ID */
    private String dictionaryId;

    /** 格式(jsonb 字符串) */
    private String format;

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

    /** 维度 DSL JSON */
    private String dsl;

    /** 来源解析快照 JSON */
    private String sourceSnapshot;

    /** 来源能力声明 JSON */
    private String supports;

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

    public void validate() {
        Assert.notBlank(this.name, new SilentException("维度名不能为空"));
        Assert.notNull(this.semanticType, new SilentException("语义类型不能为空"));
    }

    public Dimension save(DimensionRepository repository) {
        validate();
        Dimension existing = repository.findByName(this.name);
        Assert.isNull(existing, new SilentException("维度名已存在"));
        this.status = MetricStatus.DRAFT;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        return repository.save(this);
    }

    public Dimension update(DimensionRepository repository) {
        Assert.notBlank(this.id, new SilentException("维度 ID 不能为空"));
        validate();
        Dimension existingName = repository.findByName(this.name);
        Assert.isTrue(existingName == null || existingName.getId().equals(this.id),
                new SilentException("维度名已存在"));
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    public Dimension publish(DimensionRepository repository) {
        Assert.isTrue(this.status == MetricStatus.DRAFT, new SilentException("仅草稿状态可发布"));
        this.status = MetricStatus.PUBLISHED;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    public Dimension offline(DimensionRepository repository) {
        Assert.isTrue(this.status == MetricStatus.PUBLISHED, new SilentException("仅已发布维度可下线"));
        this.status = MetricStatus.OFFLINE;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    public void delete(DimensionRepository repository) {
        Assert.notBlank(this.id, new SilentException("维度 ID 不能为空"));
        Assert.isTrue(this.status != MetricStatus.PUBLISHED, new SilentException("已发布维度不可删除,请先下线"));
        repository.deleteById(this.id);
    }

    public boolean isPublished() {
        return this.status == MetricStatus.PUBLISHED;
    }

    /**
     * 维度显示名(业务名优先)。
     */
    public String displayName() {
        return isBlank(this.businessName) ? this.name : this.businessName;
    }

    /**
     * 从维度 DSL 中提取源字段编码。
     */
    public String extractFieldCode() {
        if (isBlank(this.dsl)) {
            return null;
        }
        try {
            JSONObject obj = JSON.parseObject(this.dsl);
            JSONObject expr = obj.getJSONObject("expr");
            if (expr != null) {
                return expr.getString("fieldCode");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 从维度 DSL 中提取源数据集编码。
     */
    public String extractDatasetCode() {
        if (isBlank(this.dsl)) {
            return null;
        }
        try {
            JSONObject obj = JSON.parseObject(this.dsl);
            JSONObject source = obj.getJSONObject("source");
            if (source != null) {
                return source.getString("datasetCode");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 生成维度预览 SQL(分组统计维度值)。
     *
     * @param sourceCode 数据集编码(用于 FROM 子句)
     * @param fieldCode  维度字段编码
     * @return 预览 SQL
     */
    public String previewSql(String sourceCode, String fieldCode) {
        return "SELECT " + fieldCode + ", COUNT(1) AS cnt"
                + " FROM " + sourceCode
                + " GROUP BY " + fieldCode
                + " ORDER BY cnt DESC"
                + " LIMIT 100";
    }

    /**
     * 根据数据集字段构造维度字段 DSL。
     */
    public static String buildFieldDsl(String datasetCode, DatasetFieldDTO field) {
        Map<String, Object> dsl = new LinkedHashMap<>();
        dsl.put("version", "dimension.dsl.v1");
        dsl.put("kind", MetricDslKind.FIELD.getCode());
        dsl.put("source", Map.of("type", MetricSourceType.DATASET.getCode(), "datasetCode", datasetCode));
        dsl.put("expr", Map.of("fieldCode", field.getFieldName()));
        return JSON.toJSONString(dsl);
    }

    /**
     * 根据数据集语义类型推断维度语义类型。
     */
    public static SemanticType inferSemanticType(String semanticType) {
        if (isBlank(semanticType)) {
            return SemanticType.CATEGORY;
        }
        return switch (semanticType.toUpperCase()) {
            case "GEO" -> SemanticType.GEO;
            case "TIME" -> SemanticType.TIME;
            default -> SemanticType.CATEGORY;
        };
    }

    /**
     * 生成业务编码(前缀_字段名)。
     */
    public static String generateCode(String prefix, String fieldName) {
        String sanitized = (fieldName == null ? "" : fieldName)
                .replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "_")
                .replaceAll("_+", "_")
                .toLowerCase();
        return (prefix + "_" + sanitized).toLowerCase();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
