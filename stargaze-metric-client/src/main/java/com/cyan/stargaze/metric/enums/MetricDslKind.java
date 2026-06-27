package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标 DSL 类型。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricDslKind {
    /** 原子指标 */
    ATOMIC("ATOMIC"),
    /** 派生指标 */
    DERIVED("DERIVED"),
    /** 窗口指标 */
    WINDOW("WINDOW"),
    /** API 指标 */
    API_METRIC("API_METRIC");

    @EnumValue
    @JsonValue
    private final String code;

    @JsonCreator
    public static MetricDslKind fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (MetricDslKind value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
