package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据新鲜度。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum Freshness {
    /** 离线 */
    OFFLINE("OFFLINE"),
    /** 近实时 */
    NEAR_REALTIME("NEAR_REALTIME"),
    /** 实时 */
    REALTIME("REALTIME");

    @EnumValue
    @JsonValue
    private final String code;

    @JsonCreator
    public static Freshness fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (Freshness value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
