# SmartCampus Marketplace - Microservicios & Kubernetes

Este proyecto es una plataforma de mercado digital universitaria basada en una arquitectura de microservicios robusta y moderna, diseñada para correr sobre **Kubernetes** con seguridad centralizada mediante **Keycloak (OAuth2 / OIDC)** y observabilidad avanzada.

---

## 🚀 Arquitectura del Proyecto

El ecosistema se organiza de la siguiente manera:

1. **Seguridad e Identidad (IdP)**:
   * **Keycloak**: Servidor de identidad y acceso que gestiona los usuarios, roles (**`ADMIN`**, **`ESTUDIANTE`**, **`VENDEDOR`**, **`MODERADOR`**) y emite tokens JWT firmados asimétricamente (RS256).
   * **PostgreSQL (Keycloak)**: Almacenamiento persistente dedicado para Keycloak.

2. **Infraestructura**:
   * **Config Server**: Servidor de configuración centralizado.
   * **Eureka Server**: Descubrimiento y registro dinámico de microservicios.
   * **API Gateway (Spring Cloud Gateway)**: Punto de entrada único que actúa como OAuth2 Client y Resource Server, propagando tokens de acceso a los microservicios mediante `TokenRelay`.

3. **Microservicios (16 en total)**:
   * **auth-ms**: Proxy de login que delega las credenciales a Keycloak para mantener retrocompatibilidad.
   * **producto-ms**, **catalogo-ms**, **categoria-ms**, **carrito-ms**, **orden-ms**, **pago-ms**, **inventario-ms**, **favoritos-ms**, **chat-ms**, **notification-ms**, **media-ms**, **calificacion-ms**, **persona-ms**, **publicacion-ms**, **search-ms**.
   * Todos los microservicios actúan como **OAuth2 Resource Servers** que validan la firma de los tokens dinámicamente usando las claves públicas de Keycloak (JWKS).

4. **Mensajería y Eventos**:
   * **Apache Kafka**: Broker de eventos asíncronos para la comunicación entre servicios (ej. Órdenes y Pagos).

5. **Observabilidad y Monitoreo**:
   * Stack integrado con **Prometheus**, **Loki**, **Promtail** y **Grafana** (con Ingress TLS).

---

## 🛠️ Requisitos Previos

Para ejecutar la arquitectura completa, asegúrate de tener instalado:
* **Java JDK 17 o superior**
* **Maven 3.8+**
* **Docker Desktop / Docker Engine** (mínimo 6GB de RAM asignada)
* **Kubernetes (Minikube, Docker Desktop K8s, o similar)**
* **Skaffold v4+**
* **Helm y Kubectl**

---

## 🏁 Despliegue Rápido en Kubernetes

La plataforma está completamente orquestada usando **Kustomize** y **Skaffold** para desarrollo continuo.

### 1. Iniciar en Desarrollo con Hot Reload (Skaffold)
```bash
# Compila, levanta las imágenes locales y sincroniza el código en tiempo real
skaffold dev --load-restrictor=LoadRestrictionsNone
```

### 2. Despliegue con Scripts Automatizados
```bash
# Ejecuta el despliegue secuencial por fases
./k8s/scripts/deploy.sh
```

---

## 📖 Documentación Relacionada

* [Guía de Inicialización y Despliegue Paso a Paso](GUIA_ESTUDIANTE.md): Instrucciones completas para arrancar el sistema desde cero en Kubernetes y probar la seguridad.
* [Walkthrough de la Migración a Keycloak](file:///C:/Users/USUARIO/.gemini/antigravity-ide/brain/7bd9ba1d-7195-4777-aedf-798d123f0e97/walkthrough.md): Resumen técnico de los cambios de seguridad, convertidores de roles y TLS.
