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
    DATASET("dataset"),
    /** 画像平台-特征 */
    PORTRAIT_FEATURE("portraitFeature"),
    /** 画像平台-标签 */
    PORTRAIT_TAG("portraitTag"),
    /** 画像平台-人群 */
    PORTRAIT_CROWD("portraitCrowd"),
    /** 实时 OLAP 表 */
    REALTIME_TABLE("realtimeTable"),
    /** 受限 HTTP API */
    HTTP_API("httpApi");

    @EnumValue
    @JsonValue
    private final String code;

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
