# =============================================================================
# Makefile - SmartCampus Marketplace
# Integra el código del proyecto usando Docker Compose:
#   - Construye imágenes Docker desde los Dockerfiles del proyecto
#   - Publica las imágenes al registry
#
# Uso:
#   make build-all         → Construir todas las imágenes
#   make push-all          → Publicar al registry
#   make logs MS=auth-ms   → Ver logs de un microservicio
# =============================================================================

REGISTRY    ?= erickmijael17
VERSION     ?= latest
NAMESPACE   ?= ecom

# ─── Servicios de Infraestructura ────────────────────────────────────────────
INFRA_SERVICES := config-server eureka gateway

# ─── Microservicios (todos los que tienen código en servicio/) ────────────────
EXISTING_SERVICES := auth-ms orden-ms pago-ms producto-ms

# ─── Microservicios nuevos (creados en esta migración) ───────────────────────
NEW_SERVICES := persona-ms publicacion-ms categoria-ms calificacion-ms \
                chat-ms media-ms favoritos-ms

ALL_SERVICES := $(EXISTING_SERVICES) $(NEW_SERVICES)

# ─── Helpers ─────────────────────────────────────────────────────────────────
CYAN  := \033[0;36m
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED   := \033[0;31m
NC    := \033[0m

.DEFAULT_GOAL := help

.PHONY: help
help: ## Muestra esta ayuda
	@echo ""
	@echo "SmartCampus Marketplace - Comandos Docker Compose"
	@echo "=============================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(CYAN)%-25s$(NC) %s\n", $$1, $$2}'
	@echo ""

# =============================================================================
# BUILD - Construye imágenes Docker desde los Dockerfiles del proyecto
# =============================================================================

.PHONY: build-infra
build-infra: ## Construir imágenes de infraestructura (config, eureka, gateway)
	@echo -e "$(CYAN)[BUILD]$(NC) Construyendo infraestructura..."
	docker build -t $(REGISTRY)/ecom-config-server:$(VERSION) ./infra/config
	docker build -t $(REGISTRY)/ecom-eureka:$(VERSION)        ./infra/eureka
	docker build -t $(REGISTRY)/ecom-gateway:$(VERSION)       ./infra/gateway
	@echo -e "$(GREEN)[BUILD]$(NC) ✓ Infraestructura lista"

.PHONY: build-services
build-services: ## Construir imágenes de todos los microservicios
	@echo -e "$(CYAN)[BUILD]$(NC) Construyendo microservicios..."
	@for svc in $(ALL_SERVICES); do \
		echo -e "  $(CYAN)→$(NC) Construyendo $$svc..."; \
		docker build -t $(REGISTRY)/ecom-$$svc:$(VERSION) ./servicio/$$svc || \
			{ echo -e "  $(RED)✗$(NC) Error en $$svc"; exit 1; }; \
		echo -e "  $(GREEN)✓$(NC) $$svc listo"; \
	done

.PHONY: build-all
build-all: build-infra build-services ## Construir TODAS las imágenes del proyecto
	@echo -e "$(GREEN)[BUILD]$(NC) ✓ Todas las imágenes construidas"
	@docker images | grep $(REGISTRY)/ecom

# Targets individuales por servicio
build-auth-ms:         ; docker build -t $(REGISTRY)/ecom-auth-ms:$(VERSION)         ./servicio/auth-ms
# persona-ms fusionado en auth-ms
build-publicacion-ms:  ; docker build -t $(REGISTRY)/ecom-publicacion-ms:$(VERSION)  ./servicio/publicacion-ms
build-categoria-ms:    ; docker build -t $(REGISTRY)/ecom-categoria-ms:$(VERSION)    ./servicio/categoria-ms
build-orden-ms:        ; docker build -t $(REGISTRY)/ecom-orden-ms:$(VERSION)        ./servicio/orden-ms
build-pago-ms:         ; docker build -t $(REGISTRY)/ecom-pago-ms:$(VERSION)         ./servicio/pago-ms
build-calificacion-ms: ; docker build -t $(REGISTRY)/ecom-calificacion-ms:$(VERSION) ./servicio/calificacion-ms
build-chat-ms:         ; docker build -t $(REGISTRY)/ecom-chat-ms:$(VERSION)         ./servicio/chat-ms
build-media-ms:        ; docker build -t $(REGISTRY)/ecom-media-ms:$(VERSION)        ./servicio/media-ms
build-favoritos-ms:    ; docker build -t $(REGISTRY)/ecom-favoritos-ms:$(VERSION)    ./servicio/favoritos-ms
build-producto-ms:     ; docker build -t $(REGISTRY)/ecom-producto-ms:$(VERSION)     ./servicio/producto-ms
build-config-server:   ; docker build -t $(REGISTRY)/ecom-config-server:$(VERSION)   ./infra/config
build-eureka:          ; docker build -t $(REGISTRY)/ecom-eureka:$(VERSION)           ./infra/eureka
build-gateway:         ; docker build -t $(REGISTRY)/ecom-gateway:$(VERSION)          ./infra/gateway

# =============================================================================
# PUSH - Publicar imágenes al Docker Hub (o registry configurado)
# =============================================================================

.PHONY: push-infra
push-infra: ## Publicar imágenes de infraestructura
	docker push $(REGISTRY)/ecom-config-server:$(VERSION)
	docker push $(REGISTRY)/ecom-eureka:$(VERSION)
	docker push $(REGISTRY)/ecom-gateway:$(VERSION)

.PHONY: push-services
push-services: ## Publicar imágenes de microservicios
	@for svc in $(ALL_SERVICES); do \
		echo -e "  $(CYAN)→$(NC) Publicando $(REGISTRY)/ecom-$$svc:$(VERSION)..."; \
		docker push $(REGISTRY)/ecom-$$svc:$(VERSION); \
	done

.PHONY: push-all
push-all: push-infra push-services ## Publicar TODAS las imágenes
	@echo -e "$(GREEN)[PUSH]$(NC) ✓ Todas las imágenes publicadas en $(REGISTRY)"

# Shorthand: construir y publicar en un paso
.PHONY: release
release: build-all push-all ## build-all + push-all
	@echo -e "$(GREEN)[RELEASE]$(NC) ✓ Release $(VERSION) completado"

# =============================================================================
# =============================================================================

.PHONY: compose-infra
compose-infra: ## Levantar infraestructura con Docker Compose
	docker network create ecom-prod-net 2>/dev/null || true
	docker compose -f infra/compose.yml up -d

.PHONY: compose-kafka
compose-kafka: ## Levantar Kafka con Docker Compose
	docker compose -f kafka/compose.yml up -d

.PHONY: compose-keycloak
compose-keycloak: ## Levantar Keycloak con Docker Compose
	docker network create ecom-prod-net 2>/dev/null || true
	docker compose -f keycloak/compose.yml up -d

.PHONY: compose-obs
compose-obs: ## Levantar observabilidad con Docker Compose
	docker compose -f obs/compose.yml up -d

.PHONY: compose-ms
compose-ms: ## Levantar un microservicio específico (MS=auth-ms)
	@[ "$(MS)" ] || { echo "$(RED)Error: especifica MS=nombre-del-ms$(NC)"; exit 1; }
	docker compose -f servicio/$(MS)/compose.yml up -d

.PHONY: compose-all
compose-all: compose-infra compose-keycloak compose-kafka compose-obs ## Levantar toda la plataforma en Docker Compose
	@echo -e "$(GREEN)[COMPOSE]$(NC) ✓ Plataforma completa levantada"

.PHONY: compose-down
compose-down: ## Detener todos los servicios Docker Compose
	docker compose -f infra/compose.yml down 2>/dev/null || true
	docker compose -f keycloak/compose.yml down 2>/dev/null || true
	docker compose -f kafka/compose.yml down 2>/dev/null || true
	docker compose -f obs/compose.yml down 2>/dev/null || true
	@for svc in $(ALL_SERVICES); do \
		docker compose -f servicio/$$svc/compose.yml down 2>/dev/null || true; \
	done

# =============================================================================
images-list: ## Listar todas las imágenes del proyecto en el registry local
	@docker images | grep $(REGISTRY)/ecom
