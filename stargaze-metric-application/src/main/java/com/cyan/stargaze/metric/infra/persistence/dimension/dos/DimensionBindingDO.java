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

import java.time.OffsetDateTime;

/** 维度绑定表 DO(dimension_binding)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("dimension_binding")
public class DimensionBindingDO {
    /** 主键 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /** 维度 ID */
    @TableField("dimension_id")
    private Long dimensionId;
    /** 数据集 ID */
    @TableField("dataset_id")
    private Long datasetId;
    /** 字段 ID */
    @TableField("field_id")
    private Long fieldId;
    /** 维度计算表达式(可空,直接用字段) */
    @TableField("expr")
    private String expr;
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
