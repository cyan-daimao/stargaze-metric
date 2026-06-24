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
    @TableId(value = "metric_id", type = IdType.INPUT)
    private Long metricId;
    @TableField("dimension_id")
    private Long dimensionId;
    @TableField("allowed")
    private Boolean allowed;
    @TableField("created_at")
    private OffsetDateTime createdAt;
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
