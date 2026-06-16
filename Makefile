# =============================================================================
# Makefile - SmartCampus Marketplace
# Integra el código del proyecto con Kubernetes:
#   - Construye imágenes Docker desde los Dockerfiles del proyecto
#   - Publica las imágenes al registry
#   - Despliega al clúster Kubernetes
#
# Uso:
#   make build-all         → Construir todas las imágenes
#   make push-all          → Publicar al registry
#   make deploy            → Desplegar en Kubernetes
#   make dev               → Modo desarrollo (Skaffold)
#   make logs MS=auth-ms   → Ver logs de un microservicio
#   make status            → Estado del clúster
# =============================================================================

REGISTRY    ?= erickmijael17
VERSION     ?= latest
NAMESPACE   ?= ecom
K8S_DIR     := k8s

# ─── Servicios de Infraestructura ────────────────────────────────────────────
INFRA_SERVICES := config-server eureka gateway

# ─── Microservicios (todos los que tienen código en servicio/) ────────────────
EXISTING_SERVICES := auth-ms carrito-ms catalogo-ms inventario-ms \
                     orden-ms pago-ms producto-ms

# ─── Microservicios nuevos (creados en esta migración) ───────────────────────
NEW_SERVICES := persona-ms publicacion-ms categoria-ms calificacion-ms \
                chat-ms notification-ms media-ms favoritos-ms search-ms

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
	@echo "SmartCampus Marketplace - Comandos Kubernetes"
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
build-persona-ms:      ; docker build -t $(REGISTRY)/ecom-persona-ms:$(VERSION)      ./servicio/persona-ms
build-publicacion-ms:  ; docker build -t $(REGISTRY)/ecom-publicacion-ms:$(VERSION)  ./servicio/publicacion-ms
build-categoria-ms:    ; docker build -t $(REGISTRY)/ecom-categoria-ms:$(VERSION)    ./servicio/categoria-ms
build-inventario-ms:   ; docker build -t $(REGISTRY)/ecom-inventario-ms:$(VERSION)   ./servicio/inventario-ms
build-carrito-ms:      ; docker build -t $(REGISTRY)/ecom-carrito-ms:$(VERSION)      ./servicio/carrito-ms
build-orden-ms:        ; docker build -t $(REGISTRY)/ecom-orden-ms:$(VERSION)        ./servicio/orden-ms
build-pago-ms:         ; docker build -t $(REGISTRY)/ecom-pago-ms:$(VERSION)         ./servicio/pago-ms
build-calificacion-ms: ; docker build -t $(REGISTRY)/ecom-calificacion-ms:$(VERSION) ./servicio/calificacion-ms
build-chat-ms:         ; docker build -t $(REGISTRY)/ecom-chat-ms:$(VERSION)         ./servicio/chat-ms
build-notification-ms: ; docker build -t $(REGISTRY)/ecom-notification-ms:$(VERSION) ./servicio/notification-ms
build-media-ms:        ; docker build -t $(REGISTRY)/ecom-media-ms:$(VERSION)        ./servicio/media-ms
build-favoritos-ms:    ; docker build -t $(REGISTRY)/ecom-favoritos-ms:$(VERSION)    ./servicio/favoritos-ms
build-search-ms:       ; docker build -t $(REGISTRY)/ecom-search-ms:$(VERSION)       ./servicio/search-ms
build-catalogo-ms:     ; docker build -t $(REGISTRY)/ecom-catalogo-ms:$(VERSION)     ./servicio/catalogo-ms
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
# KUBERNETES - Despliegue al clúster
# =============================================================================

.PHONY: k8s-check
k8s-check: ## Verificar conexión al clúster
	@kubectl cluster-info
	@echo -e "$(GREEN)[K8S]$(NC) ✓ Conectado al clúster"

