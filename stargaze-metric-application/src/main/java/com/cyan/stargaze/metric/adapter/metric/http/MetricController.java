package com.cyan.stargaze.metric.adapter.metric.http;

import com.cyan.arch.common.api.Response;
import com.cyan.employee.client.dto.EmployeeDTO;
import com.cyan.employee.login.filter.UserContextHolder;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.adapter.metric.http.dto.MetricBindingDTO;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.application.metric.cmd.MetricBindingCmd;
import com.cyan.stargaze.metric.application.metric.cmd.MetricCmd;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
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
 * 指标控制器(/api/metrics)。
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
    public Response<List<MetricDTO>> list(@RequestParam("workspaceId") String workspaceId,
                                          @RequestParam(value = "publishedOnly", defaultValue = "false") boolean publishedOnly) {
        return Response.success(metricService.list(workspaceId, publishedOnly));
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

    @PostMapping("/{id}/deprecate")
    public Response<MetricDTO> deprecate(@PathVariable String id) {
        return Response.success(metricService.deprecate(id));
    }

    // ---- 绑定 ----
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
        }
    }
}
