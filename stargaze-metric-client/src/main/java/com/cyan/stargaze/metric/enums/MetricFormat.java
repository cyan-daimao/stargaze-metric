package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
    NUMBER("NUMBER"),
    /** 百分比 */
    PERCENT("PERCENT"),
    /** 货币 */
    CURRENCY("CURRENCY"),
    /** 整数 */
    INT("INT");

    @EnumValue
    @JsonValue
    private final String code;

    @JsonCreator
    public static MetricFormat fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (MetricFormat value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
