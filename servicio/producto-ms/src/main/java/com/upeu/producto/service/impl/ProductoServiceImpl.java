package com.upeu.producto.service.impl;

import com.upeu.producto.client.AuthClient;
import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;
import com.upeu.producto.entity.Producto;
import com.upeu.producto.exception.ResourceNotFoundException;
import com.upeu.producto.mapper.ProductoMapper;
import com.upeu.producto.repository.ProductoRepository;
import com.upeu.producto.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.upeu.producto.client.CategoriaClient;
import com.upeu.producto.dto.CategoriaDto;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CategoriaClient categoriaClient;
    private final AuthClient authClient;

    @Override
    @Transactional
    public ProductoResponse create(ProductoRequest request, String token) {
        log.info("Iniciando creacion de producto con titulo: {} y idCategoria: {}", request.getTitulo(),
                request.getIdCategoria());
        Long localId = getLocalUserId(token);
        Producto producto = productoMapper.toEntity(request);
        producto.setIdVendedor(localId);
        Producto savedProducto = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {} por vendedor: {}", savedProducto.getId(), localId);
        return productoMapper.toResponse(savedProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> findAll() {
        log.info("Recuperando lista de productos");
        List<ProductoResponse> productos = productoRepository.findAll()
                .stream()
                .map(productoMapper::toResponse)
                .toList();
        log.info("Se encontraron {} productos", productos.size());
        return productos;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse findById(Long id) {
        log.info("Buscando producto con ID: {}", id);
        Producto producto = getProductoById(id);
        log.info("Producto encontrado: {} (ID: {})", producto.getTitulo(), id);
        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse update(Long id, ProductoRequest request, String token) {
        log.info("Iniciando actualizacion de producto ID: {}", id);
        Producto producto = getProductoById(id);
        checkOwnershipOrAdmin(producto, token);
        productoMapper.updateEntityFromRequest(producto, request);
        Producto updatedProducto = productoRepository.save(producto);
        log.info("Producto ID: {} actualizado exitosamente", id);
        return productoMapper.toResponse(updatedProducto);
    }

    @Override
    @Transactional
    public void delete(Long id, String token) {
        log.info("Iniciando eliminacion de producto ID: {}", id);
        Producto producto = getProductoById(id);
        checkOwnershipOrAdmin(producto, token);
        productoRepository.deleteById(id);
        log.info("Producto ID: {} eliminado exitosamente", id);
    }

    private Producto getProductoById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado: ID {}", id);
                    return new ResourceNotFoundException("Producto con id " + id + " no encontrado");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse findDetalleById(Long id) {
        log.info("Buscando detalle de producto con ID: {}", id);

        Producto producto = getProductoById(id);

        CategoriaDto categoria = null;

        try {
            categoria = categoriaClient.findCategoriaById(producto.getIdCategoria());
        } catch (Exception e) {
            log.warn("No se pudo obtener la categoria desde categoria-ms. idCategoria={}", producto.getIdCategoria(), e);
        }

        return ProductoResponse.builder()
                .id(producto.getId())
                .titulo(producto.getTitulo())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .moneda(producto.getMoneda())
                .estado(producto.getEstado())
                .idCategoria(producto.getIdCategoria())
                .idVendedor(producto.getIdVendedor())
                .publicadoEn(producto.getPublicadoEn())
                .actualizadoEn(producto.getActualizadoEn())
                .categoria(categoria)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Long getLocalUserId(String token) {
        try {
            Map<String, Object> userInfo = authClient.getUserInfo("Bearer " + token);
            Object localIdObj = userInfo.get("localId");
            if (localIdObj instanceof Number) {
                return ((Number) localIdObj).longValue();
            }
            throw new RuntimeException("No se pudo obtener el localId del usuario");
        } catch (Exception e) {
            log.error("Error al obtener informacion del usuario desde auth-ms", e);
            throw new RuntimeException("Error de autenticacion: no se puede verificar el usuario", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkOwnershipOrAdmin(Producto producto, String token) {
        try {
            Map<String, Object> userInfo = authClient.getUserInfo("Bearer " + token);
            Object localIdObj = userInfo.get("localId");
            List<String> roles = (List<String>) userInfo.get("roles");

            Long currentLocalId = localIdObj instanceof Number ? ((Number) localIdObj).longValue() : null;

            boolean isAdmin = roles != null && roles.contains("ADMIN");
            boolean isOwner = currentLocalId != null && currentLocalId.equals(producto.getIdVendedor());

            if (isAdmin) {
                return;
            }

            if (isOwner) {
                return;
            }

            throw new SecurityException("No tienes permiso para modificar este producto");
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al verificar permisos", e);
            throw new RuntimeException("Error de autorizacion", e);
        }
    }
}
