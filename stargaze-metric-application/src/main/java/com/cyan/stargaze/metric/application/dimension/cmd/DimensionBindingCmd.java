package com.cyan.stargaze.metric.application.dimension.cmd;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 维度绑定命令。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DimensionBindingCmd {

    /** 维度 ID */
    @NotBlank(message = "维度 ID 不能为空")
    private String dimensionId;

    /** 数据集 ID */
    @NotBlank(message = "数据集 ID 不能为空")
    private String datasetId;

    /** 字段 ID */
    @NotBlank(message = "字段 ID 不能为空")
    private String fieldId;

    /** 维度计算表达式(可空) */
    private String expr;
}
