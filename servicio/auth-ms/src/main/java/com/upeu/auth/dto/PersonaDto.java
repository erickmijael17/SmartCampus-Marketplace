package com.upeu.auth.dto;

import com.upeu.auth.entity.Persona.TipoUsuario;
import jakarta.validation.constraints.*;
import lombok.*;

public class PersonaDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank(message = "Los nombres son requeridos")
        @Size(max = 100)
        private String nombres;

        @NotBlank(message = "Los apellidos son requeridos")
        @Size(max = 100)
        private String apellidos;

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no es válido")
        private String email;

        @Size(max = 20)
        private String telefono;

        @Size(max = 20)
        private String codigoUniversitario;

        @NotNull(message = "El tipo de usuario es requerido")
        private TipoUsuario tipoUsuario;

        @Size(max = 100)
        private String carrera;

        @Size(max = 100)
        private String facultad;

        @Size(max = 500)
        private String fotoPerfilUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String userId;
        private String nombres;
        private String apellidos;
        private String email;
        private String telefono;
        private String codigoUniversitario;
        private TipoUsuario tipoUsuario;
        private String carrera;
        private String facultad;
        private String fotoPerfilUrl;
        private Boolean activo;
    }
}
