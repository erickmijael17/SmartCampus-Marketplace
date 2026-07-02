package com.upeu.chat.controller;

import com.upeu.chat.dto.ComprobantePagoRequest;
import com.upeu.chat.dto.ComprobantePagoResponse;
import com.upeu.chat.dto.ConversacionRequest;
import com.upeu.chat.dto.ConversacionResponse;
import com.upeu.chat.dto.MensajeRequest;
import com.upeu.chat.dto.MensajeResponse;
import com.upeu.chat.dto.MensajeVentaValidadaRequest;
import com.upeu.chat.dto.MensajeVentaValidadaResponse;
import com.upeu.chat.service.ChatService;
import com.upeu.chat.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MensajeService mensajeService;

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

    @PostMapping("/comprobantes")
    public ResponseEntity<ComprobantePagoResponse> createReceipt(@RequestBody ComprobantePagoRequest request) {
        return ResponseEntity.ok(chatService.crearComprobante(request));
    }

    @PostMapping("/mensaje-venta-validada")
    public ResponseEntity<MensajeVentaValidadaResponse> createValidatedSaleMessage(
            @RequestBody MensajeVentaValidadaRequest request) {
        return ResponseEntity.ok(chatService.crearMensajeVentaValidada(request));
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

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<MensajeResponse>> findMessages(@PathVariable Long id) {
        return ResponseEntity.ok(mensajeService.findByConversacionId(id));
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<MensajeResponse> createMessage(
            @PathVariable Long id,
            @RequestBody MensajeRequest request) {
        return ResponseEntity.ok(mensajeService.create(id, request));
    }
}
