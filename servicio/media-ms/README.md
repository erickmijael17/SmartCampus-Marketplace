# Microservicio Media

Microservicio de almacenamiento y distribucion de archivos multimedia (imagenes de productos, comprobantes de pago, avatares, etc.).

## Responsabilidad
- Carga de imagenes (upload).
- Servir los archivos estaticos almacenados.
- Eliminar o reemplazar archivos.

## Endpoints
- `GET /api/v1/media/health`
- `GET /api/v1/media`
- `GET /api/v1/media/{id}`
- `POST /api/v1/media`
- `POST /api/v1/media/upload` (consumes `multipart/form-data`)
- `GET /api/v1/media/files/{storedName:.+}` (descarga/lectura del archivo real)
- `PUT /api/v1/media/{id}`
- `DELETE /api/v1/media/{id}`

## Dependencias
- Gateway y Eureka
- Config Server
- Base de datos relacional (PostgreSQL en produccion) para metadatos.
- Sistema de archivos para los blobs.
