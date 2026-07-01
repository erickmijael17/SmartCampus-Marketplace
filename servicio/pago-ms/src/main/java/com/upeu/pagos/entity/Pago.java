package com.upeu.pagos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_orden", nullable = false)
    private Long idOrden;

    @Column(name = "id_comprador", nullable = false)
    private Long idComprador;

    @Column(name = "id_vendedor")
    private Long idVendedor;

    @Column(name = "publicacion_id")
    private Long publicacionId;

    @Column(name = "titulo_producto", length = 180)
    private String tituloProducto;

    @Column(name = "descripcion_producto", length = 500)
    private String descripcionProducto;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "referencia_transaccion", length = 80)
    private String referenciaTransaccion;

    @Column(name = "mp_preference_id", length = 120)
    private String mpPreferenceId;

    @Column(name = "mp_payment_id", length = 80)
    private String mpPaymentId;

    @Column(name = "mp_status", length = 50)
    private String mpStatus;

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @Column(name = "external_reference", length = 120)
    private String externalReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

