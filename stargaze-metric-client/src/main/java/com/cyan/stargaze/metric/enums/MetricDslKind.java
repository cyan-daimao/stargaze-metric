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
    API_METRIC("API_METRIC"),
    /** 维度字段映射 */
    FIELD("FIELD"),
    /** 时间维度 */
    TIME("TIME"),
    /** 维度映射 */
    MAPPING("MAPPING"),
    /** 画像标签维度 */
    PORTRAIT_TAG("PORTRAIT_TAG"),
    /** API 查询维度 */
    API_LOOKUP("API_LOOKUP");

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
