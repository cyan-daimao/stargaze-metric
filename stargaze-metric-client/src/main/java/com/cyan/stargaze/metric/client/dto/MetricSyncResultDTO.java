package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 一键同步结果。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricSyncResultDTO {

    /** 同步的数据集 ID */
    private String datasetId;

    /** 成功创建的指标列表 */
    private List<MetricDTO> created;

    /** 因名称重复跳过的字段 */
    private List<String> skippedDuplicates;

    /** 失败原因 */
    private List<String> errors;
}
