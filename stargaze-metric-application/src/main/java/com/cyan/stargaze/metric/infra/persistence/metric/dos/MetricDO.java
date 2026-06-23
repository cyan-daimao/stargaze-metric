package com.cyan.stargaze.metric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** 指标表 DO(metric)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("metric")
public class MetricDO {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("workspace_id")
    private Long workspaceId;
    @TableField("name")
    private String name;
    @TableField("business_name")
    private String businessName;
    @TableField("type")
    private MetricType type;
    @TableField("measure_kind")
    private MeasureKind measureKind;
    @TableField("dsl")
    private String dsl;
    @TableField("caliber")
    private String caliber;
    @TableField("owner_id")
    private Long ownerId;
    @TableField("status")
    private MetricStatus status;
    @TableField("version")
    private Integer version;
    @TableField("created_by")
    private Long createdBy;
    @TableField("updated_by")
    private Long updatedBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
