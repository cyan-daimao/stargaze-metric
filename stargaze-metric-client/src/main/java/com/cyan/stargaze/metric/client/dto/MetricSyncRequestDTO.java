package com.cyan.stargaze.metric.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 一键同步请求。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricSyncRequestDTO {

    /** 数据集 ID */
    @NotBlank(message = "数据集 ID 不能为空")
    private String datasetId;

    /** 指定同步的指标名称列表（为空则同步全部度量字段） */
    private List<String> metricNames;
}
