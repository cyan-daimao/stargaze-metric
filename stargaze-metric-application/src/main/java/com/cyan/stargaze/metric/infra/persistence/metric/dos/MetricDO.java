package com.cyan.stargaze.metric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

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
    @TableField("code")
    private String code;
    @TableField("business_name")
    private String businessName;
    @TableField("description")
    private String description;
    @TableField("folder")
    private String folder;
    @TableField("format")
    private MetricFormat format;
    @TableField("type")
    private MetricType type;
    @TableField("measure_kind")
    private MeasureKind measureKind;
    @TableField("expression")
    private String expression;
    @TableField("filter_condition")
    private String filterCondition;
    @TableField("precision")
    private Integer precision;
    @TableField("dsl")
    private String dsl;
    @TableField("caliber")
    private String caliber;
    @TableField("primary_dataset_id")
    private Long primaryDatasetId;
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
    private OffsetDateTime createdAt;
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
