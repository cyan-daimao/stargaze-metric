package com.cyan.stargaze.metric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.SilentException;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 指标-维度绑定（描述指标可组合哪些维度）。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDimensionBinding {

    /** 主键 */
    private String id;

    /** 指标 ID */
    private String metricId;

    /** 指标业务编码 */
    private String metricCode;

    /** 维度 ID */
    private String dimensionId;

    /** 维度业务编码 */
    private String dimensionCode;

    /** 维度名称 */
    private String dimensionName;

    /** 维度来源类型 */
    private MetricSourceType sourceType;

    /** 维度来源编码 */
    private String sourceCode;

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
     * 校验绑定合法性。
     */
    public void validate() {
        Assert.notBlank(this.metricId, new SilentException("指标 ID 不能为空"));
        Assert.notBlank(this.dimensionId, new SilentException("维度 ID 不能为空"));
    }
}
