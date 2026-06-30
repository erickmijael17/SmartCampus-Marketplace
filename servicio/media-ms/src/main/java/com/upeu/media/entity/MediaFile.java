package com.upeu.media.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "archivos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url")
    private String url;

    @Column(name = "nombre_original")
    private String nombreOriginal;

    @Column(name = "nombre_almacen")
    private String nombreAlmacen;

    @Column(name = "ruta")
    private String ruta;

    @Column(name = "tipo_mime")
    private String tipoMime;

    @Column(name = "tamanio")
    private Long tamanoBytes;

    @Column(name = "propietario_id")
    private Long idUploader;

    @Column(name = "entidad_id")
    private Long idPublicacion;

    @Column(name = "entidad_tipo")
    private String entidadTipo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime creadoEn;

    @jakarta.persistence.Transient
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
    }
}
