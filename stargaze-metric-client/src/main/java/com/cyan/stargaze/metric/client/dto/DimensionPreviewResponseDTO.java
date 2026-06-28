package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 维度预览响应(维度值分组统计结果)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DimensionPreviewResponseDTO {

    /** 维度业务编码 */
    private String dimensionCode;

    /** 维度名称 */
    private String dimensionName;

    /** 耗时(毫秒) */
    private Long elapsedMs;

    /** 预览 SQL */
    private String sql;

    /** 结果列名 */
    private List<String> columns;

    /** 结果行(每行含维度值与计数) */
    private List<Map<String, Object>> rows;

    /** 执行耗时(秒) */
    private Double executionTime;
}
