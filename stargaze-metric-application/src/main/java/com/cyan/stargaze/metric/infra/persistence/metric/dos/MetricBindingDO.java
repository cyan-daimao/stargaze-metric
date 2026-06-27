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

/** 指标绑定表 DO(metric_binding)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("metric_binding")
public class MetricBindingDO {
    /** 主键 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /** 指标 ID */
    @TableField("metric_id")
    private Long metricId;
    /** 数据集 ID */
    @TableField("dataset_id")
    private Long datasetId;
    /** 字段 ID */
    @TableField("field_id")
    private Long fieldId;
    /** 是否主数据集绑定 */
    @TableField("is_primary")
    private Boolean primary;
    /** DSL 覆盖(JSON,可空) */
    @TableField("dsl_override")
    private String dslOverride;
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
