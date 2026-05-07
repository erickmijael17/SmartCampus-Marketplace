# CampusMarket - Arquitectura de Microservicios

## 1. Vision General Del Sistema
CampusMarket es un mercado digital universitario donde estudiantes publican, buscan y compran productos. La arquitectura se separa en 6 microservicios de negocio:

1. producto
2. catalogo
3. carrito
4. ordenes
5. pagos
6. inventario

Objetivo general:
- Implementar una plataforma modular, mantenible y escalable para gestionar el ciclo completo de compra dentro del campus.

Alcance:
- Se modela solo logica de negocio.
- No se incluye autenticacion, registro de usuarios, seguridad ni infraestructura adicional en esta documentacion funcional.

---

## 2. Estructura General Del Proyecto

### 2.1 Estructura Global
```text
ProyectoMS2026/
  infra/
    config-repo/
      catalogo-dev.yml
      catalogo-prod.yml
      producto-dev.yml
      producto-prod.yml
      carrito-dev.yml
      carrito-prod.yml
      ordenes-dev.yml
      ordenes-prod.yml
      pagos-dev.yml
      pagos-prod.yml
      inventario-dev.yml
      inventario-prod.yml
      gateway-dev.yml
      gateway-prod.yml
      registry-server-dev.yml
      registry-server-prod.yml
    config-server/
    registry-server/
    gateway/
    docker-compose.yml

  servicio/
    catalogo/
    producto/
    carrito/
    ordenes/
    pagos/
    inventario/
    CampusMarket-Documentacion.md
```

### 2.2 Estructura Interna Recomendada Por Microservicio
```text
microservicio/
  src/main/java/com/upeu/<servicio>/
    controller/
    service/
      impl/
    repository/
    entity/
    dto/
    mapper/
    config/
    client/
    exception/
    filter/
  src/main/resources/
    application.yml
    db/migration/
  Dockerfile
  docker-compose-dev.yml
  docker-compose.yml
  .env
  .env.example
  pom.xml
  README.md
```

### 2.3 Funcion De Cada Carpeta
- controller: expone endpoints REST y valida entrada.
- service: define contratos de negocio.
- service/impl: implementa reglas de negocio.
- repository: acceso a BD con JPA.
- entity: mapeo ORM de tablas.
- dto: objetos de entrada/salida para API.
- mapper: conversion entre entidad y DTO.
- config: configuraciones tecnicas (OpenAPI, beans).
- client: clientes Feign para llamadas entre micros.
- exception: errores de negocio y manejo global.
- filter: correlacion de trazas para observabilidad.

---

## 3. Microservicio Por Microservicio

### 3.1 producto
Proposito:
- Gestionar publicaciones de productos de estudiantes vendedores.

Funcionalidades:
- CRUD de productos.
- Consulta de detalle de producto con categoria.

Entidades:
- Producto.

Relaciones logicas:
- Usa catalogo para obtener datos de categoria.
- Es fuente para carrito e inventario.

Estructura interna:
- controller: ProductoController.
- service: ProductoService.
- repository: ProductoRepository.
- entity: Producto.
- dto: ProductoRequest, ProductoResponse, CategoriaDto.
- client: CatalogoClient.

### 3.2 catalogo
Proposito:
- Administrar categorias y clasificacion funcional del marketplace.

Funcionalidades:
- CRUD de categorias.

Entidades:
- Categoria.

Relaciones logicas:
- Producto referencia idCategoria.

Estructura interna:
- controller: CategoriaController.
- service: CategoriaService.
- repository: CategoriaRepository.
- entity: Categoria.
- dto: CategoriaRequest, CategoriaResponse.

### 3.3 carrito
Proposito:
- Mantener seleccion temporal de compra por estudiante.

Funcionalidades:
- Agregar item al carrito.
- Listar carrito por comprador.
- Actualizar y eliminar item.

Entidades:
- Carrito.

Relaciones logicas:
- Consulta producto para validar existencia.
- Provee insumo para ordenes.

Estructura interna:
- controller: CarritoController.
- service: CarritoService.
- repository: CarritoRepository.
- entity: Carrito.
- dto: CarritoRequest, CarritoResponse.
- client: ProductoClient.

