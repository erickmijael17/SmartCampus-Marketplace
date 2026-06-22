=========================================================
 INFORME DE PRUEBAS DE FUNCIONALIDAD - auth-ms + Keycloak
=========================================================
Fecha: 2026-06-22
Proyecto: SmartCampus-Marketplace

RESUMEN
-------
Se realizaron pruebas de funcionalidad del microservicio auth-ms
utilizando Keycloak como proveedor de identidad OAuth2. Se detectaron
ERRORES CRITICOS que impiden la compilacion y ejecucion del servicio.

=========================================================
ERRORES ENCONTRADOS
=========================================================

ERROR 1 (CRITICO - Compilacion)
-------------------------------
Archivo: servicio/auth-ms/pom.xml:58
Problema: Dependencia 'flyway-database-postgresql' sin version.
          Spring Boot 3.2.0 no gestiona esta dependencia en su BOM.
Solucion: Agregar <version> explicita (ej: 10.17.0) a la dependencia.

ERROR 2 (CRITICO - Compilacion/ejecucion)
-----------------------------------------
Archivo: servicio/auth-ms/src/main/java/com/upeu/auth/AuthApplication.java:3
Problema: Se referencia 'JwtProperties' (import + @EnableConfigurationProperties)
          pero el archivo JwtProperties.java NO EXISTE en auth-ms/config/.
Solucion: Crear servicio/auth-ms/src/main/java/com/upeu/auth/config/JwtProperties.java
          (existe en otros microservicios como producto-ms, carrito-ms, etc.)

ERROR 3 (CRITICO - Ejecucion)
------------------------------
Archivo: servicio/auth-ms/src/main/java/com/upeu/auth/config/DataInitializer.java:21
Problema: Se inyecta PasswordEncoder via constructor pero NO HAY un @Bean
          de PasswordEncoder definido en SecurityConfig.java ni en ninguna
          otra clase de configuracion. Causara NoSuchBeanDefinitionException
          al iniciar la aplicacion.
Solucion: Agregar @Bean PasswordEncoder en SecurityConfig.java.

ERROR 4 (CRITICO - SISTEMICO - Todos los microservicios)
--------------------------------------------------------
Archivos: TODOS los pom.xml (servicio/*/pom.xml, infra/*/pom.xml)
Problema: Spring Boot 3.2.0 es INCOMPATIBLE con Spring Cloud 2025.0.x.
          Se usa spring-cloud.version = 2025.0.1 o 2025.0.2, pero Spring
          Cloud 2025.0.x (Bath) requiere Spring Boot 4.x.
          Con Spring Boot 3.2.0 debe usarse Spring Cloud 2023.0.x (Leyton).
Solucion: Cambiar <spring-cloud.version> a 2023.0.x en todos los pom.xml.

ERROR 5 (MEDIO - Inconsistencia)
--------------------------------
Problema: Se usan DOS versiones distintas de Spring Cloud:
          - 6 servicios usan 2025.0.1
          - 10 servicios usan 2025.0.2
Impacto:  Medio - inconsistencia de versiones entre servicios.

ERROR 6 (ALTO - Carrito-ms e Inventario-ms)
--------------------------------------------
Archivos: servicio/carrito-ms/pom.xml, servicio/inventario-ms/pom.xml
Problema: Tienen 'flyway-database-postgresql' pero les falta 'flyway-core'.
          Flyway no funcionara sin el core.
Solucion: Agregar dependencia 'flyway-core' a ambos pom.xml.

ERROR 7 (ALTO - Docker/Infraestructura)
----------------------------------------
Problema: NO EXISTE configuracion Docker para Keycloak en el proyecto.
          Keycloak es referenciado en:
          - infra/config/config-repo/application-dev.yml (KEYCLOAK_URL)
          - infra/config/config-repo/application-prod.yml (KEYCLOAK_URL)
          - infra/config/config-repo/gateway-dev.yml (keycloak-route)
          - infra/config/config-repo/gateway-prod.yml (keycloak-route)
          Pero no hay:
          - compose.yml con servicio keycloak
          - Directorio keycloak/ en la raiz
          - Archivo de importacion de realm (JSON)
Solucion: Crear infra/keycloak/compose.yml con Keycloak 25.x + import de
          realm smartcampus configurado con client marketplace-client.

=========================================================
PRUEBAS EXITOSAS (Keycloak standalone)
=========================================================
A pesar de los errores anteriores, se verifico que Keycloak
funciona correctamente con la configuracion esperada:

1. KEYCLOAK INICIADO: quay.io/keycloak/keycloak:25.0.6 (start-dev)
2. REALM 'smartcampus' CREADO: OK (via API admin)
3. CLIENT 'marketplace-client' CREADO: OK
   - directAccessGrantsEnabled = true (password grant)
   - publicClient = true
4. FLUJO PASSWORD GRANT FUNCIONA:
   - testuser:test123 -> Token JWT obtenido correctamente
   - admin:admin123 -> Token JWT obtenido correctamente
5. JWKS ENDPOINT: http://localhost:8080/realms/smartcampus/protocol/openid-connect/certs
   -> Retorna 2 claves publicas
6. TOKEN JWT CONTIENE:
   - "preferred_username": "testuser"
   - "realm_access.roles": ["default-roles-smartcampus", "offline_access", "uma_authorization"]
   - "iss": "http://localhost:8080/realms/smartcampus"

NOTA: El AuthService.java puede parsear correctamente estos campos.

=========================================================
CONCLUSION
=========================================================
El diseno de integracion auth-ms + Keycloak es CORRECTO (proxy
de login hacia Keycloak usando password grant + parseo de JWT
para extraer preferred_username y realm_access.roles).

SIN EMBARGO, existen 4 errores CRITICOS que impiden la
compilacion y ejecucion del auth-ms, ademas de problemas en
la infraestructura Docker/Docker Compose que impiden levantar
Keycloak como parte del stack.

Se requiere corregir los errores 1-4 y 7 como minimo para
poder realizar una prueba end-to-end completa.

=========================================================
