package com.cyan.stargaze.metric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.metric.domain.metric.repository.MetricBindingRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 指标-数据集字段绑定(同一指标可绑定多个数据集,不同物理实现)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricBinding {

    /** 主键 */
    private String id;

    /** 指标 ID */
    private String metricId;

    /** 数据集 ID */
    private String datasetId;

    /** 字段 ID */
    private String fieldId;

    /** 是否主数据集 */
    private Boolean primary;

    /** 该数据集下 DSL 覆盖(可空) */
    private String dslOverride;

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

    public void validate() {
        Assert.notBlank(this.metricId, new SilentException("指标 ID 不能为空"));
        Assert.notBlank(this.datasetId, new SilentException("数据集 ID 不能为空"));
        Assert.notBlank(this.fieldId, new SilentException("字段 ID 不能为空"));
    }

    public boolean isPrimary() {
        return this.primary != null && this.primary;
    }

    /**
     * 保存绑定关系。
     */
    public MetricBinding save(MetricBindingRepository repo) {
        this.validate();
        return repo.save(this);
    }

    /**
     * 更新绑定关系。
     */
    public MetricBinding update(MetricBindingRepository repo) {
        Assert.notBlank(this.id, new SilentException("绑定 ID 不能为空"));
        this.validate();
        return repo.save(this);
    }

    /**
     * 删除绑定关系。
     */
    public void delete(MetricBindingRepository repo) {
        Assert.notBlank(this.id, new SilentException("绑定 ID 不能为空"));
        repo.deleteById(this.id);
    }
}
