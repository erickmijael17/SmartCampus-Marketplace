# SmartCampus Marketplace

SmartCampus Marketplace es una plataforma de mercado digital universitario para la UPeU, orientada a compra, venta, favoritos, ordenes, pagos, chat, publicaciones y gestion de productos dentro de un entorno academico.

Arquitectura de microservicios con Spring Boot, Spring Cloud Config, Eureka, Spring Cloud Gateway, PostgreSQL, Kafka, observabilidad y Keycloak como proveedor de identidad del realm `smartcampus`.

Documentacion en linea: [erickmijael17.github.io/SmartCampus-Marketplace](https://erickmijael17.github.io/SmartCampus-Marketplace/)

---

## Documentacion

| Documento | Contenido |
|---|---|
| [Sitio MkDocs](https://erickmijael17.github.io/SmartCampus-Marketplace/) | Documentacion renderizada en GitHub Pages |
| [Producto del curso](docs/producto-curso.md) | Definicion U1/U2/U3, evaluaciones y stack |
| [Arquitectura](docs/arquitectura.md) | Diagramas, componentes y flujos del sistema |
| [Frontend Angular](docs/frontend-angular.md) | SPA Angular 20, rutas, guards, servicios y Gateway |
| [Desarrollo DEV](docs/desarrollo.md) | Arranque local paso a paso |
| [Produccion PROD](docs/produccion.md) | Despliegue con Docker Compose |
| [Seguridad](docs/seguridad.md) | Keycloak, JWT, roles y rutas protegidas |
| [Pagos Mercado Pago](docs/pagos-mercadopago.md) | Checkout, validacion manual, webhook y chat |
| [Observabilidad](docs/observabilidad.md) | Actuator, Prometheus, Loki, Promtail y Grafana |
| [Kafka y eventos](docs/kafka-eventos.md) | Topicos, productores, consumidores y evidencias |
| [Dominio de negocio](docs/dominio-negocio.md) | Marketplace universitario, usuarios y reglas |
| [Referencia de puertos](docs/puertos.md) | Puertos DEV/PROD y endpoints de salud |
| [Manual de usuario](docs/manual-usuario.md) | Flujos por rol: estudiante, vendedor y administrador |
| [Sesiones del curso](docs/sesiones/indice.md) | Evidencias S01-S16 con formato academico |

Libro digital MkDocs: carpeta [docs/](docs/).

### Publicar documentacion en GitHub Pages

Workflow: [.github/workflows/docs.yml](.github/workflows/docs.yml), construye MkDocs y publica el sitio en la rama `gh-pages`.

Pasos una sola vez:

1. Subir estos cambios a `main` o `frontend_Smart`.
2. Esperar que GitHub Actions termine en verde: `Actions -> Publicar documentacion`.
3. Ir a `Settings -> Pages`.
4. En `Source`, elegir `Deploy from a branch`.
5. En `Branch`, elegir `gh-pages` y carpeta `/ (root)`.
6. Guardar.
7. En `About -> Website`, colocar `https://erickmijael17.github.io/SmartCampus-Marketplace/`.

URL final:

```text
https://erickmijael17.github.io/SmartCampus-Marketplace/
```

---

## Inicio rapido DEV

```bash
make compose-infra
make compose-keycloak
make compose-kafka
make compose-obs
```

Tambien puedes levantar todo lo anterior con:

```bash
make compose-all
```

Para levantar un microservicio especifico:

```bash
make compose-ms MS=auth-ms
```

---

## Ver documentacion local

```bash
python -m pip install -r requirements-docs.txt
python -m mkdocs serve -a 127.0.0.1:8000
```

Abrir:

```text
http://127.0.0.1:8000/SmartCampus-Marketplace/
```

Build de verificacion:

```bash
python -m mkdocs build --strict
```

---

## Stack tecnologico

| Capa | Tecnologia |
|---|---|
| Backend | Java 17, Spring Boot 3.x, Spring Cloud |
| Configuracion | Spring Cloud Config Server |
| Registro | Eureka Server |
| Gateway | Spring Cloud Gateway |
| Seguridad | Keycloak, JWT RS256, OAuth2 Resource Server |
| Datos | PostgreSQL, Flyway |
| Mensajeria | Apache Kafka, Kafka UI |
| Frontend | Angular 20, TypeScript, RxJS, Karma, Playwright |
| Observabilidad | Prometheus, Loki, Promtail, Grafana |
| Despliegue | Docker Compose |
| Documentacion | MkDocs Material, GitHub Pages |

---

## Estructura del repositorio

```text
SmartCampus-Marketplace/
├── infra/           Config Server, Eureka, Gateway y config-repo
├── keycloak/        Realm smartcampus e import de identidad
├── kafka/           Broker, Kafka UI y exporter
├── obs/             Prometheus, Loki, Promtail y Grafana
├── servicio/        Microservicios de dominio y soporte
├── frontend/        SPA Angular en la rama frontend_Smart
├── docs/            Libro digital MkDocs
├── mkdocs.yml       Navegacion y tema de la documentacion
└── Makefile         Atajos de build y despliegue
```

---

## Microservicios

| Servicio | Responsabilidad |
|---|---|
| `auth-ms` | Login y delegacion de autenticacion a Keycloak |
| `producto-ms` | Gestion de productos |
| `categoria-ms` | Gestion de categorias |
| `orden-ms` | Ordenes de compra |
| `pago-ms` | Mercado Pago, confirmacion y validacion de pagos |
| `favoritos-ms` | Productos favoritos por usuario |
| `chat-ms` | Mensajeria y comprobantes de venta validada |
| `media-ms` | Gestion de archivos e imagenes |
| `calificacion-ms` | Resenas y calificaciones |
| `publicacion-ms` | Publicaciones del marketplace |

---

## Puertos principales

| Servicio | URL |
|---|---|
| Gateway | `http://localhost:28082` |
| Config Server | `http://localhost:28888` |
| Eureka | `http://localhost:28761` |
| Keycloak | `http://localhost:8080` |
| Kafka UI | `http://localhost:28085` |
| Grafana | `http://localhost:23000` |

Los microservicios no deben publicarse directamente al host por HTTP. En Docker Compose quedan accesibles por red interna y se consumen a traves del Gateway.

---

## Estado funcional documentado

| Capacidad | DEV | PROD local |
|---|---|---|
| Config Server | Si | Si |
| Eureka | Si | Si |
| Gateway | Si | Si |
| Keycloak realm `smartcampus` | Si | Si |
| Microservicios por Docker Compose | Si | Si |
| Kafka y eventos | Si | Si |
| Observabilidad | Si | Si |
| Frontend Angular | Si, rama `frontend_Smart` | Build de producción |
| Mercado Pago | Si, con variables `MP_*` | Si, con variables de entorno |
| Libro digital MkDocs | Si | Si |

---

## Licencia y uso

Proyecto educativo - UPeU / Desarrollo de Aplicaciones Distribuidas.
