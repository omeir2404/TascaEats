package pt.ul.fc.css.tascaeats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        // Arranca a aplicação Spring Boot do Frontend Web
        SpringApplication.run(WebApplication.class, args);
    }

    /**
     * Ao declarar este método com @Bean, dizemos ao Spring para criar
     * uma instância de RestTemplate e guardá-la no seu contexto.
     * Assim, poderás injetar (via construtor) este RestTemplate
     * em qualquer WebController teu para fazer chamadas ao Backend!
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}