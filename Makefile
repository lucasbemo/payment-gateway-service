.PHONY: help build test run clean docker-up docker-down logs

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the application
	mvn clean package -DskipTests

test: ## Run tests
	mvn clean test

integration-test: ## Run integration tests
	mvn clean verify -Pintegration

run: ## Run the application locally
	mvn spring-boot:run

clean: ## Clean the project
	mvn clean

docker-up: ## Start all Docker containers
	docker-compose up -d

docker-down: ## Stop all Docker containers
	docker-compose down

docker-logs: ## View Docker container logs
	docker-compose logs -f

docker-restart: ## Restart Docker containers
	docker-compose down && docker-compose up -d

logs: ## View application logs
	docker-compose logs -f payment-gateway

swagger: ## Open Swagger UI
	@echo "Opening Swagger UI at http://localhost:8000/swagger-ui.html"
	@open http://localhost:8000/swagger-ui.html 2>/dev/null || \
		xdg-open http://localhost:8000/swagger-ui.html 2>/dev/null || \
		echo "Please open http://localhost:8000/swagger-ui.html in your browser"

pgadmin: ## Open pgAdmin
	@echo "Opening pgAdmin at http://localhost:8080"
	@open http://localhost:8080 2>/dev/null || \
		xdg-open http://localhost:8080 2>/dev/null || \
		echo "Please open http://localhost:8080 in your browser"

kafka-ui: ## Open Kafka UI
	@echo "Opening Kafka UI at http://localhost:8081"
	@open http://localhost:8081 2>/dev/null || \
		xdg-open http://localhost:8081 2>/dev/null || \
		echo "Please open http://localhost:8081 in your browser"

grafana: ## Open Grafana
	@echo "Opening Grafana at http://localhost:3000"
	@open http://localhost:3000 2>/dev/null || \
		xdg-open http://localhost:3000 2>/dev/null || \
		echo "Please open http://localhost:3000 in your browser"

prometheus: ## Open Prometheus
	@echo "Opening Prometheus at http://localhost:9090"
	@open http://localhost:9090 2>/dev/null || \
		xdg-open http://localhost:9090 2>/dev/null || \
		echo "Please open http://localhost:9090 in your browser"

zipkin: ## Open Zipkin
	@echo "Opening Zipkin at http://localhost:9411"
	@open http://localhost:9411 2>/dev/null || \
		xdg-open http://localhost:9411 2>/dev/null || \
		echo "Please open http://localhost:9411 in your browser"

minio: ## Open MinIO Console
	@echo "Opening MinIO Console at http://localhost:9001"
	@open http://localhost:9001 2>/dev/null || \
		xdg-open http://localhost:9001 2>/dev/null || \
		echo "Please open http://localhost:9001 in your browser"

health: ## Check application health
	@curl -s http://localhost:8000/actuator/health | jq .

generate-openapi: ## Generate OpenAPI documentation
	mvn springdoc:generate

coverage: ## Generate code coverage report
	mvn clean test jacoco:report

sonar: ## Run SonarQube analysis
	mvn clean verify sonar:sonar -Dsonar.projectKey=payment-gateway

format: ## Format code
	mvn spotless:apply

validate: ## Validate code style
	mvn spotless:check
