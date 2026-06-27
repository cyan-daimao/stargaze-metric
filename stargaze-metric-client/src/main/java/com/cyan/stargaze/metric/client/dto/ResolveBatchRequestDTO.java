package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 批量解析指标请求。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResolveBatchRequestDTO {

    /** 数据集编码(可选,用于多数据集绑定时显式指定) */
    private String datasetCode;

    /** 指标业务编码列表 */
    private List<String> metricCodes;
}
