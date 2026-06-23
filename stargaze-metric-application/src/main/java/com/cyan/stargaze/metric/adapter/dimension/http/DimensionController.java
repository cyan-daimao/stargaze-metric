package com.cyan.stargaze.metric.adapter.dimension.http;

import com.cyan.arch.common.api.Response;
import com.cyan.employee.client.dto.EmployeeDTO;
import com.cyan.employee.login.filter.UserContextHolder;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionBindingDTO;
import com.cyan.stargaze.metric.adapter.dimension.http.dto.DimensionDTO;
import com.cyan.stargaze.metric.application.dimension.DimensionService;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
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
 * 维度控制器(/api/dimensions)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/dimensions")
@RequiredArgsConstructor
public class DimensionController {

    private final DimensionService dimensionService;

    @PostMapping
    public Response<DimensionDTO> create(@RequestBody @Valid DimensionCmd cmd) {
        fillUser(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionDTO(dimensionService.create(cmd)));
    }

    @PutMapping("/{id}")
    public Response<DimensionDTO> update(@PathVariable String id, @RequestBody @Valid DimensionCmd cmd) {
        cmd.setId(id);
        fillUser(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionDTO(dimensionService.update(cmd)));
    }

    @GetMapping("/{id}")
    public Response<DimensionDTO> findById(@PathVariable String id) {
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionDTO(dimensionService.findById(id)));
    }

    @GetMapping
    public Response<List<DimensionDTO>> list(@RequestParam("workspaceId") String workspaceId,
                                             @RequestParam(value = "publishedOnly", defaultValue = "false") boolean publishedOnly) {
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionDTOList(dimensionService.list(workspaceId, publishedOnly)));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable String id) {
        dimensionService.delete(id);
        return Response.success();
    }

    @PostMapping("/{id}/publish")
    public Response<DimensionDTO> publish(@PathVariable String id) {
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionDTO(dimensionService.publish(id)));
    }

    @PostMapping("/{id}/bindings")
    public Response<DimensionBindingDTO> addBinding(@PathVariable("id") String dimensionId,
                                                    @RequestBody @Valid DimensionBindingCmd cmd) {
        cmd.setDimensionId(dimensionId);
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionBindingDTO(dimensionService.addBinding(cmd)));
    }

    @GetMapping("/{id}/bindings")
    public Response<List<DimensionBindingDTO>> listBindings(@PathVariable("id") String dimensionId) {
        return Response.success(MetricAdapterConvert.INSTANCE.toDimensionBindingDTOList(dimensionService.listBindings(dimensionId)));
    }

    @DeleteMapping("/bindings/{bindingId}")
    public Response<Void> removeBinding(@PathVariable String bindingId) {
        dimensionService.removeBinding(bindingId);
        return Response.success();
    }

    private void fillUser(DimensionCmd cmd) {
        EmployeeDTO employee = UserContextHolder.getCurrentEmployee();
        if (employee != null && employee.getId() != null) {
            cmd.setCreatedBy(employee.getId());
        }
    }
}
