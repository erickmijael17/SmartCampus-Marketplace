package com.upeu.producto.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "auth-ms", contextId = "auth")
public interface AuthClient {

    @GetMapping("/auth/user-info")
    Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token);
}
