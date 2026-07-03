package com.upeu.pagos.client;

import com.upeu.pagos.dto.ActualizarEstadoOrdenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ORDEN-MS", path = "/api/v1/ordenes")
public interface OrdenClient {

    @PatchMapping("/{id}/estado")
    Object updateEstado(@PathVariable("id") Long id, @RequestBody ActualizarEstadoOrdenRequest request);
}
