package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 指标预览响应(单指标值)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PreviewResponseDTO {

    /** 指标业务编码 */
    private String metricCode;

    /** 指标名称 */
    private String metricName;

    /** 数值 */
    private Double value;

    /** 格式化数值 */
    private String formattedValue;

    /** 计划类型: SqlPlan / ApiLookupPlan */
    private String planType;

    /** 耗时(毫秒) */
    private Long elapsedMs;

    /** 预览 SQL(仅 SQL 来源) */
    private String sql;

    /** API 点查计划(仅 httpApi 来源) */
    private Map<String, Object> apiLookupPlan;
}
