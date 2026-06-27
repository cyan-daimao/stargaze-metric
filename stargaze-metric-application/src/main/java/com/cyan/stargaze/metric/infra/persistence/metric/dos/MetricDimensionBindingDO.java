package com.cyan.stargaze.metric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/** 指标-维度绑定表 DO(metric_dimension_binding)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("metric_dimension_binding")
public class MetricDimensionBindingDO {
    /** 主键 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /** 指标 ID */
    @TableField("metric_id")
    private Long metricId;
    /** 指标业务编码 */
    @TableField("metric_code")
    private String metricCode;
    /** 维度 ID */
    @TableField("dimension_id")
    private Long dimensionId;
    /** 维度业务编码 */
    @TableField("dimension_code")
    private String dimensionCode;
    /** 维度名称 */
    @TableField("dimension_name")
    private String dimensionName;
    /** 维度来源类型 */
    @TableField("source_type")
    private MetricSourceType sourceType;
    /** 维度来源编码 */
    @TableField("source_code")
    private String sourceCode;
    /** 创建人 */
    @TableField("created_by")
    private String createdBy;
    /** 修改人 */
    @TableField("updated_by")
    private String updatedBy;
    /** 创建时间 */
    @TableField("created_at")
    private OffsetDateTime createdAt;
    /** 更新时间 */
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
    /** 逻辑删除时间(null=存活,now()=删除) */
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
