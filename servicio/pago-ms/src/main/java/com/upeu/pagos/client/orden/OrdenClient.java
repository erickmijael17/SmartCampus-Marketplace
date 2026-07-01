package com.upeu.pagos.client.orden;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "orden-ms", path = "/api/v1/ordenes")
public interface OrdenClient {

    @GetMapping("/{id}")
    OrdenClientResponse findById(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    OrdenClientResponse update(@PathVariable("id") Long id, @RequestBody OrdenClientRequest request);
}
