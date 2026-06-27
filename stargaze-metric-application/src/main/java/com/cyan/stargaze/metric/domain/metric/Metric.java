package com.cyan.stargaze.metric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
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

import java.time.OffsetDateTime;

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
}
