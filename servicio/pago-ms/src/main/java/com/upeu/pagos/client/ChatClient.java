package com.upeu.pagos.client;

import com.upeu.pagos.dto.ComprobantePagoRequest;
import com.upeu.pagos.dto.ComprobantePagoResponse;
import com.upeu.pagos.dto.MensajeVentaValidadaRequest;
import com.upeu.pagos.dto.MensajeVentaValidadaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CHAT-MS", path = "/api/v1/chats")
@Deprecated(forRemoval = false)
public interface ChatClient {

    @PostMapping("/comprobantes")
    ComprobantePagoResponse createReceipt(@RequestBody ComprobantePagoRequest request);

    @PostMapping("/mensaje-venta-validada")
    MensajeVentaValidadaResponse createValidatedSaleMessage(@RequestBody MensajeVentaValidadaRequest request);
}
