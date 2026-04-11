package pt.ul.fc.css.tascaeats.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;

@DataJpaTest
public class IRestauranteRepositoryTest {

    @Autowired
    private IRestauranteRepository restauranteRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("A: Pesquisar restaurante por nome sem case-sensitive.")
    void findByNomeContainingIgnoreCaseDevePesquisarSemCaseSensitive() {
        guardarRestaurante("Tasca Eats", "123456789", EstadoRestaurante.FECHADO, "admin1@gmail.com");
        guardarRestaurante("PIZZARIA CENTRAL", "987654321", EstadoRestaurante.ABERTO, "admin2@gmail.com");
        guardarRestaurante("Hamburgueria", "111222333", EstadoRestaurante.RESERVADO, "admin3@gmail.com");

        List<Restaurante> resultados = restauranteRepository.findByNomeContainingIgnoreCase("tasca");

        assertThat(resultados)
                .hasSize(1)
                .extracting(Restaurante::getNome)
                .containsExactly("Tasca Eats");
    }

    @Test
    @DisplayName("B: Encontrar restaurante por NIF único.")
    void findByNifDeveEncontrarRestaurantePorNifUnico() {
        Restaurante restaurante = guardarRestaurante(
                "Restaurante",
                "222333444",
                EstadoRestaurante.FECHADO,
                "admin4@gmail.com"
        );

        Optional<Restaurante> resultado = restauranteRepository.findByNif("222333444");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(restaurante.getId());
        assertThat(resultado.get().getNome()).isEqualTo("Restaurante");
    }

    @Test
    @DisplayName("C: Procurar restaurante com NIF inexistente deve devolver vazio.")
    void findByNifComNifInexistenteDeveDevolverVazio() {
        guardarRestaurante("Tasca Existente", "555666777", EstadoRestaurante.ABERTO, "admin5@gmail.com");

        Optional<Restaurante> resultado = restauranteRepository.findByNif("000000000");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("D: Procura de restaurantes por estado deve devolver apenas restaurantes no estado pedido")
    void findByEstadoDeveDevolverApenasRestaurantesNoEstadoPedido() {
        guardarRestaurante("Aberto 1", "100000001", EstadoRestaurante.ABERTO, "admin6@gmail.com");
        guardarRestaurante("Fechado", "100000002", EstadoRestaurante.FECHADO, "admin7@gmail.com");
        guardarRestaurante("Aberto 2", "100000003", EstadoRestaurante.ABERTO, "admin8@gmail.com");

        List<Restaurante> resultados = restauranteRepository.findByEstado(EstadoRestaurante.ABERTO);

        assertThat(resultados)
                .hasSize(2)
                .extracting(Restaurante::getNome)
                .containsExactlyInAnyOrder("Aberto 1", "Aberto 2");
        assertThat(resultados)
                .extracting(Restaurante::getEstado)
                .containsOnly(EstadoRestaurante.ABERTO);
    }


    //**********Funções auxiliares************

    private Restaurante guardarRestaurante(String nome, String nif, EstadoRestaurante estado, String emailAdmin) {
        RestauranteAdmin admin = new RestauranteAdmin("Admin " + nif, emailAdmin, "password123");
        entityManager.persist(admin);

        Restaurante restaurante = new Restaurante(
                nome,
                nif,
                criarMorada(nif),
                admin,
                new CatalogoEntregas()
        );

        if (estado == EstadoRestaurante.ABERTO) {
            restaurante.abrir();
        } else if (estado == EstadoRestaurante.RESERVADO) {
            restaurante.reservar();
        }

        entityManager.persist(restaurante);
        entityManager.flush();
        return restaurante;
    }

    private Morada criarMorada(String sufixo) {
        Morada morada = new Morada();
        morada.setRua("Rua " + sufixo);
        morada.setNumero(1);
        morada.setCodigoPostal("1000-000");
        morada.setCidade("Lisboa");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");
        return morada;
    }
}
