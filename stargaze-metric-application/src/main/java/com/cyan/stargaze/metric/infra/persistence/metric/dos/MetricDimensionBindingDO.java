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

/** 指标维度绑定表 DO(metric_dimension_binding)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("metric_dimension_binding")
public class MetricDimensionBindingDO {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("metric_id")
    private Long metricId;
    @TableField("dimension_id")
    private Long dimensionId;
    @TableField("dimension_name")
    private String dimensionName;
    @TableField("dataset_id")
    private Long datasetId;
    @TableField("field_id")
    private Long fieldId;
    @TableField("created_at")
    private OffsetDateTime createdAt;
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
