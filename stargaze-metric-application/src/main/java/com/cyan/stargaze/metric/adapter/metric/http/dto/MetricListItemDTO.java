package com.cyan.stargaze.metric.adapter.metric.http.dto;

import com.cyan.stargaze.metric.enums.MetricSourceType;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 指标列表项。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricListItemDTO {

    /** 指标业务编码 */
    private String metricCode;

    /** 指标名称 */
    private String metricName;

    /** 业务描述 */
    private String description;

    /** 来源类型 */
    private MetricSourceType sourceType;

    /** 来源编码 */
    private String sourceCode;

    /** 来源名称 */
    private String sourceName;

    /** 来源类型展示标签 */
    private String sourceTypeLabel;

    /** 计算逻辑摘要 */
    private String logicSummary;

    /** 可关联维度 */
    private List<String> relatedDimensions;

    /** 状态 */
    private MetricStatus status;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private OffsetDateTime updatedAt;
}
