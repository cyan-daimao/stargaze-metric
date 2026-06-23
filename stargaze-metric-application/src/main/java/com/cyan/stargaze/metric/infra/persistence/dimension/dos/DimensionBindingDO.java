package com.cyan.stargaze.metric.infra.persistence.dimension.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** 维度绑定表 DO(dimension_binding)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("dimension_binding")
public class DimensionBindingDO {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("dimension_id")
    private Long dimensionId;
    @TableField("dataset_id")
    private Long datasetId;
    @TableField("field_id")
    private Long fieldId;
    @TableField("expr")
    private String expr;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
