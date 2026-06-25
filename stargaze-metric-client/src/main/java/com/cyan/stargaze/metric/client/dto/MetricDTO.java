package com.cyan.stargaze.metric.client.dto;

import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricFormat;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 指标 DTO(对外契约 + 前端)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDTO {

    /** 主键 */
    private String id;

    /** 空间 ID */
    private String workspaceId;

    /** 指标名 */
    private String name;

    /** 指标标识（英文代码） */
    private String code;

    /** 业务名 */
    private String businessName;

    /** 业务定义/口径说明 */
    private String description;

    /** 所属目录 */
    private String folder;

    /** 数据格式 */
    private MetricFormat format;

    /** 指标类型 */
    private MetricType type;

    /** 聚合方式（兼容旧字段 measureKind） */
    private MeasureKind aggregation;

    /** 聚合方式（原枚举） */
    private MeasureKind measureKind;

    /** 计算表达式（对应原 DSL） */
    private String expression;

    /** 过滤条件 */
    private String filterCondition;

    /** 小数位 */
    private Integer precision;

    /** 指标 DSL（兼容旧字段） */
    private String dsl;

    /** 口径说明（兼容旧字段） */
    private String caliber;

    /** 主数据集 ID */
    private String primaryDatasetId;

    /** 主数据集名称 */
    private String primaryDatasetName;

    /** 辅助数据集 ID 列表 */
    private List<String> secondaryDatasetIds;

    /** 辅助数据集数量 */
    private Integer secondaryDatasetCount;

    /** 维度数量 */
    private Integer dimensionCount;

    /** 绑定维度引用列表 */
    private List<MetricDimensionRefDTO> dimensions;

    /** 状态 */
    private MetricStatus status;

    /** 版本号 */
    private Integer version;

    /** 负责人 ID */
    private String ownerId;

    /** 创建人 ID */
    private String createdBy;

    /** 创建人姓名（展示用） */
    private String creatorName;

    /** 修改人 ID */
    private String updatedBy;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新时间 */
    private OffsetDateTime updateTime;
}
