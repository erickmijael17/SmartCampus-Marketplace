package com.upeu.inventario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}

