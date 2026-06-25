package com.cyan.stargaze.metric.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 跨数据集维度重复检测结果。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CheckDimensionResultDTO {

    /** 重复维度列表 */
    private List<DuplicateDimensionDTO> duplicates;

    /**
     * 重复维度项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class DuplicateDimensionDTO {
        /** 维度名称 */
        private String dimensionName;
        /** 涉及数据集 */
        private List<DatasetDTO> datasets;

        /**
         * 涉及数据集。
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Accessors(chain = true)
        public static class DatasetDTO {
            private String id;
            private String name;
        }
    }
}
