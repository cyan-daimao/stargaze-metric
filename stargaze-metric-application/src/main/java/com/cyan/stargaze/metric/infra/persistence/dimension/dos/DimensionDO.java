package com.cyan.stargaze.metric.infra.persistence.dimension.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.SemanticType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/** 维度表 DO(dimension)。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("dimension")
public class DimensionDO {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("workspace_id")
    private Long workspaceId;
    @TableField("name")
    private String name;
    @TableField("business_name")
    private String businessName;
    @TableField("semantic_type")
    private SemanticType semanticType;
    @TableField("dictionary_id")
    private Long dictionaryId;
    @TableField("format")
    private String format;
    @TableField("owner_id")
    private Long ownerId;
    @TableField("status")
    private MetricStatus status;
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
