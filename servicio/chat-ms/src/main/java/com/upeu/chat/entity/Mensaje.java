package com.upeu.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mensajes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_conversacion")
    private Long idConversacion;

    @Column(name = "id_remitente")
    private Long idRemitente;

    @Column(name = "receptor_id")
    private Long receptorId;

    @Column(name = "contenido")
    private String contenido;

    @Column(name = "tipo_remitente", length = 30)
    private String tipoRemitente;

    @Column(name = "tipo_mensaje", length = 50)
    private String tipoMensaje;

    @Column(name = "id_orden")
    private Long idOrden;

    @Column(name = "pago_id")
    private Long pagoId;

    @Column(name = "mp_payment_id", length = 80)
    private String mpPaymentId;

    @Column(name = "leido")
    private Boolean leido;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
