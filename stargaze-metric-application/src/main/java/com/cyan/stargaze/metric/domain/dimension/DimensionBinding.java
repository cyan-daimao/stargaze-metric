package com.cyan.stargaze.metric.domain.dimension;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionBindingRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 维度-数据集字段绑定(一个维度可绑定多个数据集的不同物理字段)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DimensionBinding {

    /** 主键 */
    private String id;

    /** 维度 ID */
    private String dimensionId;

    /** 数据集 ID */
    private String datasetId;

    /** 字段 ID */
    private String fieldId;

    /** 维度计算表达式(可空,直接用字段) */
    private String expr;

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
        Assert.notBlank(this.dimensionId, new SilentException("维度 ID 不能为空"));
        Assert.notBlank(this.datasetId, new SilentException("数据集 ID 不能为空"));
        Assert.notBlank(this.fieldId, new SilentException("字段 ID 不能为空"));
    }

    /**
     * 保存绑定关系。
     */
    public DimensionBinding save(DimensionBindingRepository repo) {
        this.validate();
        return repo.save(this);
    }

    /**
     * 更新绑定关系。
     */
    public DimensionBinding update(DimensionBindingRepository repo) {
        Assert.notBlank(this.id, new SilentException("绑定 ID 不能为空"));
        this.validate();
        return repo.save(this);
    }

    /**
     * 删除绑定关系。
     */
    public void delete(DimensionBindingRepository repo) {
        Assert.notBlank(this.id, new SilentException("绑定 ID 不能为空"));
        repo.deleteById(this.id);
    }
}
