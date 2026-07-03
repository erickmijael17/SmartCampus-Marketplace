package com.upeu.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "auth-ms", url = "http://localhost:18080")
public interface AuthClient {

    @GetMapping("/auth/public-profile/{userId}")
    Map<String, Object> getPublicProfile(@PathVariable("userId") Long userId);
    
    @GetMapping("/auth/user-info")
    Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token);
}
