package com.cyan.stargaze.metric.application.metric.cmd;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标绑定命令。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricBindingCmd {

    /** 指标 ID */
    @NotBlank(message = "指标 ID 不能为空")
    private String metricId;

    /** 数据集 ID */
    @NotBlank(message = "数据集 ID 不能为空")
    private String datasetId;

    /** 字段 ID */
    @NotBlank(message = "字段 ID 不能为空")
    private String fieldId;

    /** 该数据集下 DSL 覆盖 */
    private String dslOverride;
}
