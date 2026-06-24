package com.upeu.chat.controller;

import com.upeu.chat.dto.ConversacionRequest;
import com.upeu.chat.dto.ConversacionResponse;
import com.upeu.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK - Chat Service is up and running via Gateway!");
    }

    @GetMapping
    public ResponseEntity<List<ConversacionResponse>> findAll() {
        return ResponseEntity.ok(chatService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversacionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ConversacionResponse> create(@RequestBody ConversacionRequest request) {
        return ResponseEntity.ok(chatService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConversacionResponse> update(@PathVariable Long id, @RequestBody ConversacionRequest request) {
        return ResponseEntity.ok(chatService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chatService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
