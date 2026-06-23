package com.cyan.stargaze.metric.domain.dimension;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

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

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除时间 */
    private LocalDateTime deletedAt;

    public void validate() {
        Assert.notBlank(this.dimensionId, new SilentException("维度 ID 不能为空"));
        Assert.notBlank(this.datasetId, new SilentException("数据集 ID 不能为空"));
        Assert.notBlank(this.fieldId, new SilentException("字段 ID 不能为空"));
    }
}
