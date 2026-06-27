package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 语义类型(GEO/TIME/CATEGORY)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SemanticType {
    GEO("GEO"),
    TIME("TIME"),
    CATEGORY("CATEGORY");

    @EnumValue
    @JsonValue
    private final String code;

    @JsonCreator
    public static SemanticType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (SemanticType value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