### 3.4 ordenes
Proposito:
- Confirmar compras y registrar ordenes.

Funcionalidades:
- Crear orden a partir de carrito.
- Consultar ordenes por comprador.
- Actualizar estado de orden.

Entidades:
- Orden.

Relaciones logicas:
- Consume carrito para tomar items.
- Invoca pagos para transaccion.
- Invoca inventario para reservar/descontar stock.

Estructura interna:
- controller: OrdenController.
- service: OrdenService.
- repository: OrdenRepository.
- entity: Orden.
- dto: OrdenRequest, OrdenResponse.

### 3.5 pagos
Proposito:
- Registrar transacciones de pago asociadas a orden.

Funcionalidades:
- Crear pago por orden.
- Consultar pagos por comprador.
- Actualizar estado de pago.

Entidades:
- Pago.

Relaciones logicas:
- Recibe idOrden desde ordenes.
- Devuelve estado APROBADO o RECHAZADO.

Estructura interna:
- controller: PagoController.
- service: PagoService.
- repository: PagoRepository.
- entity: Pago.
- dto: PagoRequest, PagoResponse.

### 3.6 inventario
Proposito:
- Controlar disponibilidad por producto.

Funcionalidades:
- CRUD de inventario por producto.
- Consultar stock por producto.
- Ajustar stock disponible/reservado.

Entidades:
- Inventario.

Relaciones logicas:
- Producto aporta idProducto.
- Ordenes descuenta o reserva stock.

Estructura interna:
- controller: InventarioController.
- service: InventarioService.
- repository: InventarioRepository.
- entity: Inventario.
- dto: InventarioRequest, InventarioResponse.

---

## 4. Base De Datos Por Microservicio

### 4.1 producto
Tabla: productos
- id (PK)
- titulo
- descripcion
- precio
- moneda
- estado
- id_categoria
- id_vendedor
- publicado_en
- actualizado_en

Por que existe:
- Almacena publicaciones comerciales hechas por estudiantes.

### 4.2 catalogo
Tabla: categorias
- id (PK)
- codigo (UNIQUE)
- nombre
- descripcion
- activo
- created_at
- updated_at

Por que existe:
- Centraliza clasificacion para facilitar busqueda y filtrado.

### 4.3 carrito
Tabla: carritos
- id (PK)
- id_comprador
- id_producto
- cantidad
- precio_unitario
- estado
- created_at

Por que existe:
- Mantiene seleccion temporal antes de confirmar compra.

### 4.4 ordenes
Tabla: ordenes
- id (PK)
- id_comprador
- id_producto
- cantidad
- precio_unitario
- estado
- created_at

Por que existe:
- Guarda compra confirmada como transaccion de negocio.

### 4.5 pagos
Tabla: pagos
- id (PK)
- id_orden
- id_comprador
- monto
- metodo_pago
- estado
- referencia_transaccion
- created_at

Por que existe:
- Mantiene trazabilidad financiera por orden.

### 4.6 inventario
Tabla: inventarios
- id (PK)
- id_producto (UNIQUE)
- stock_disponible
- stock_reservado
- estado
- updated_at

Por que existe:
- Controla existencia real para evitar sobreventa.

Relaciones logicas entre microservicios:
- producto.id_categoria -> catalogo.id
- carrito.id_producto -> producto.id
- ordenes.id_producto -> producto.id
- pagos.id_orden -> ordenes.id
- inventarios.id_producto -> producto.id

Nota:
- Se manejan como referencias logicas por id para mantener independencia de BD por microservicio.

---

## 5. API REST (Resumen Con Ejemplos)

### 5.1 producto
- POST /api/v1/productos
Request:
```json
{
  "titulo": "Calculadora Cientifica",
  "descripcion": "Modelo FX-991ES para cursos de ingenieria",
  "precio": 95.50,
  "moneda": "PEN",
  "estado": "DISPONIBLE",
  "idCategoria": 1,
  "idVendedor": 1001
}
```
Response:
```json
{
  "id": 1,
  "titulo": "Calculadora Cientifica",
  "descripcion": "Modelo FX-991ES para cursos de ingenieria",
  "precio": 95.50,
  "moneda": "PEN",
  "estado": "DISPONIBLE",
  "idCategoria": 1,
  "idVendedor": 1001
}
```

