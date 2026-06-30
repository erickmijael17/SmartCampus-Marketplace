package com.upeu.auth.service;

import com.upeu.auth.dto.PersonaDto;
import com.upeu.auth.entity.Persona;
import com.upeu.auth.exception.PersonaNotFoundException;
import com.upeu.auth.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;

    @Transactional(readOnly = true)
    public List<PersonaDto.Response> findAll() {
        return personaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PersonaDto.Response findById(Long id) {
        return personaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PersonaNotFoundException("Persona no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public PersonaDto.Response findByUserId(String userId) {
        return personaRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new PersonaNotFoundException("Persona no encontrada para userId: " + userId));
    }

    @Transactional
    public PersonaDto.Response create(String userId, PersonaDto.Request request) {
        if (personaRepository.existsByUserId(userId)) {
            throw new RuntimeException("Ya existe un perfil para el usuario: " + userId);
        }
        if (personaRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + request.getEmail());
        }

        Persona persona = Persona.builder()
                .userId(userId)
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .codigoUniversitario(request.getCodigoUniversitario())
                .tipoUsuario(request.getTipoUsuario())
                .carrera(request.getCarrera())
                .facultad(request.getFacultad())
                .fotoPerfilUrl(request.getFotoPerfilUrl())
                .activo(true)
                .build();

        return toResponse(personaRepository.save(persona));
    }

    @Transactional
    public PersonaDto.Response createFromJwt(Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String preferredUsername = jwt.getClaimAsString("preferred_username");

        if (personaRepository.existsByUserId(userId)) {
            return personaRepository.findByUserId(userId).map(this::toResponse)
                    .orElseThrow(() -> new RuntimeException("Inconsistencia: userId existe pero no se pudo recuperar"));
        }

        String nombres = name != null ? name : preferredUsername != null ? preferredUsername : "Usuario";
        String emailFinal = email != null ? email : preferredUsername != null ? preferredUsername + "@smartcampus.edu.pe" : "usuario@smartcampus.edu.pe";

        Persona persona = Persona.builder()
                .userId(userId)
                .nombres(nombres)
                .apellidos("")
                .email(emailFinal)
                .tipoUsuario(Persona.TipoUsuario.ESTUDIANTE)
                .activo(true)
                .build();

        return toResponse(personaRepository.save(persona));
    }

    @Transactional
    public PersonaDto.Response update(String userId, PersonaDto.Request request) {
        Persona persona = personaRepository.findByUserId(userId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona no encontrada para userId: " + userId));

        persona.setNombres(request.getNombres());
        persona.setApellidos(request.getApellidos());
        persona.setEmail(request.getEmail());
        persona.setTelefono(request.getTelefono());
        persona.setCodigoUniversitario(request.getCodigoUniversitario());
        persona.setTipoUsuario(request.getTipoUsuario());
        persona.setCarrera(request.getCarrera());
        persona.setFacultad(request.getFacultad());
        persona.setFotoPerfilUrl(request.getFotoPerfilUrl());

        return toResponse(personaRepository.save(persona));
    }

    private PersonaDto.Response toResponse(Persona p) {
        return PersonaDto.Response.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .nombres(p.getNombres())
                .apellidos(p.getApellidos())
                .email(p.getEmail())
                .telefono(p.getTelefono())
                .codigoUniversitario(p.getCodigoUniversitario())
                .tipoUsuario(p.getTipoUsuario())
                .carrera(p.getCarrera())
                .facultad(p.getFacultad())
                .fotoPerfilUrl(p.getFotoPerfilUrl())
                .activo(p.getActivo())
                .build();
    }
}
