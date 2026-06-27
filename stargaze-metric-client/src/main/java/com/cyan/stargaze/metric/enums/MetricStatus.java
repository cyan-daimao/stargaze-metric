package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标/维度生命周期状态。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricStatus {
    /** 草稿(可改 DSL) */
    DRAFT("DRAFT"),
    /** 已发布(口径冻结,不可改 DSL) */
    PUBLISHED("PUBLISHED"),
    /** 已下线 */
    OFFLINE("OFFLINE"),
    /** 来源异常 */
    SOURCE_ERROR("SOURCE_ERROR");

    @EnumValue
    @JsonValue
    private final String code;

    @JsonCreator
    public static MetricStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (MetricStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