- GET /api/v1/productos
- GET /api/v1/productos/{id}
- PUT /api/v1/productos/{id}
- DELETE /api/v1/productos/{id}
- GET /api/v1/productos/detalle/{id}

### 5.2 catalogo
- POST /api/v1/categorias
Request:
```json
{
  "codigo": "TEC",
  "nombre": "Tecnologia",
  "descripcion": "Accesorios y dispositivos",
  "activo": true
}
```
Response:
```json
{
  "id": 1,
  "codigo": "TEC",
  "nombre": "Tecnologia",
  "descripcion": "Accesorios y dispositivos",
  "activo": true
}
```

- GET /api/v1/categorias
- GET /api/v1/categorias/{id}
- PUT /api/v1/categorias/{id}
- DELETE /api/v1/categorias/{id}

### 5.3 carrito
- POST /api/v1/carritos
Request:
```json
{
  "idComprador": 2001,
  "idProducto": 1,
  "cantidad": 2,
  "precioUnitario": 95.50,
  "estado": "ACTIVO"
}
```
Response:
```json
{
  "id": 10,
  "idComprador": 2001,
  "idProducto": 1,
  "cantidad": 2,
  "precioUnitario": 95.50,
  "estado": "ACTIVO"
}
```

- GET /api/v1/carritos
- GET /api/v1/carritos/{id}
- GET /api/v1/carritos/usuario/{idComprador}
- PUT /api/v1/carritos/{id}
- DELETE /api/v1/carritos/{id}

### 5.4 ordenes
- POST /api/v1/ordenes
Request:
```json
{
  "idComprador": 2001,
  "idProducto": 1,
  "cantidad": 2,
  "precioUnitario": 95.50,
  "estado": "PENDIENTE_PAGO"
}
```
Response:
```json
{
  "id": 500,
  "idComprador": 2001,
  "idProducto": 1,
  "cantidad": 2,
  "precioUnitario": 95.50,
  "estado": "PENDIENTE_PAGO"
}
```

- GET /api/v1/ordenes
- GET /api/v1/ordenes/{id}
- GET /api/v1/ordenes/usuario/{idComprador}
- PUT /api/v1/ordenes/{id}
- DELETE /api/v1/ordenes/{id}

### 5.5 pagos
- POST /api/v1/pagos
Request:
```json
{
  "idComprador": 2001,
  "idOrden": 500,
  "monto": 191.00,
  "metodoPago": "YAPE",
  "estado": "APROBADO",
  "referenciaTransaccion": "TXN-2026-00001"
}
```
Response:
```json
{
  "id": 900,
  "idComprador": 2001,
  "idOrden": 500,
  "monto": 191.00,
  "metodoPago": "YAPE",
  "estado": "APROBADO",
  "referenciaTransaccion": "TXN-2026-00001"
}
```

- GET /api/v1/pagos
- GET /api/v1/pagos/{id}
- GET /api/v1/pagos/usuario/{idComprador}
- PUT /api/v1/pagos/{id}
- DELETE /api/v1/pagos/{id}

### 5.6 inventario
- POST /api/v1/inventarios
Request:
```json
{
  "idProducto": 1,
  "stockDisponible": 40,
  "stockReservado": 3,
  "estado": "ACTIVO"
}
```
Response:
```json
{
  "id": 300,
  "idProducto": 1,
  "stockDisponible": 40,
  "stockReservado": 3,
  "estado": "ACTIVO"
}
```

- GET /api/v1/inventarios
- GET /api/v1/inventarios/{id}
- GET /api/v1/inventarios/producto/{idProducto}
- PUT /api/v1/inventarios/{id}
- DELETE /api/v1/inventarios/{id}

---

## 6. Flujo Completo Del Sistema

1. Publicacion de producto:
- Vendedor registra producto en producto.
- Producto queda vinculado a una categoria de catalogo.

2. Visualizacion en catalogo:
- Comprador navega listado de productos por categoria.

3. Agregar al carrito:
- Comprador agrega producto en carrito con cantidad y precio referencial.

4. Generar orden:
- Ordenes toma informacion seleccionada y crea orden en estado PENDIENTE_PAGO.

5. Procesar pago:
- Pagos registra transaccion para la orden.
- Si aprobado, orden cambia a PAGADO.

