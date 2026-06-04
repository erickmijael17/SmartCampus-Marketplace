# SmartCampus Marketplace - Microservicios

Este proyecto es una plataforma de mercado digital diseñada para un entorno universitario, permitiendo a los estudiantes comprar y vender productos de manera eficiente. Está construido utilizando una arquitectura de microservicios robusta y moderna.

## 🚀 Arquitectura del Proyecto

El sistema se divide en tres capas principales:

1.  **Infraestructura (`/infra`)**: Servicios de soporte que permiten el funcionamiento del ecosistema.
    *   **Config Server**: Gestión centralizada de configuraciones.
    *   **Eureka Server**: Registro y descubrimiento de servicios.
    *   **API Gateway**: Punto de entrada único con seguridad JWT y enrutamiento.
2.  **Servicios de Negocio (`/servicio`)**: La lógica principal del marketplace.
    *   **Auth MS**: Autenticación, registro de usuarios y generación de JWT.
    *   **Producto MS**: Gestión de catálogo de productos.
    *   **Catálogo MS**: Clasificación y categorías.
    *   **Carrito MS**: Gestión temporal de compras.
    *   **Orden MS**: Procesamiento de pedidos (comunicación asíncrona vía Kafka).
    *   **Pago MS**: Procesamiento de transacciones.
    *   **Inventario MS**: Control de stock.
3.  **Soporte y Observabilidad**:
    *   **Kafka (`/kafka`)**: Mensajería asíncrona para comunicación entre servicios.
    *   **Observabilidad (`/obs`)**: Stack completo con Prometheus, Grafana, Loki y Promtail.

---

## 🛠️ Requisitos Previos

Para ejecutar este proyecto en una nueva computadora, asegúrate de tener instalado:

*   **Java 17 o superior**: Necesario para compilar y ejecutar los microservicios Spring Boot.
*   **Maven 3.8+**: Para la gestión de dependencias y construcción de JARs.
*   **Docker y Docker Compose**: Fundamental para levantar las bases de datos, Kafka y el stack de observabilidad.
*   **Postman / Insomnia**: Para probar los endpoints de la API.
*   **IDE (IntelliJ IDEA recomendado)**: Con plugins para Spring Boot y Lombok.

---

## 🏁 Guía Rápida de Inicio

Para una guía detallada paso a paso, consulta la [Guía para Estudiantes](GUIA_ESTUDIANTE.md).

### 1. Clonar y Configurar
```bash
git clone <url-del-repositorio>
cd SmartCampus-Marketplace
```

### 2. Levantar Infraestructura Base
Es necesario iniciar Docker para las bases de datos y Kafka antes de correr los servicios Java:
```bash
# Iniciar bases de datos de servicios
cd servicio/auth-ms && docker compose -f compose-dev.yml up -d
# Repetir para los demás microservicios según sea necesario o usar el compose global en infra
```

### 3. Orden de Arranque (Muy Importante)
Los servicios deben iniciarse en este orden para evitar errores de conexión:
1.  **Config Server** (Puerto 18888)
2.  **Eureka Server** (Puerto 18761)
3.  **Kafka** (vía Docker)
4.  **Microservicios de Negocio** (Auth, Producto, etc.)
5.  **API Gateway** (Puerto 18080)

---

## 📖 Documentación Adicional

*   [Guía Detallada para Estudiantes](GUIA_ESTUDIANTE.md): Instrucciones paso a paso, flujo de Dev a Prod y pruebas.
*   [Arquitectura Funcional](servicio/CampusMarket-Documentacion.md): Detalle de cada microservicio y sus responsabilidades.

---

## 👥 Contribuidores
Proyecto desarrollado para fines académicos en la UPEU.
