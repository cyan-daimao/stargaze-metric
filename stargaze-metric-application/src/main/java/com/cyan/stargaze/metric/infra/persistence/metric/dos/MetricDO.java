package com.cyan.stargaze.metric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.stargaze.metric.enums.Freshness;
import com.cyan.stargaze.metric.enums.MetricDslKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.QueryMode;
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
    /** 主键 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /** 指标业务编码 */
    @TableField("metric_code")
    private String metricCode;
    /** 指标名称 */
    @TableField("name")
    private String name;
    /** 指标标识(全局唯一) */
    @TableField("code")
    private String code;
    /** 指标描述 */
    @TableField("description")
    private String description;
    /** 所属文件夹 */
    @TableField("folder")
    private String folder;
    /** 指标格式 */
    @TableField("format")
    private MetricFormat format;
    /** 指标状态 */
    @TableField("status")
    private MetricStatus status;
    /** 来源类型 */
    @TableField("source_type")
    private MetricSourceType sourceType;
    /** 来源编码 */
    @TableField("source_code")
    private String sourceCode;
    /** 来源名称 */
    @TableField("source_name")
    private String sourceName;
    /** 查询模式 */
    @TableField("query_mode")
    private QueryMode queryMode;
    /** 数据鲜度 */
    @TableField("freshness")
    private Freshness freshness;
    /** DSL 类型 */
    @TableField("dsl_kind")
    private MetricDslKind dslKind;
    /** DSL 定义(JSON) */
    @TableField("dsl")
    private String dsl;
    /** 来源快照(JSON) */
    @TableField("source_snapshot")
    private String sourceSnapshot;
    /** 支持的查询能力(JSON) */
    @TableField("supports")
    private String supports;
    /** 数值精度(小数位数) */
    @TableField("precision")
    private Integer precision;
    /** 负责人 ID */
    @TableField("owner_id")
    private Long ownerId;
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
