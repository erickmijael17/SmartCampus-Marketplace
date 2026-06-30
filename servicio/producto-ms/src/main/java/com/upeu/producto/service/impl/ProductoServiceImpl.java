package com.upeu.producto.service.impl;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CategoriaClient categoriaClient;

    @Override
    @Transactional
    public ProductoResponse create(ProductoRequest request) {
        log.info("Iniciando creacion de producto con titulo: {} y idCategoria: {}", request.getTitulo(),
                request.getIdCategoria());
        Producto producto = productoMapper.toEntity(request);
        Producto savedProducto = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", savedProducto.getId());
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
    public ProductoResponse update(Long id, ProductoRequest request) {
        log.info("Iniciando actualizacion de producto ID: {}", id);
        Producto producto = getProductoById(id);
        productoMapper.updateEntityFromRequest(producto, request);
        Producto updatedProducto = productoRepository.save(producto);
        log.info("Producto ID: {} actualizado exitosamente", id);
        return productoMapper.toResponse(updatedProducto);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Iniciando eliminacion de producto ID: {}", id);
        getProductoById(id);
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
    /*
     * @Override
     * 
     * @Transactional(readOnly = true)
     * public ProductoResponse findDetalleById(Integer id) {
     * log.info("Buscando detalle de producto con ID: {}", id);
     * 
     * Producto producto = getProductoById(id);
     * 
     * CategoriaDto categoria = categoriaClient.findCategoriaById(
     * producto.getIdCategoria().longValue());
     * 
     * return ProductoResponse.builder()
     * .id(producto.getId())
     * .nombre(producto.getNombre())
     * .descripcion(producto.getDescripcion())
     * .idCategoria(producto.getIdCategoria())
     * .categoria(categoria)
     * .build();
     * 
     * }
     */
}
