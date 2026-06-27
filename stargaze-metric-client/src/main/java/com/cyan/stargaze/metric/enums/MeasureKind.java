package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 度量聚合方式。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MeasureKind {
    SUM("SUM"),
    AVG("AVG"),
    COUNT("COUNT"),
    DISTINCT_COUNT("DISTINCT_COUNT"),
    MAX("MAX"),
    MIN("MIN"),
    /** 表达式(派生指标,DSL 自定义) */
    EXPR("EXPR");

    @EnumValue
    private final String code;

    @JsonValue
    public String toJsonValue() {
        String[] parts = code.split("_");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i].substring(0, 1));
            sb.append(parts[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    @JsonCreator
    public static MeasureKind fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (MeasureKind value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
