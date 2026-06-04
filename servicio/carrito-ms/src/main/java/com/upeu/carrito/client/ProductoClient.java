package com.upeu.carrito.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "producto-ms", contextId = "producto")
public interface ProductoClient {

    @GetMapping("/api/v1/productos/{id}")
    Map<String, Object> findProductoById(@PathVariable("id") Long id);
}