6. Actualizar inventario:
- Inventario descuenta stockDisponible o mueve a stockReservado segun regla.

---

## 7. Comunicacion Entre Microservicios

### 7.1 Llamadas recomendadas
- producto -> catalogo: validar/consultar categoria.
- carrito -> producto: validar existencia y estado del producto.
- ordenes -> carrito: obtener items seleccionados.
- ordenes -> pagos: solicitar procesamiento de pago.
- ordenes -> inventario: reservar o descontar stock.

### 7.2 Datos intercambiados
- Identificadores: idProducto, idCategoria, idOrden, idComprador.
- Datos de negocio: cantidad, precio, monto, estado de pago, estado de stock.

### 7.3 Sincrono vs Asincrono
Sincrono (REST/Feign) cuando:
- se requiere respuesta inmediata para continuar flujo (ejemplo: validar producto antes de crear carrito).

Asincrono (eventos) cuando:
- se actualizan procesos posteriores (ejemplo: pago_aprobado, orden_confirmada, inventario_actualizado) sin bloquear usuario.

Recomendacion academica:
- iniciar con sincrono para simplicidad.
- evolucionar a asincrono para resiliencia y desacoplamiento.

---

## 8. Documentacion Tecnica Formal

### 8.1 Objetivo General
Disenar e implementar una arquitectura de microservicios para un marketplace universitario con separacion clara de responsabilidades y ciclo de compra completo.

### 8.2 Objetivos Especificos
- producto: publicar y administrar productos de estudiantes.
- catalogo: clasificar productos por categorias.
- carrito: consolidar seleccion temporal del comprador.
- ordenes: formalizar la compra.
- pagos: registrar y controlar transacciones.
- inventario: mantener disponibilidad real de productos.

### 8.3 Responsabilidades
- Cada microservicio posee su propia base de datos y su propia logica.
- Las interacciones se hacen por API entre servicios.

### 8.4 Reglas De Negocio Principales
- Un producto debe tener categoria valida.
- No se debe confirmar orden sin stock suficiente.
- Una orden solo pasa a confirmada con pago aprobado.
- El inventario se ajusta luego de la confirmacion de pago.

### 8.5 Supuestos Y Decisiones De Diseno
- IDs compartidos son referencias logicas, no llaves foraneas fisicas entre BDs.
- Estado de entidades se maneja por campos string (ejemplo: ACTIVO, PENDIENTE_PAGO, APROBADO).
- DTOs se usan para desacoplar contrato API del modelo persistente.
- Manejo global de excepciones para respuestas consistentes.

---

## 9. Redaccion Para Informe O Exposicion
CampusMarket resuelve la necesidad de intercambio comercial dentro del campus universitario mediante una plataforma distribuida basada en microservicios. La solucion se divide en seis componentes especializados: productos, catalogo, carrito, ordenes, pagos e inventario. Esta division permite que cada dominio evolucione de forma independiente, reduzca acoplamiento y facilite mantenimiento.

La eleccion de esta arquitectura responde a criterios de escalabilidad, claridad organizacional y alineacion con buenas practicas modernas de backend. Cada microservicio atiende un problema concreto del negocio: publicar articulos, clasificarlos, gestionar seleccion de compra, formalizar la transaccion, registrar el pago y controlar stock. De esta manera, el sistema cubre de extremo a extremo el flujo de compra dentro del campus, con una base tecnica apropiada para contexto academico y preprofesional.

Beneficios principales:
- Mantenibilidad por separacion de responsabilidades.
- Escalabilidad selectiva por dominio.
- Trazabilidad de operaciones y errores.
- Facilidad para pruebas y evolucion del sistema.
- Preparacion para integrar mecanismos avanzados de resiliencia y eventos.

---

## 10. Buenas Practicas Aplicadas
- Estructura por capas y responsabilidades claras.
- Uso de DTOs en entrada y salida.
- Validaciones con Jakarta Validation.
- Manejo global de excepciones con formato uniforme.
- Nombres claros en entidades, endpoints y servicios.
- Migraciones versionadas con Flyway.
- Separacion de configuracion por entorno en config-repo.
- Trazabilidad con X-Trace-ID en filtros HTTP.
