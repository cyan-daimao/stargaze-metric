package com.cyan.stargaze.metric.domain.dimension;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
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

    /** 空间 ID */
    private String workspaceId;

    /** 维度名(空间内唯一) */
    private String name;

    /** 维度标识（字段名） */
    private String code;

    /** 业务名 */
    private String businessName;

    /** 所属目录 */
    private String folder;

    /** 语义类型(geo/time/category) */
    private SemanticType semanticType;

    /** 字典 ID */
    private String dictionaryId;

    /** 格式(jsonb 字符串) */
    private String format;

    /** 负责人 ID */
    private String ownerId;

    /** 状态 */
    private MetricStatus status;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    private OffsetDateTime createdAt;

    /** 更新时间 */
    private OffsetDateTime updatedAt;

    /** 逻辑删除时间 */
    private OffsetDateTime deletedAt;

    private void validate() {
        Assert.notBlank(this.workspaceId, new SilentException("空间 ID 不能为空"));
        Assert.notBlank(this.name, new SilentException("维度名不能为空"));
        Assert.notNull(this.semanticType, new SilentException("语义类型不能为空"));
    }

    public Dimension save(DimensionRepository repository) {
        validate();
        Dimension existing = repository.findByName(this.workspaceId, this.name);
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
