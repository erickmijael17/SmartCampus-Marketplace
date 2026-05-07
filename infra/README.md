# Infraestructura de Microservicios

Este modulo contiene la infraestructura base de la arquitectura de microservicios.

---

## Componentes actuales

- config-repo (configuracion externa)
- Config Server (Spring Cloud Config Server)
- Registry Server (Eureka)
- API Gateway

---

## Componentes planificados

- Feign entre microservicios
- Circuit Breaker
- Seguridad
- Gestion del trafico en Gateway
- Observabilidad y trazabilidad
- Integracion con frontend

---

## Arquitectura (estado actual)

```text
Client -> Gateway -> Microservicios -> Registry Server -> Config Server -> config-repo
```

Evolucion objetivo:

```text
Client -> Gateway + atributos de calidad -> Microservicios -> Registry Server -> Config Server
```

---

## Puertos utilizados

| Servicio | Puerto expuesto |
|---|---:|
| Config Server DEV | 7071 |
| Config Server PROD | 7072 |
| Registry Server DEV | 7081 |
| Registry Server PROD | 7082 |
| Gateway DEV | 7091 |
| Gateway PROD | 7092 |

---

## Red de infraestructura

Se utiliza una red Docker comun:

```text
ms-net
```

Esta red permite la comunicacion entre:

- config-server
- registry-server
- gateway
- microservicios

---

## Estructura del modulo

```text
infra/
  config-server/
  registry-server/
  gateway/
  config-repo/
  docker-compose.yml
```

---

# Config Server

## Descripcion

Servidor de configuracion centralizada para los microservicios.

Permite:

- externalizar configuracion
- separar codigo de configuracion
- soportar multiples entornos (`dev`, `prod`)
- facilitar despliegue de microservicios

---

## Configuracion utilizada

Modo:

```text
native
```

Ruta del repositorio montado:

```text
/config-repo
```

---

## Levantar Config Server

DEV (desde `infra/config-server`):

```bash
mvn spring-boot:run
```

PROD (desde `infra`):

```bash
docker compose up -d config-server
```

---

## Pruebas de Config Server

DEV:

```bash
curl http://localhost:7071/catalogo/dev
```

PROD:

```bash
curl http://localhost:7072/catalogo/prod
```

---

# Registry Server (Eureka)

## Descripcion

Servidor de registro y descubrimiento de servicios.

Permite:

- registro automatico de microservicios
- descubrimiento dinamico
- integracion posterior con API Gateway (`lb://`)

---

## Levantar Registry Server

DEV (desde `infra/registry-server`):

```bash
mvn spring-boot:run
```

PROD (desde `infra`):

```bash
docker compose up -d registry-server
```

---

## Acceso a Eureka

DEV:

```text
http://localhost:7081
```

PROD (host):

```text
http://localhost:7082
```

---

# config-repo

Contiene la configuracion externa de infraestructura y microservicios.

Archivos actuales:

```text
config-repo/
  catalogo-dev.yml
  catalogo-prod.yml
  carrito-dev.yml
  carrito-prod.yml
  gateway-dev.yml
  gateway-prod.yml
  inventario-dev.yml
  inventario-prod.yml
  ordenes-dev.yml
  ordenes-prod.yml
  pagos-dev.yml
  pagos-prod.yml
  producto-dev.yml
  producto-prod.yml
  registry-server-dev.yml
  registry-server-prod.yml
```

Ejemplo:

```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

---

# Flujo de uso

1. Levantar infraestructura base

```bash
docker compose up -d
```

2. Verificar endpoints

```text
http://localhost:7072/catalogo/prod
http://localhost:7082
http://localhost:7092/api/v1/catalogo/instancia
http://localhost:7092/api/v1/producto/instancia
```

3. Levantar microservicio (ejemplo: catalogo)

4. Verificar registro del microservicio en Eureka

5. Probar enrutamiento por Gateway con `lb://catalogo`

6. Probar enrutamiento por Gateway con `lb://producto`

---

# Problemas comunes

## 1. Microservicio no conecta a config-server

Causa:
- red incorrecta

Solucion:
- conectar el servicio a `ms-net`

---

## 2. Microservicio no aparece en Eureka

Causa:
- `defaultZone` incorrecto
- `registry-server` no disponible

Solucion:
- en DEV usar `http://localhost:7081/eureka`
- en Docker usar `http://registry-server:7081/eureka`

---

## 3. Configuracion no cargada

Causa:
- archivo no existe en `config-repo`

Solucion:
- verificar nombres por entorno (`*-dev.yml`, `*-prod.yml`)

---

## 4. Uso incorrecto de localhost en Docker

Dentro de Docker:

- Incorrecto: `localhost`
- Correcto: `config-server`, `registry-server`

---

# Estado de avance

- [x] Config Server
- [x] Registry Server (Eureka)
- [x] API Gateway
- [x] Enrutamiento `lb://catalogo` y `lb://producto`
- [ ] Feign
- [ ] Circuit Breaker
- [ ] Seguridad
- [ ] Gestion del trafico (filtros, politicas y control de peticiones)
- [ ] Observabilidad y trazabilidad
- [ ] Integracion con frontend

---

# Siguiente paso

Continuar con los atributos de calidad sobre la base actual:

- incorporar Feign para comunicacion entre microservicios
- incorporar Circuit Breaker
- integrar seguridad con autenticacion y autorizacion
- aplicar gestion del trafico en Gateway
- fortalecer observabilidad y trazabilidad entre servicios
- habilitar integracion con frontend

---

# Tag sugerido

```bash
git tag -a vs04-gateway-lb-r2 -m "Infraestructura: ajustes de puertos y documentacion de gateway y balanceo"
git push origin vs04-gateway-lb-r2
```
