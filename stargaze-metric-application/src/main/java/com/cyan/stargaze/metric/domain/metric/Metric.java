package com.cyan.stargaze.metric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 业务指标(充血模型,口径统一)。
 * <p>
 * 状态机:draft → published(口径冻结,不可改 expression)→ offline/deprecated。
 * 版本管理:发布/口径变更记 metric_version。
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

    /** 空间 ID */
    private String workspaceId;

    /** 指标名(空间内唯一) */
    private String name;

    /** 指标标识（英文代码，空间内唯一） */
    private String code;

    /** 业务名 */
    private String businessName;

    /** 业务定义/口径说明 */
    private String description;

    /** 所属目录 */
    private String folder;

    /** 数据格式 */
    private MetricFormat format;

    /** 指标类型(atomic/derived/window) */
    private MetricType type;

    /** 聚合方式 */
    private MeasureKind measureKind;

    /** 计算表达式（对应原 DSL） */
    private String expression;

    /** 过滤条件 */
    private String filterCondition;

    /** 小数位 */
    private Integer precision;

    /** 指标 DSL(兼容旧字段) */
    private String dsl;

    /** 口径说明(兼容旧字段) */
    private String caliber;

    /** 主数据集 ID */
    private String primaryDatasetId;

    /** 辅助数据集 ID 列表（不持久化到 metric 表，由绑定表维护） */
    private List<String> boundDatasetIds;

    /** 绑定的维度字段名列表（不持久化到 metric 表，由绑定表维护） */
    private List<String> dimensions;

    /** 负责人 ID */
    private String ownerId;

    /** 状态 */
    private MetricStatus status;

    /** 版本号 */
    private Integer version;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private OffsetDateTime createdAt;

    /** 更新时间 */
    private OffsetDateTime updatedAt;

    /** 逻辑删除时间 */
    private OffsetDateTime deletedAt;

    private void validate() {
        Assert.notBlank(this.workspaceId, new SilentException("空间 ID 不能为空"));
        Assert.notBlank(this.name, new SilentException("指标名不能为空"));
        Assert.notBlank(this.code, new SilentException("指标标识不能为空"));
        Assert.notNull(this.type, new SilentException("指标类型不能为空"));
        Assert.notNull(this.measureKind, new SilentException("聚合方式不能为空"));
        Assert.notBlank(this.expression, new SilentException("计算表达式不能为空"));
        Assert.notBlank(this.primaryDatasetId, new SilentException("主数据集不能为空"));
    }

    /**
     * 保存(新建,空间内名称与标识唯一)
     */
    public Metric save(MetricRepository repository) {
        validate();
        Metric existing = repository.findByName(this.workspaceId, this.name);
        Assert.isNull(existing, new SilentException("指标名已存在"));
        Metric existingCode = repository.findByCode(this.workspaceId, this.code);
        Assert.isNull(existingCode, new SilentException("指标标识已存在"));
        this.status = MetricStatus.DRAFT;
        this.version = 1;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新(仅 draft 可改 expression;published 走版本)
     */
    public Metric update(MetricRepository repository) {
        Assert.notBlank(this.id, new SilentException("指标 ID 不能为空"));
        Assert.isTrue(this.status == MetricStatus.DRAFT,
                new SilentException("已发布指标不可直接修改,请走版本变更"));
        validate();
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    /**
     * 发布:draft → published,口径冻结,版本号递增
     */
    public Metric publish(MetricRepository repository) {
        Assert.isTrue(this.status == MetricStatus.DRAFT,
                new SilentException("仅草稿状态可发布"));
        this.status = MetricStatus.PUBLISHED;
        this.version = (this.version == null ? 1 : this.version) + 1;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    /**
     * 下线:published → offline
     */
    public Metric offline(MetricRepository repository) {
        Assert.isTrue(this.status == MetricStatus.PUBLISHED,
                new SilentException("仅已发布指标可下线"));
        this.status = MetricStatus.OFFLINE;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    /**
     * 废弃:published → deprecated(兼容旧状态)
     */
    public Metric deprecate(MetricRepository repository) {
        Assert.isTrue(this.status == MetricStatus.PUBLISHED,
                new SilentException("仅已发布指标可废弃"));
        this.status = MetricStatus.DEPRECATED;
        this.updatedAt = OffsetDateTime.now();
        return repository.update(this);
    }

    /**
     * 是否已发布(消费红线:看板/ChatBI 只能引用已发布指标)
     */
    public boolean isPublished() {
        return this.status == MetricStatus.PUBLISHED;
    }

    /**
     * 是否可用(草稿/已发布为可用)
     */
    public boolean isAvailable() {
        return this.status == MetricStatus.DRAFT || this.status == MetricStatus.PUBLISHED;
    }

    public void delete(MetricRepository repository) {
        Assert.notBlank(this.id, new SilentException("指标 ID 不能为空"));
        repository.deleteById(this.id);
    }
}
