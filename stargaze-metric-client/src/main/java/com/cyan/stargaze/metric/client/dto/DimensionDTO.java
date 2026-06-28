package com.cyan.stargaze.metric.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.SemanticType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 维度 DTO（对外契约）。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DimensionDTO {

    /** 主键 */
    private String id;

    /** 维度全局唯一编码 */
    private String name;

    /** 维度显示名 */
    private String dimName;

    /** 维度描述 */
    private String description;

    /** 语义类型 */
    private SemanticType semanticType;

    /** 字典 ID */
    private String dictionaryId;

    /** 格式(jsonb 字符串) */
    private String format;

    /** 状态 */
    private MetricStatus status;

    /** 来源类型 */
    private MetricSourceType sourceType;

    /** 来源类型展示标签,如"数据集"、"画像平台" */
    private String sourceTypeLabel;

    /** 源字段编码 */
    private String dimCode;

    /** 所属目录 */
    private String folder;

    /** 关联指标 */
    private List<String> relatedMetrics;

    /** 关联指标数量 */
    private Integer relatedMetricCount;

    /** 关联数据集 */
    private List<String> relatedDatasets;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private OffsetDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private OffsetDateTime updatedAt;
}
