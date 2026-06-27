package com.cyan.stargaze.metric.adapter.metric.http;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyan.arch.common.api.Response;
import com.cyan.employee.login.filter.UserContextHolder;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricDetailDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricListItemDTO;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.BindableSourceDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.PageDTO;
import com.cyan.stargaze.metric.client.dto.PreviewRequestDTO;
import com.cyan.stargaze.metric.client.dto.PreviewResponseDTO;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.enums.MetricSourceType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 指标控制器(/api/v1/metrics)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;
    private final MetricRepository metricRepository;
    private final MetricAdapterConvert adapterConvert;

    @GetMapping
    public Response<PageDTO<MetricListItemDTO>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "folder", required = false) String folder) {
        PageDTO<MetricDTO> result = metricService.list(page, size, keyword, status, folder);
        List<MetricListItemDTO> items = adapterConvert.toListItemList(result.getData());
        return Response.success(new PageDTO<MetricListItemDTO>()
                .setData(items)
                .setTotal(result.getTotal())
                .setPage(result.getPage())
                .setSize(result.getSize()));
    }

    @GetMapping("/{metricCode}")
    public Response<MetricDetailDTO> findByCode(@PathVariable String metricCode) {
        return Response.success(adapterConvert.toDetail(metricService.findByCode(metricCode)));
    }

    @GetMapping("/bindable-sources")
    public Response<List<BindableSourceDTO>> bindableSources(
            @RequestParam("sourceType") String sourceType) {
        MetricSourceType type = MetricSourceType.fromCode(sourceType);
        if (type == null) {
            return Response.success(List.of());
        }
        return Response.success(metricService.bindableSources(type));
    }

    @PostMapping
    public Response<MetricDetailDTO> create(@RequestBody @Valid MetricCmd cmd) {
        String passport = currentPassport();
        cmd.setCreatedBy(passport);
        cmd.setUpdatedBy(passport);
        return Response.success(adapterConvert.toDetail(metricService.create(cmd)));
    }

    @PutMapping("/{metricCode}")
    public Response<MetricDetailDTO> update(@PathVariable String metricCode,
                                            @RequestBody @Valid MetricCmd cmd) {
        cmd.setUpdatedBy(currentPassport());
        return Response.success(adapterConvert.toDetail(metricService.update(metricCode, cmd)));
    }

    @DeleteMapping("/{metricCode}")
    public Response<Void> delete(@PathVariable String metricCode) {
        metricService.delete(metricCode);
        return Response.success();
    }

    @PostMapping("/{metricCode}/publish")
    public Response<MetricDetailDTO> publish(@PathVariable String metricCode) {
        return Response.success(adapterConvert.toDetail(metricService.publish(metricCode)));
    }

    @PostMapping("/{metricCode}/offline")
    public Response<MetricDetailDTO> offline(@PathVariable String metricCode) {
        return Response.success(adapterConvert.toDetail(metricService.offline(metricCode)));
    }

    @PostMapping("/{metricCode}/preview")
    public Response<PreviewResponseDTO> preview(@PathVariable String metricCode,
                                                @RequestBody @Valid PreviewRequestDTO request) {
        return Response.success(metricService.preview(metricCode, request));
    }

    @GetMapping("/folders")
    public Response<List<String>> listFolders() {
        return Response.success(metricRepository.listDistinctFolders());
    }

    @GetMapping("/check-name")
    public Response<CheckNameResultDTO> checkName(
            @RequestParam("name") String name,
            @RequestParam(value = "excludeMetricCode", required = false) String excludeMetricCode) {
        return Response.success(metricService.checkName(name, excludeMetricCode));
    }

    @GetMapping("/check-code")
    public Response<CheckNameResultDTO> checkCode(
            @RequestParam("code") String code,
            @RequestParam(value = "excludeMetricCode", required = false) String excludeMetricCode) {
        return Response.success(metricService.checkCode(code, excludeMetricCode));
    }

    private String currentPassport() {
        return UserContextHolder.getCurrentEmployee().getPassport();
    }
}
