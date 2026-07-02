package com.upeu.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "auth-ms")
public interface AuthClient {

    @GetMapping("/auth/public-profile/{userId}")
    Map<String, Object> getPublicProfile(@PathVariable("userId") Long userId);
}
