package com.cyan.stargaze.metric.adapter.dimension.http.dto;

import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.SemanticType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 维度 DTO。
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

    /** 空间 ID */
    private String workspaceId;

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
}
