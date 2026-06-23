package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标/维度引用的物理字段(resolve 返回)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FieldRefDTO {

    /** 字段 ID */
    private String fieldId;

    /** 物理字段名 */
    private String originName;

    /** 数据集 ID */
    private String datasetId;
}
