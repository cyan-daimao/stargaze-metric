package com.cyan.stargaze.metric.application.source;

import com.cyan.stargaze.metric.client.dto.SourceResolveDTO;

/**
 * 来源解析应用服务。
 * <p>
 * 将 dataset/画像/实时表/API 来源解析为物理执行信息。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface SourceResolverService {

    /**
     * 解析数据集来源。
     *
     * @param datasetCode 数据集编码
     * @return 来源解析结果
     */
    SourceResolveDTO resolveDataset(String datasetCode);

    /**
     * 解析画像特征来源。
     *
     * @param featureCode 特征编码
     * @return 来源解析结果
     */
    SourceResolveDTO resolvePortraitFeature(String featureCode);

    /**
     * 解析实时表来源。
     *
     * @param tableCode 表编码
     * @return 来源解析结果
     */
    SourceResolveDTO resolveRealtimeTable(String tableCode);

    /**
     * 解析 HTTP API 来源。
     *
     * @param apiCode API 编码
     * @return 来源解析结果
     */
    SourceResolveDTO resolveHttpApi(String apiCode);
}
