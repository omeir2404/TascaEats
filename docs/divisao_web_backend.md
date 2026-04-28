Optimizing tool selection...Perfeito. Aqui está um guia direto para fazer a divisão em backend + web e garantir que a interface web fala com o backend via REST.

1. Comandos de Terminal (Bash)

Executa na raiz do projeto:

~~~bash
mkdir -p backend web
mv src pom.xml Dockerfile backend/
~~~

Opcional, para confirmar:

~~~bash
tree -L 2
~~~

2. Novo docker-compose.yml (3 serviços)

Substitui o ficheiro docker-compose.yml da raiz por este conteúdo:

~~~yaml
version: "3.9"

services:
  db:
    image: postgres:16-alpine
    container_name: tascaeats-db
    environment:
      POSTGRES_DB: tascaeats
      POSTGRES_USER: tascaeats
      POSTGRES_PASSWORD: tascaeats
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tascaeats -d tascaeats"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: tascaeats-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/tascaeats
      SPRING_DATASOURCE_USERNAME: tascaeats
      SPRING_DATASOURCE_PASSWORD: tascaeats
    ports:
      - "8080:8080"
      - "9090:9090"
    depends_on:
      db:
        condition: service_healthy

  web:
    build:
      context: ./web
      dockerfile: Dockerfile
    container_name: tascaeats-web
    environment:
      BACKEND_API_URL: http://backend:8080
    ports:
      - "8081:8081"
    depends_on:
      - backend

volumes:
  postgres_data:
~~~

3. Configuração RestTemplate (projeto web)

Cria uma classe de configuração no projeto web, por exemplo em src/main/java/.../config/RestTemplateConfig.java:

~~~java
package pt.ul.fc.css.tascaeats.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
~~~

4. Exemplo WebRestauranteController (projeto web)

Exemplo completo de controlador no web, por exemplo em src/main/java/.../controllers/WebRestauranteController.java:

~~~java
package pt.ul.fc.css.tascaeats.web.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class WebRestauranteController {

    private final RestTemplate restTemplate;
    private final String backendApiUrl;

    public WebRestauranteController(
            RestTemplate restTemplate,
            @Value("${BACKEND_API_URL:http://localhost:8080}") String backendApiUrl) {
        this.restTemplate = restTemplate;
        this.backendApiUrl = backendApiUrl;
    }

    @GetMapping("/restaurantes")
    public String listarRestaurantes(Model model) {
        String url = backendApiUrl + "/api/restaurantes";

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> restaurantes =
                response.getBody() != null ? response.getBody() : Collections.emptyList();

        model.addAttribute("restaurantes", restaurantes);
        return "restaurantes";
    }
}
~~~

Este controlador cumpre o requisito: a web não acede à base de dados diretamente, comunica com o backend exclusivamente por endpoint REST. 

Se quiseres, no próximo passo posso gerar também um pom.xml mínimo para o projeto web (com thymeleaf + web) e um Dockerfile para esse serviço.