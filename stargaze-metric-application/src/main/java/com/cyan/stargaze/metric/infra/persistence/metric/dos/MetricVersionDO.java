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

/** 指标版本表 DO(metric_version)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("metric_version")
public class MetricVersionDO {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("metric_id")
    private Long metricId;
    @TableField("version")
    private Integer version;
    @TableField("dsl")
    private String dsl;
    @TableField("change_log")
    private String changeLog;
    @TableField("created_by")
    private Long createdBy;
    @TableField("created_at")
    private OffsetDateTime createdAt;
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
