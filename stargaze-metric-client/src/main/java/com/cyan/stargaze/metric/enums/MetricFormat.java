package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标数据格式。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricFormat {
    /** 数值（默认） */
    NUMBER("number"),
    /** 百分比 */
    PERCENT("percent"),
    /** 货币 */
    CURRENCY("currency"),
    /** 整数 */
    INT("int");

    @EnumValue
    private final String code;
}
