package com.cyan.stargaze.metric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 指标×维度组合合法性。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDimensionCompat {

    /** 指标 ID */
    private String metricId;

    /** 维度 ID */
    private String dimensionId;

    /** 是否允许组合 */
    private Boolean allowed;

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

    /**
     * 校验组合合法性对象。
     */
    public void validate() {
        Assert.notNull(this.metricId, new SilentException("指标 ID 不能为空"));
        Assert.notNull(this.dimensionId, new SilentException("维度 ID 不能为空"));
    }
}
