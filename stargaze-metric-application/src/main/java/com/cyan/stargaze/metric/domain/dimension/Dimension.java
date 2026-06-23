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

import java.time.LocalDateTime;

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

    /** 业务名 */
    private String businessName;

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
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除时间 */
    private LocalDateTime deletedAt;

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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    public Dimension update(DimensionRepository repository) {
        Assert.notBlank(this.id, new SilentException("维度 ID 不能为空"));
        validate();
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    public Dimension publish(DimensionRepository repository) {
        this.status = MetricStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
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
