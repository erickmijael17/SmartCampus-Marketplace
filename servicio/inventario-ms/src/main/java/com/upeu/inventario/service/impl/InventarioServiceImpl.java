package com.upeu.inventario.service.impl;

import com.upeu.inventario.dto.InventarioRequest;
import com.upeu.inventario.dto.InventarioResponse;
import com.upeu.inventario.entity.Inventario;
import com.upeu.inventario.exception.ResourceNotFoundException;
import com.upeu.inventario.mapper.InventarioMapper;
import com.upeu.inventario.repository.InventarioRepository;
import com.upeu.inventario.service.InventarioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;

    @Override
    @Transactional
    public InventarioResponse create(InventarioRequest request) {
        Inventario inventario = inventarioMapper.toEntity(request);
        return inventarioMapper.toResponse(inventarioRepository.save(inventario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> findAll() {
        return inventarioRepository.findAll().stream().map(inventarioMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> findByProducto(Long idProducto) {
        return inventarioRepository.findByIdProducto(idProducto).stream().map(inventarioMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse findById(Long id) {
        return inventarioMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional
    public InventarioResponse update(Long id, InventarioRequest request) {
        Inventario entity = getEntity(id);
        inventarioMapper.updateEntity(entity, request);
        return inventarioMapper.toResponse(inventarioRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getEntity(id);
        inventarioRepository.deleteById(id);
    }

    private Inventario getEntity(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario con id " + id + " no encontrado"));
    }
}

