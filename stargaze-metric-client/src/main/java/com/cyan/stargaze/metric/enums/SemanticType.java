package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 语义类型(geo/time/category)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SemanticType {
    GEO("geo"),
    TIME("time"),
    CATEGORY("category");

    @EnumValue
    private final String code;
}
