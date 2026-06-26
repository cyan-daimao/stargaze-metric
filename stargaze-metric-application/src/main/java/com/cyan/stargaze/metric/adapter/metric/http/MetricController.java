package com.cyan.stargaze.metric.adapter.metric.http;

import com.cyan.arch.common.api.Response;
import com.cyan.employee.client.dto.EmployeeDTO;
import com.cyan.employee.login.filter.UserContextHolder;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricBindingDTO;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricSyncRequestDTO;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.CheckDimensionRequestDTO;
import com.cyan.stargaze.metric.client.dto.CheckDimensionResultDTO;
import com.cyan.stargaze.metric.client.dto.CheckNameResultDTO;
import com.cyan.stargaze.metric.client.dto.DatasetListItemDTO;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricDimensionRefDTO;
import com.cyan.stargaze.metric.client.dto.MetricSyncResultDTO;
import com.cyan.stargaze.metric.client.dto.PageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping
    public Response<MetricDTO> create(@RequestBody @Valid MetricCmd cmd) {
        fillUser(cmd);
        return Response.success(metricService.create(cmd));
    }

    @PutMapping("/{id}")
    public Response<MetricDTO> update(@PathVariable String id, @RequestBody @Valid MetricCmd cmd) {
        cmd.setId(id);
        fillUser(cmd);
        return Response.success(metricService.update(cmd));
    }

    @GetMapping("/{id}")
    public Response<MetricDTO> findById(@PathVariable String id) {
        return Response.success(metricService.findById(id));
    }

    @GetMapping
    public Response<PageDTO<MetricDTO>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "publishedOnly", defaultValue = "false") boolean publishedOnly) {
        if (publishedOnly) {
            return Response.success(new PageDTO<MetricDTO>()
                    .setData(metricService.list(true))
                    .setTotal(0)
                    .setPage(1L)
                    .setSize(20L));
        }
        return Response.success(metricService.list(page, size, keyword, status, folder));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable String id) {
        metricService.delete(id);
        return Response.success();
    }

    @PostMapping("/{id}/publish")
    public Response<MetricDTO> publish(@PathVariable String id) {
        return Response.success(metricService.publish(id));
    }

    @PostMapping("/{id}/offline")
    public Response<MetricDTO> offline(@PathVariable String id) {
        return Response.success(metricService.offline(id));
    }

    @PostMapping("/{id}/deprecate")
    public Response<MetricDTO> deprecate(@PathVariable String id) {
        return Response.success(metricService.deprecate(id));
    }

    // ---- 校验 ----
    @GetMapping("/check-name")
    public Response<CheckNameResultDTO> checkName(
            @RequestParam("name") String name,
            @RequestParam(value = "excludeId", required = false) String excludeId) {
        return Response.success(metricService.checkName(name, excludeId));
    }

    @GetMapping("/check-code")
    public Response<CheckNameResultDTO> checkCode(
            @RequestParam("code") String code,
            @RequestParam(value = "excludeId", required = false) String excludeId) {
        return Response.success(metricService.checkCode(code, excludeId));
    }

    @PostMapping("/check-dimensions")
    public Response<CheckDimensionResultDTO> checkDimensions(@RequestBody @Valid CheckDimensionRequestDTO request) {
        return Response.success(metricService.checkDimensions(request));
    }

    // ---- 一键同步 ----
    @GetMapping("/sync-datasets")
    public Response<PageDTO<DatasetListItemDTO>> listSyncDatasets(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "5") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "datasource", required = false) String datasource) {
        return Response.success(metricService.listSyncDatasets(page, size, keyword, type, datasource));
    }

    @PostMapping("/sync")
    public Response<MetricSyncResultDTO> sync(@RequestBody @Valid MetricSyncRequestDTO request) {
        String createdBy = getCurrentUserId();
        com.cyan.stargaze.metric.client.dto.MetricSyncRequestDTO clientReq =
                new com.cyan.stargaze.metric.client.dto.MetricSyncRequestDTO()
                        .setDatasetId(request.getDatasetId())
                        .setMetricNames(request.getMetricNames());
        return Response.success(metricService.syncFromDataset(clientReq, createdBy));
    }

    // ---- 维度绑定 ----
    @GetMapping("/{id}/dimensions")
    public Response<List<String>> listDimensions(@PathVariable("id") String metricId) {
        MetricDTO dto = metricService.findById(metricId);
        return Response.success(dto.getDimensions().stream().map(MetricDimensionRefDTO::getDimensionId).toList());
    }

    // ---- 数据集绑定 ----
    @PostMapping("/{id}/bindings")
    public Response<MetricBindingDTO> addBinding(@PathVariable("id") String metricId,
                                                 @RequestBody @Valid MetricBindingCmd cmd) {
        cmd.setMetricId(metricId);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricBindingDTO(metricService.addBinding(cmd)));
    }

    @GetMapping("/{id}/bindings")
    public Response<List<MetricBindingDTO>> listBindings(@PathVariable("id") String metricId) {
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricBindingDTOList(metricService.listBindings(metricId)));
    }

    @DeleteMapping("/bindings/{bindingId}")
    public Response<Void> removeBinding(@PathVariable String bindingId) {
        metricService.removeBinding(bindingId);
        return Response.success();
    }

    private void fillUser(MetricCmd cmd) {
        EmployeeDTO employee = UserContextHolder.getCurrentEmployee();
        if (employee != null && employee.getId() != null) {
            cmd.setCreatedBy(employee.getId());
            cmd.setUpdatedBy(employee.getId());
        }
    }

    private String getCurrentUserId() {
        EmployeeDTO employee = UserContextHolder.getCurrentEmployee();
        return employee == null ? null : employee.getId();
    }
}
