// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ul.fc.css.tascaeats.domain.avaliacao.Avaliacao;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;
import pt.ul.fc.css.tascaeats.dto.avaliacaoDTO.AvaliacaoDTO;
import pt.ul.fc.css.tascaeats.repository.avaliacaoRepository.IAvaliacaoRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;
import pt.ul.fc.css.tascaeats.service.avaliacaoServices.AvaliacaoService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private IAvaliacaoRepository avaliacaoRepository;

    @Mock
    private IPedidoRepository pedidoRepository;

    @Mock
    private IRestauranteRepository restauranteRepository;

    @Mock
    private IUtilizadorRepository utilizadorRepository;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Test
    void getAvaliacaoByPedidoNuloOuInexistenteDevolveNull() {
        assertThat(avaliacaoService.getAvaliacaoByPedido(null)).isNull();

        when(avaliacaoRepository.findByPedidoId(99L)).thenReturn(Optional.empty());

        assertThat(avaliacaoService.getAvaliacaoByPedido(99L)).isNull();
    }

    @Test
    void getAvaliacaoByPedidoMapeiaDto() {
        Cliente cliente = clienteComId(1L);
        Restaurante restaurante = restauranteComId(2L);
        Pedido pedido = pedidoEntregue(3L, 1L, 2L);
        Avaliacao avaliacao = avaliacaoComId(4L, 5, "bom", cliente, restaurante, pedido);

        when(avaliacaoRepository.findByPedidoId(3L)).thenReturn(Optional.of(avaliacao));

        AvaliacaoDTO dto = avaliacaoService.getAvaliacaoByPedido(3L);

        assertThat(dto.getId()).isEqualTo(4L);
        assertThat(dto.getClassificacao()).isEqualTo(5);
        assertThat(dto.getComentario()).isEqualTo("bom");
        assertThat(dto.getClienteId()).isEqualTo(1L);
        assertThat(dto.getRestauranteId()).isEqualTo(2L);
        assertThat(dto.getPedidoId()).isEqualTo(3L);
    }

    @Test
    void criarAvaliacaoValidaDadosBasicos() {
        assertEquals("Dados da avaliação são obrigatórios.",
                assertThrows(IllegalArgumentException.class, () -> avaliacaoService.criarAvaliacao(null)).getMessage());

        assertEquals("A classificação deve estar entre 1 e 5.",
                assertThrows(IllegalArgumentException.class,
                        () -> avaliacaoService.criarAvaliacao(new AvaliacaoDTO(null, 6, "", 1L, 2L, 3L))).getMessage());

        assertEquals("Pedido, cliente e restaurante são obrigatórios.",
                assertThrows(IllegalArgumentException.class,
                        () -> avaliacaoService.criarAvaliacao(new AvaliacaoDTO(null, 4, "", null, 2L, 3L))).getMessage());
    }

    @Test
    void criarAvaliacaoFalhaQuandoPedidoJaAvaliado() {
        AvaliacaoDTO dto = dtoValido();
        when(avaliacaoRepository.existsByPedidoId(3L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> avaliacaoService.criarAvaliacao(dto));

        assertEquals("Este pedido já foi avaliado.", ex.getMessage());
    }

    @Test
    void criarAvaliacaoFalhaQuandoPedidoNaoEntregue() {
        AvaliacaoDTO dto = dtoValido();
        Pedido pedido = new Pedido(3L, 1L, 2L);

        when(avaliacaoRepository.existsByPedidoId(3L)).thenReturn(false);
        when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedido));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> avaliacaoService.criarAvaliacao(dto));

        assertEquals("Só é possível avaliar pedidos entregues.", ex.getMessage());
    }

    @Test
    void criarAvaliacaoFalhaQuandoClienteOuRestauranteNaoCorrespondemAoPedido() {
        Pedido pedidoClienteDiferente = pedidoEntregue(3L, 9L, 2L);
        when(avaliacaoRepository.existsByPedidoId(3L)).thenReturn(false);
        when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedidoClienteDiferente));

        IllegalArgumentException clienteErrado = assertThrows(IllegalArgumentException.class,
                () -> avaliacaoService.criarAvaliacao(dtoValido()));
        assertEquals("O cliente não corresponde ao pedido informado.", clienteErrado.getMessage());

        Pedido pedidoRestauranteDiferente = pedidoEntregue(3L, 1L, 8L);
        when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedidoRestauranteDiferente));

        IllegalArgumentException restauranteErrado = assertThrows(IllegalArgumentException.class,
                () -> avaliacaoService.criarAvaliacao(dtoValido()));
        assertEquals("O restaurante informado não pertence ao pedido.", restauranteErrado.getMessage());
    }

    @Test
    void criarAvaliacaoFalhaQuandoUtilizadorNaoECliente() {
        AvaliacaoDTO dto = dtoValido();
        Pedido pedido = pedidoEntregue(3L, 1L, 2L);
        Utilizador admin = new RestauranteAdmin("Admin", "admin@test.pt", "pw");
        admin.setId(1L);

        when(avaliacaoRepository.existsByPedidoId(3L)).thenReturn(false);
        when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedido));
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(admin));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> avaliacaoService.criarAvaliacao(dto));

        assertEquals("O utilizador informado não é cliente.", ex.getMessage());
    }

    @Test
    void criarAvaliacaoComSucessoAtualizaMediaDoRestaurante() {
        AvaliacaoDTO dto = new AvaliacaoDTO(null, 4, "  Muito bom  ", 1L, 2L, 3L);
        Cliente cliente = clienteComId(1L);
        Restaurante restaurante = restauranteComId(2L);
        Pedido pedido = pedidoEntregue(3L, 1L, 2L);

        when(avaliacaoRepository.existsByPedidoId(3L)).thenReturn(false);
        when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedido));
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(2L)).thenReturn(Optional.of(restaurante));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(i -> i.getArgument(0));
        when(avaliacaoRepository.countByRestauranteId(2L)).thenReturn(3);
        when(avaliacaoRepository.calculateAverageRatingByRestauranteId(2L)).thenReturn(4.566);

        Avaliacao avaliacao = avaliacaoService.criarAvaliacao(dto);

        assertThat(avaliacao.getComentario()).isEqualTo("Muito bom");
        assertThat(avaliacao.getCliente()).isSameAs(cliente);
        assertThat(avaliacao.getRestaurante()).isSameAs(restaurante);
        assertThat(restaurante.getNumeroAvaliacoes()).isEqualTo(3);
        assertThat(restaurante.getAvaliacaoMedia()).isEqualTo(4.6);
        verify(restauranteRepository).save(restaurante);
    }

    @Test
    void pedidoJaAvaliadoTrataNuloEConsultaRepositorio() {
        assertThat(avaliacaoService.pedidoJaAvaliado(null)).isFalse();

        when(avaliacaoRepository.existsByPedidoId(3L)).thenReturn(true);

        assertThat(avaliacaoService.pedidoJaAvaliado(3L)).isTrue();
    }

    @Test
    void getAvaliacoesRestauranteMapeiaLista() {
        Cliente cliente = clienteComId(1L);
        Restaurante restaurante = restauranteComId(2L);
        Pedido pedido = pedidoEntregue(3L, 1L, 2L);
        Avaliacao avaliacao = avaliacaoComId(4L, 5, "excelente", cliente, restaurante, pedido);
        when(avaliacaoRepository.findByRestauranteId(2L)).thenReturn(List.of(avaliacao));

        var result = avaliacaoService.getAvaliacoesRestaurante(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("classificacao", 5);
        assertThat(result.get(0)).containsEntry("comentario", "excelente");
        assertThat(result.get(0)).containsEntry("clienteId", 1L);
    }

    private AvaliacaoDTO dtoValido() {
        return new AvaliacaoDTO(null, 4, "bom", 1L, 2L, 3L);
    }

    private Pedido pedidoEntregue(Long id, Long clienteId, Long restauranteId) {
        Pedido pedido = new Pedido(id, clienteId, restauranteId);
        pedido.setEstado(EstadoPedido.ENTREGUE);
        return pedido;
    }

    private Cliente clienteComId(Long id) {
        Cliente cliente = new Cliente("Cliente", "cliente@test.pt", "pw", morada());
        cliente.setId(id);
        return cliente;
    }

    private Restaurante restauranteComId(Long id) {
        Restaurante restaurante = new Restaurante(
                "Restaurante",
                "123456789",
                morada(),
                new RestauranteAdmin("Admin", "admin@test.pt", "pw"),
                new CatalogoEntregas());
        ReflectionTestUtils.setField(restaurante, "id", id);
        return restaurante;
    }

    private Avaliacao avaliacaoComId(Long id, int classificacao, String comentario, Cliente cliente, Restaurante restaurante, Pedido pedido) {
        Avaliacao avaliacao = new Avaliacao(classificacao, comentario, cliente, restaurante, pedido);
        ReflectionTestUtils.setField(avaliacao, "id", id);
        return avaliacao;
    }

    private Morada morada() {
        Morada morada = new Morada();
        morada.setRua("Rua");
        morada.setNumero(1);
        morada.setCodigoPostal("1000-000");
        morada.setCidade("Lisboa");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");
        return morada;
    }
}