.PHONY: deploy
deploy: k8s-check ## Despliegue completo en Kubernetes (3 fases)
	@chmod +x $(K8S_DIR)/scripts/*.sh
	@$(K8S_DIR)/scripts/deploy.sh

.PHONY: deploy-phase1
deploy-phase1: k8s-check ## Fase 1: Infraestructura (Config, Eureka, Gateway)
	@chmod +x $(K8S_DIR)/scripts/deploy-phase1.sh
	@$(K8S_DIR)/scripts/deploy-phase1.sh

.PHONY: deploy-phase2
deploy-phase2: k8s-check ## Fase 2: Kafka + Observabilidad
	@chmod +x $(K8S_DIR)/scripts/deploy-phase2.sh
	@$(K8S_DIR)/scripts/deploy-phase2.sh

.PHONY: deploy-phase3
deploy-phase3: k8s-check ## Fase 3: Microservicios
	@chmod +x $(K8S_DIR)/scripts/deploy-phase3.sh
	@$(K8S_DIR)/scripts/deploy-phase3.sh

# Despliegue de un solo microservicio
.PHONY: deploy-ms
deploy-ms: ## Desplegar un microservicio específico (MS=auth-ms)
	@[ "$(MS)" ] || { echo "$(RED)Error: especifica MS=nombre-del-ms$(NC)"; exit 1; }
	@echo -e "$(CYAN)[K8S]$(NC) Desplegando $(MS)..."
	kubectl apply -f ./servicio/$(MS)/k8s/$(MS).yaml
	kubectl rollout status deployment/$(MS) -n $(NAMESPACE) --timeout=300s

# Actualizar imagen de un microservicio sin redesplegar todo
.PHONY: update-ms
update-ms: ## Actualizar imagen de un MS (MS=auth-ms VERSION=1.0.1)
	@[ "$(MS)" ] || { echo "$(RED)Error: especifica MS=nombre-del-ms$(NC)"; exit 1; }
	docker build -t $(REGISTRY)/ecom-$(MS):$(VERSION) ./servicio/$(MS)
	docker push $(REGISTRY)/ecom-$(MS):$(VERSION)
	kubectl set image deployment/$(MS) $(MS)=$(REGISTRY)/ecom-$(MS):$(VERSION) -n $(NAMESPACE)
	kubectl rollout status deployment/$(MS) -n $(NAMESPACE)

# =============================================================================
# DESARROLLO - Docker Compose (modo original sin K8s)
# =============================================================================

.PHONY: compose-infra
compose-infra: ## Levantar infraestructura con Docker Compose
	docker network create ecom-prod-net 2>/dev/null || true
	docker compose -f infra/compose.yml up -d

.PHONY: compose-kafka
compose-kafka: ## Levantar Kafka con Docker Compose
	docker compose -f kafka/compose.yml up -d

.PHONY: compose-obs
compose-obs: ## Levantar observabilidad con Docker Compose
	docker compose -f obs/compose.yml up -d

.PHONY: compose-ms
compose-ms: ## Levantar un microservicio específico (MS=auth-ms)
	@[ "$(MS)" ] || { echo "$(RED)Error: especifica MS=nombre-del-ms$(NC)"; exit 1; }
	docker compose -f servicio/$(MS)/compose.yml up -d

.PHONY: compose-all
compose-all: compose-infra compose-kafka compose-obs ## Levantar toda la plataforma en Docker Compose
	@echo -e "$(GREEN)[COMPOSE]$(NC) ✓ Plataforma completa levantada"

.PHONY: compose-down
compose-down: ## Detener todos los servicios Docker Compose
	docker compose -f infra/compose.yml down 2>/dev/null || true
	docker compose -f kafka/compose.yml down 2>/dev/null || true
	docker compose -f obs/compose.yml down 2>/dev/null || true
	@for svc in auth-ms carrito-ms catalogo-ms inventario-ms orden-ms pago-ms producto-ms $(NEW_SERVICES); do \
		docker compose -f servicio/$$svc/compose.yml down 2>/dev/null || true; \
	done

# =============================================================================
# MONITOREO / DEBUGGING
# =============================================================================

.PHONY: status
status: ## Estado de todos los pods en el namespace ecom
	@echo -e "$(CYAN)═══ PODS ════════════════════════════════════$(NC)"
	kubectl get pods -n $(NAMESPACE) --sort-by=.metadata.name
	@echo ""
	@echo -e "$(CYAN)═══ SERVICES ════════════════════════════════$(NC)"
	kubectl get services -n $(NAMESPACE)
	@echo ""
	@echo -e "$(CYAN)═══ INGRESS ═════════════════════════════════$(NC)"
	kubectl get ingress -n $(NAMESPACE)
	@echo ""
	@echo -e "$(CYAN)═══ HPA ═════════════════════════════════════$(NC)"
	kubectl get hpa -n $(NAMESPACE)

.PHONY: logs
logs: ## Ver logs de un microservicio (MS=auth-ms)
	@[ "$(MS)" ] || { echo "$(RED)Error: especifica MS=nombre-del-ms$(NC)"; exit 1; }
	kubectl logs -f deployment/$(MS) -n $(NAMESPACE) --tail=100

.PHONY: shell
shell: ## Abrir shell en un pod (MS=auth-ms)
	@[ "$(MS)" ] || { echo "$(RED)Error: especifica MS=nombre-del-ms$(NC)"; exit 1; }
	kubectl exec -it deployment/$(MS) -n $(NAMESPACE) -- /bin/sh

.PHONY: port-forward
port-forward: ## Port-forward de servicios de administración
	@echo "Iniciando port-forwards (Ctrl+C para detener)..."
	kubectl port-forward svc/eureka    8761:8761  -n $(NAMESPACE) &
	kubectl port-forward svc/grafana   3000:3000  -n $(NAMESPACE) &
	kubectl port-forward svc/kafka-ui  8085:8080  -n $(NAMESPACE) &
	kubectl port-forward svc/prometheus 9090:9090 -n $(NAMESPACE) &
	kubectl port-forward svc/gateway   8080:8080  -n $(NAMESPACE)

.PHONY: kafka-topics
kafka-topics: ## Crear topics de Kafka (ordenes-topic, pagos-topic)
	kubectl exec -it kafka-0 -n $(NAMESPACE) -- \
		/opt/kafka/bin/kafka-topics.sh --create --if-not-exists \
		--topic ordenes-topic --partitions 3 --replication-factor 1 \
		--bootstrap-server localhost:9092
	kubectl exec -it kafka-0 -n $(NAMESPACE) -- \
		/opt/kafka/bin/kafka-topics.sh --create --if-not-exists \
		--topic pagos-topic --partitions 3 --replication-factor 1 \
		--bootstrap-server localhost:9092
	kubectl exec -it kafka-0 -n $(NAMESPACE) -- \
		/opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# =============================================================================
# ROLLBACK / LIMPIEZA
# =============================================================================

.PHONY: rollback
rollback: ## Rollback de un microservicio (MS=auth-ms)
	@[ "$(MS)" ] || { echo "$(RED)Error: especifica MS=nombre-del-ms$(NC)"; exit 1; }
	kubectl rollout undo deployment/$(MS) -n $(NAMESPACE)

.PHONY: destroy
destroy: ## ⚠ DESTRUIR todo el namespace ecom
	@echo -e "$(RED)¿Estás seguro? Esto eliminará TODOS los recursos de ecom.$(NC)"
	@echo "Escribe 'DESTROY ecom' para confirmar:"
	@read confirm && [ "$$confirm" = "DESTROY ecom" ] || exit 1
	kubectl delete namespace $(NAMESPACE)
	@echo -e "$(GREEN)[OK]$(NC) Namespace ecom eliminado"

.PHONY: images-list
images-list: ## Listar todas las imágenes del proyecto en el registry local
	@docker images | grep $(REGISTRY)/ecom
