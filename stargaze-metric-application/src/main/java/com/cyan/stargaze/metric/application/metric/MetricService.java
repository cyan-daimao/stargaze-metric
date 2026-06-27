package com.cyan.stargaze.metric.application.metric;

import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.BindableSourceDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.SyncDatasetItemDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncRequestDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncResultDTO;
import com.cyan.stargaze.metric.client.dto.PageDTO;
import com.cyan.stargaze.metric.client.dto.PreviewRequestDTO;
import com.cyan.stargaze.metric.client.dto.PreviewResponseDTO;
import com.cyan.stargaze.metric.client.dto.ResolveBatchRequestDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.enums.MetricSourceType;

import java.util.List;

/**
 * 指标应用服务。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricService {

    MetricDTO create(MetricCmd cmd);

    MetricDTO update(String metricCode, MetricCmd cmd);

    MetricDTO findByCode(String metricCode);

    PageDTO<MetricDTO> list(Integer page, Integer size, String keyword, String status, String folder);

    void delete(String metricCode);

    /** 发布:draft → published,记版本 */
    MetricDTO publish(String metricCode);

    /** 下线:published → offline */
    MetricDTO offline(String metricCode);

    /** 单指标预览(不带维度分组) */
    PreviewResponseDTO preview(String metricCode, PreviewRequestDTO request);

    /** 查询可绑定来源 */
    List<BindableSourceDTO> bindableSources(MetricSourceType sourceType);

    /** 解析指标 AST(供 query 编译期调用) */
    MetricResolveDTO resolve(String metricCode, String datasetCode);

    /** 批量解析指标 AST */
    List<MetricResolveDTO> resolveBatch(ResolveBatchRequestDTO request);

    /** 一键同步:查询可选数据集列表 */
    PageDTO<SyncDatasetItemDTO> syncDatasets(Integer page, Integer size, String keyword, String type);

    /** 一键同步:执行同步 */
    MetricSyncResultDTO sync(MetricSyncRequestDTO request);

    /** 校验指标×维度组合合法性 */
    ValidationResultDTO validate(List<String> metricCodes, List<String> dimCodes);

    /** 按名称查重 */
    CheckNameResultDTO checkName(String name, String excludeMetricCode);

    /** 按标识查重 */
    CheckNameResultDTO checkCode(String code, String excludeMetricCode);
}
