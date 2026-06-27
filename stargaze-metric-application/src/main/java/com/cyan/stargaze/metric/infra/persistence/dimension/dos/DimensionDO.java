package com.cyan.stargaze.metric.infra.persistence.dimension.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
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
    @TableField("dim_code")
    private String code;
    @TableField("name")
    private String name;
    @TableField(exist = false)
    private String businessName;
    @TableField("description")
    private String description;
    @TableField("folder")
    private String folder;
    @TableField("semantic_type")
    private SemanticType semanticType;
    @TableField(exist = false)
    private Long dictionaryId;
    @TableField("format")
    private String format;
    @TableField("status")
    private MetricStatus status;
    @TableField("source_type")
    private MetricSourceType sourceType;
    @TableField("source_code")
    private String sourceCode;
    @TableField("source_name")
    private String sourceName;
    @TableField("query_mode")
    private QueryMode queryMode;
    @TableField("freshness")
    private Freshness freshness;
    @TableField("dsl_kind")
    private MetricDslKind dslKind;
    @TableField("dsl")
    private String dsl;
    @TableField("source_snapshot")
    private String sourceSnapshot;
    @TableField("supports")
    private String supports;
    @TableField("owner_id")
    private Long ownerId;
    @TableField("created_by")
    private String createdBy;
    @TableField("updated_by")
    private String updatedBy;
    @TableField("created_at")
    private OffsetDateTime createdAt;
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
