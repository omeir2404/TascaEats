// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import pt.ul.fc.css.tascaeats.domain.menu.Menu;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
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
        guardarRestaurante("Tasca Eats", "123456789", EstadoRestaurante.FECHADO, "admin1@gmail.com", null);
        guardarRestaurante("PIZZARIA DO BAIRRO", "987654321", EstadoRestaurante.ABERTO, "admin2@gmail.com", null);
        guardarRestaurante("Hamburgueria", "111222333", EstadoRestaurante.RESERVADO, "admin3@gmail.com", null);

        List<Restaurante> resultados = restauranteRepository.findByNomeContainingIgnoreCase("tasca");

        assertThat(resultados)
                .hasSize(1)
                .extracting(Restaurante::getNome)
                .containsExactly("Tasca Eats");
    }

    @Test
    @DisplayName("B: Encontrar restaurante por NIF unico.")
    void findByNifDeveEncontrarRestaurantePorNifUnico() {
        Restaurante restaurante = guardarRestaurante(
                "Restaurante",
                "222333444",
                EstadoRestaurante.FECHADO,
                "admin4@gmail.com",
                null
        );

        Optional<Restaurante> resultado = restauranteRepository.findByNif("222333444");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(restaurante.getId());
        assertThat(resultado.get().getNome()).isEqualTo("Restaurante");
    }

    @Test
    @DisplayName("C: Procurar restaurante com NIF inexistente deve devolver vazio.")
    void findByNifComNifInexistenteDeveDevolverVazio() {
        guardarRestaurante("Tasca Existente", "555666777", EstadoRestaurante.ABERTO, "admin5@gmail.com", null);

        Optional<Restaurante> resultado = restauranteRepository.findByNif("000000000");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("D: Procura de restaurantes por estado deve devolver apenas restaurantes no estado pedido")
    void findByEstadoDeveDevolverApenasRestaurantesNoEstadoPedido() {
        guardarRestaurante("Aberto 1", "100000001", EstadoRestaurante.ABERTO, "admin6@gmail.com", null);
        guardarRestaurante("Fechado", "100000002", EstadoRestaurante.FECHADO, "admin7@gmail.com", null);
        guardarRestaurante("Aberto 2", "100000003", EstadoRestaurante.ABERTO, "admin8@gmail.com", null);

        List<Restaurante> resultados = restauranteRepository.findByEstado(EstadoRestaurante.ABERTO);

        assertThat(resultados)
                .hasSize(2)
                .extracting(Restaurante::getNome)
                .containsExactlyInAnyOrder("Aberto 1", "Aberto 2");
        assertThat(resultados)
                .extracting(Restaurante::getEstado)
                .containsOnly(EstadoRestaurante.ABERTO);
    }

    @Test
    @DisplayName("E: Deve identificar o restaurante mais popular de uma franquia especifica")
    void findRestauranteMaisPopularDaFranquiaDeveDevolverRestauranteComMaisPedidos() {
        Restaurante alphaCentro = guardarRestaurante(
                "Alpha Centro", "900000001", EstadoRestaurante.ABERTO, "franquia1@gmail.com", "Franquia Alpha"
        );
        Restaurante alphaNorte = guardarRestaurante(
                "Alpha Norte", "900000002", EstadoRestaurante.ABERTO, "franquia2@gmail.com", "Franquia Alpha"
        );
        guardarRestaurante(
                "Beta Sul", "900000003", EstadoRestaurante.ABERTO, "franquia3@gmail.com", "Franquia Beta"
        );

        guardarPedido(1L, alphaCentro.getId(), LocalDate.of(2026, 8, 1));
        guardarPedido(2L, alphaCentro.getId(), LocalDate.of(2026, 8, 2));
        guardarPedido(3L, alphaCentro.getId(), LocalDate.of(2026, 8, 3));
        guardarPedido(4L, alphaNorte.getId(), LocalDate.of(2026, 8, 4));

        Optional<Restaurante> resultado = restauranteRepository.findRestauranteMaisPopularDaFranquia("Franquia Alpha");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(alphaCentro.getId());
        assertThat(resultado.get().getNome()).isEqualTo("Alpha Centro");
    }

    private Restaurante guardarRestaurante(String nome,
                                           String nif,
                                           EstadoRestaurante estado,
                                           String emailAdmin,
                                           String franquia) {
        RestauranteAdmin admin = new RestauranteAdmin("Admin " + nif, emailAdmin, "password123");
        entityManager.persist(admin);

        Menu menu = new Menu("Menu " + nif);
        entityManager.persist(menu);

        Restaurante restaurante = new Restaurante(
                nome,
                nif,
                criarMorada(nif),
                admin,
                new CatalogoEntregas()
        );
        restaurante.setMenu(menu);
        restaurante.setFranquia(franquia);

        if (estado == EstadoRestaurante.ABERTO) {
            restaurante.abrir();
        } else if (estado == EstadoRestaurante.RESERVADO) {
            restaurante.reservar();
        }

        entityManager.persist(restaurante);
        entityManager.flush();
        return restaurante;
    }

    private Pedido guardarPedido(Long clienteId, Long restauranteId, LocalDate data) {
        Pedido pedido = new Pedido(null, clienteId);
        pedido.adicionarProduto(restauranteId * 100, restauranteId);
        pedido.setData(data);
        entityManager.persist(pedido);
        entityManager.flush();
        return pedido;
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
