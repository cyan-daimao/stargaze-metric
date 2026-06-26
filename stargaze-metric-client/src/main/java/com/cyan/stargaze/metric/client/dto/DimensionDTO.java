package com.cyan.stargaze.metric.client.dto;

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

    /** 维度名 */
    private String name;

    /** 业务名 */
    private String businessName;

    /** 语义类型 */
    private SemanticType semanticType;

    /** 字典 ID */
    private String dictionaryId;

    /** 格式(jsonb 字符串) */
    private String format;

    /** 状态 */
    private MetricStatus status;

    /** 字段名 */
    private String fieldName;

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
    private OffsetDateTime createdAt;

    /** 更新时间 */
    private OffsetDateTime updatedAt;
}
