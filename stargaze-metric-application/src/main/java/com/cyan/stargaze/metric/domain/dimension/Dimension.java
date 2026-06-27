package com.cyan.stargaze.metric.domain.dimension;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
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

    private void validate() {
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
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    public Dimension publish(DimensionRepository repository) {
        this.status = MetricStatus.PUBLISHED;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    public void delete(DimensionRepository repository) {
        Assert.notBlank(this.id, new SilentException("维度 ID 不能为空"));
        repository.deleteById(this.id);
    }

    public boolean isPublished() {
        return this.status == MetricStatus.PUBLISHED;
    }
}
