package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标/维度来源类型。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricSourceType {
    /** 数据集 */
    DATASET("DATASET"),
    /** 画像平台-特征 */
    PORTRAIT_FEATURE("PORTRAIT_FEATURE"),
    /** 画像平台-标签 */
    PORTRAIT_TAG("PORTRAIT_TAG"),
    /** 画像平台-人群 */
    PORTRAIT_CROWD("PORTRAIT_CROWD"),
    /** 实时 OLAP 表 */
    REALTIME_TABLE("REALTIME_TABLE"),
    /** 受限 HTTP API */
    HTTP_API("HTTP_API");

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
    public static MetricSourceType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (MetricSourceType value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
