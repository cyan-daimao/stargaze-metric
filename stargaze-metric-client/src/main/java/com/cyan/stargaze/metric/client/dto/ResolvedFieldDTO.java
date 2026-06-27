package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标解析后的物理字段信息(供 query 编译期使用)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResolvedFieldDTO {

    /** 字段 ID */
    private String fieldId;

    /** 物理字段名 */
    private String originName;

    /** 显示别名 */
    private String alias;
}
