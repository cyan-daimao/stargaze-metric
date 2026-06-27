package com.cyan.stargaze.metric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * 指标×维度组合合法性表 DO(metric_dimension_compat)。
 * <p>
 * 原表为复合主键 (metric_id, dimension_id);为适配 MyBatis-Plus,以 metric_id 作为 @TableId,
 * 查询均走 LambdaQueryWrapper,不依赖 selectById。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("metric_dimension_compat")
public class MetricDimensionCompatDO {
    /** 指标 ID(复合主键之一) */
    @TableId(value = "metric_id", type = IdType.INPUT)
    private Long metricId;
    /** 维度 ID(复合主键之一) */
    @TableField("dimension_id")
    private Long dimensionId;
    /** 是否允许组合 */
    @TableField("allowed")
    private Boolean allowed;
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
