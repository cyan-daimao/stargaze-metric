package com.cyan.stargaze.metric.client;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.ResolveBatchRequestDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 指标平台 RPC 契约(服务间调用)。
 * <p>
 * 路径统一 /rpc/v1/metric,供 query/dashboard 依赖。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "stargaze-metric", contextId = "metricClient", path = "/rpc/v1/metric", url = "${feign.stargaze-metric.url:}")
public interface MetricClient {

    /**
     * 解析指标 AST(纯函数,query 编译期调用)
     */
    @GetMapping("/{metricCode}/resolve")
    Response<MetricResolveDTO> resolve(@PathVariable("metricCode") String metricCode,
                                       @RequestParam(value = "datasetCode", required = false) String datasetCode);

    /**
     * 列出已发布指标
     */
    @GetMapping("/list")
    Response<List<MetricDTO>> list();

    /**
     * 校验指标×维度组合合法性
     */
    @GetMapping("/validate")
    Response<ValidationResultDTO> validate(@RequestParam("metricCodes") List<String> metricCodes,
                                           @RequestParam("dimCodes") List<String> dimCodes);

    /**
     * 指标是否存在且已发布
     */
    @GetMapping("/{metricCode}/exists")
    Response<Boolean> exists(@PathVariable("metricCode") String metricCode);

    /**
     * 批量解析指标 AST
     */
    @PostMapping("/resolve-batch")
    Response<List<MetricResolveDTO>> resolveBatch(@RequestBody ResolveBatchRequestDTO request);
}
