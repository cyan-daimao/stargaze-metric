package com.cyan.stargaze.metric.adapter.metric.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.client.MetricClient;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.ResolveBatchRequestDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指标 RPC 控制器:实现 {@link MetricClient} 契约(/rpc/v1/metric)。
 * <p>
 * 供 query/dashboard 依赖。resolve 为纯函数,query 编译期调用。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metric")
@RequiredArgsConstructor
public class MetricRpcController implements MetricClient {

    private final MetricService metricService;
    private final MetricRepository metricRepository;

    @Override
    public Response<MetricResolveDTO> resolve(String metricCode, String datasetCode) {
        return Response.success(metricService.resolve(metricCode, datasetCode));
    }

    @Override
    public Response<List<MetricDTO>> list() {
        return Response.success(metricRepository.list(MetricStatus.PUBLISHED).stream()
                .map(m -> new MetricDTO()
                        .setMetricCode(m.getMetricCode())
                        .setName(m.getName())
                        .setCode(m.getCode())
                        .setSourceType(m.getSourceType())
                        .setSourceCode(m.getSourceCode())
                        .setSourceName(m.getSourceName())
                        .setQueryMode(m.getQueryMode())
                        .setFreshness(m.getFreshness())
                        .setDslKind(m.getDslKind())
                        .setDsl(m.getDsl())
                        .setStatus(m.getStatus()))
                .toList());
    }

    @Override
    public Response<ValidationResultDTO> validate(List<String> metricCodes, List<String> dimCodes) {
        return Response.success(metricService.validate(metricCodes, dimCodes));
    }

    @Override
    public Response<Boolean> exists(String metricCode) {
        Metric metric = metricRepository.findByMetricCode(metricCode);
        return Response.success(metric != null && metric.isPublished());
    }

    @Override
    public Response<List<MetricResolveDTO>> resolveBatch(ResolveBatchRequestDTO request) {
        return Response.success(metricService.resolveBatch(request));
    }
}
