package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ul.fc.css.tascaeats.domain.produto.CatalogoProdutos;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Morada;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarEstadoRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.CriarRestauranteDTO;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IRestauranteAdminRepository;
import pt.ul.fc.css.tascaeats.service.restauranteServices.RestauranteService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestauranteServiceTest {

    @Mock
    private IRestauranteRepository restauranteRepository;

    @Mock
    private IRestauranteAdminRepository restauranteAdminRepository;

    @InjectMocks
    private RestauranteService restauranteService;

    // A) Criação de restaurante com DTO nulo
    @Test
    @DisplayName("A: Criar restaurante com DTO nulo.")
    void createRestauranteWithNullDTO(){
        try{
            restauranteService.criarRestaurante(null, "adimn@gmail.com");
            fail("Devia dar IllegalArgumentException. O DTO não pode ser nulo");
        }catch(IllegalArgumentException e){
            assertEquals("Os dados do restaurante são obrigatórios.", e.getMessage());
        }
    }


    //B) Criação de um restaurante com administrador inexistente.
    @Test
    @DisplayName("B: Criar restaurante com Admin inexistente.")
    void createRestauranteWithAdminInexistente() {
        try {
            CriarRestauranteDTO dto = criarDTOValido();
            when(restauranteAdminRepository.findByEmail("admin@gmail.com")).thenReturn(null);

            restauranteService.criarRestaurante(dto, "admin@gmail.com");
            fail("O administrador não foi encontrado. Devia lançar IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertEquals("O administrador não foi encontrado.", e.getMessage());
        }
    }


    //C) Criar restaurante com nome em branco
    @Test
    @DisplayName("C: Criar restaurante com nome em branco.")
    void createRestauranteWithBlankNome() {
        try{
            CriarRestauranteDTO dto = criarDTOValido();
            dto.setNome("  ");
            when(restauranteAdminRepository.findByEmail("admin@gmail.com")).thenReturn(adminValido());

            restauranteService.criarRestaurante(dto, "admin@gmail.com");
            fail("O nome do restaurante é obrigatório.");
        } catch (IllegalArgumentException e) {
            assertEquals("O nome do restaurante é obrigatório.", e.getMessage());
        }
    }


    //D) Criar restaurante com o email do administrador nulo ou vazio.
    @Test
    @DisplayName("D: Criar restaurante com adminEmail nulo ou vazio.")
    void createRestauranteWithNullOrBlankAdminEmail() {
        CriarRestauranteDTO dto = criarDTOValido();

        try {
            restauranteService.criarRestaurante(dto, null);
            fail("O email do administrador é obrigatório. Devia lançar IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertEquals("O email do administrador é obrigatório.", e.getMessage());
        }

        try{
            restauranteService.criarRestaurante(dto, "   ");
            fail("O email do administrador é obrigatório. Devia lançar IllegalArgumentException.");
        }catch(IllegalArgumentException e){
            assertEquals("O email do administrador é obrigatório.", e.getMessage());
        }
    }


    //E) Criar restaurante com NIF inválido
    @Test
    @DisplayName("E: Criar restaurante com nif inválido.")
    void createRestauranteWithInvalidNif() {
        CriarRestauranteDTO dto = criarDTOValido();
        when(restauranteAdminRepository.findByEmail("admin@gmail.com")).thenReturn(adminValido());

        // Com letras
        dto.setNif("abcd");
        try {
            restauranteService.criarRestaurante(dto, "admin@gmail.com");
            fail("NIF inválido. Devia lançar IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertEquals("Nif inválido. O nif deve ter 9 dígitos.", e.getMessage());
        }

        // Com menos dígitos que 9
        dto.setNif("12345");
        try {
            restauranteService.criarRestaurante(dto, "admin@gmail.com");
            fail("NIF inválido. Devia lançar IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertEquals("Nif inválido. O nif deve ter 9 dígitos.", e.getMessage());
        }
    }


    // F) Criar restaurante com NIF já existente
    @Test
    @DisplayName("F: Criar restaurante com nif já existente.")
    void createRestauranteWithExistingNif() {
        RestauranteAdmin admin = adminValido();
        CriarRestauranteDTO dto = criarDTOValido();
        Restaurante existente = new Restaurante("Outro", "123456789",
                new Morada("Rua X", 10, "1000-001", "Lisboa"),
                admin,
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );

        when(restauranteAdminRepository.findByEmail("admin@gmail.com")).thenReturn(admin);
        when(restauranteRepository.findByNif("123456789")).thenReturn(Optional.of(existente));

        try {
            restauranteService.criarRestaurante(dto, "admin@gmail.com");
            fail("Já existe um restaurante com esse NIF. Devia lançar IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertEquals("Já existe um restaurante com esse nif.", e.getMessage());
        }
    }


    // G) Criar restaurante com morada nula
    @Test
    @DisplayName("G: Criar Restaurante com morada nula.")
    void createRestauranteWithNullMorada() {
        CriarRestauranteDTO dto = criarDTOValido();
        dto.setMorada(null);

        when(restauranteAdminRepository.findByEmail("admin@gmail.com")).thenReturn(adminValido());
        when(restauranteRepository.findByNif("123456789")).thenReturn(Optional.empty());

        try {
            restauranteService.criarRestaurante(dto, "admin@gmail.com");
            fail("A morada é obrigatória. Devia lançar IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertEquals("A morada é obrigatória.", e.getMessage());
        }
    }


    // H) Criar restaurante com número da porta menor ou igual a 0.
    @Test
    @DisplayName("H: Criar restaurante com número da porta <= 0.")
    void createRestauranteWithInvalidNumeroPorta() {
        CriarRestauranteDTO dto = criarDTOValido();
        dto.getMorada().setNumeroPorta(0);

        when(restauranteAdminRepository.findByEmail("admin@gmail.com")).thenReturn(adminValido());
        when(restauranteRepository.findByNif("123456789")).thenReturn(Optional.empty());

        try {
            restauranteService.criarRestaurante(dto, "admin@gmail.com");
            fail("O número da porta não pode ser negativo e tem de existir. Devia lançar IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertEquals("O número da porta não pode ser negativo e tem de existir.", e.getMessage());
        }
    }


    //I) Criar restaurante com sucesso.
    @Test
    @DisplayName("I: Criar restaurante com sucesso. Tem de criar restaurante com estado FECHADO.")
    void createRestauranteSuccessCreatesClosedWithCatalogs() {
        RestauranteAdmin admin = adminValido();
        CriarRestauranteDTO dto = criarDTOValido();
        dto.setNome("Tasca Eats");
        dto.setNif("123456789");

        when(restauranteAdminRepository.findByEmail("admin@gmail.com")).thenReturn(admin);
        when(restauranteRepository.findByNif("123456789")).thenReturn(Optional.empty());
        when(restauranteRepository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Restaurante created = restauranteService.criarRestaurante(dto, "  admin@gmail.com ");

        verify(restauranteRepository).save(any(Restaurante.class));
        assertThat(created.getNome()).isEqualTo("Tasca Eats");
        assertThat(created.getNif()).isEqualTo("123456789");
        assertThat(created.getEstado()).isEqualTo(EstadoRestaurante.FECHADO);
        assertThat(created.getCatalogoProdutos()).isNotNull();
        assertThat(created.getCatalogoEntregas()).isNotNull();
        assertThat(created.getAdministrador()).isSameAs(admin);
    }


    //J) Atualizar restaurante com DTO nulo.
    @Test
    @DisplayName("J: Atualizar restaurante com DTO nulo.")
    void updateRestauranteWithNullDTO() {
        try {
            restauranteService.atualizarRestaurante(1L, null);
            fail("É necessário haver dados para atualizar o restaurante.");
        } catch (IllegalArgumentException e) {
            assertEquals("É necessário haver dados para atualizar o restaurante.", e.getMessage());
        }
    }


    //K) Atualizar restaurante inexistente.
    @Test
    @DisplayName("K: Atualizar restaurante com restaurante inexistente.")
    void updateRestauranteWithInexistenteRestaurante() {
        AtualizarRestauranteDTO dto = atualizarDTOValido();
        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());
        try {
            restauranteService.atualizarRestaurante(99L, dto);
            fail("O restaurante não foi encontrado.");
        } catch (IllegalArgumentException e) {
            assertEquals("O restaurante não foi encontrado.", e.getMessage());
        }
    }


    //L) Atualizar restaurante com NIF de outro restaurante.
    @Test
    @DisplayName("L: Atualizar restaurante com nif de outro restaurante.")
    void updateRestauranteWithNifFromAnotherRestaurante() {
        RestauranteAdmin admin = adminValido();
        Restaurante restauranteExistente = new Restaurante(
                "Restaurante A",
                "111111111",
                new Morada("Rua A", 1, "1111-111", "Lisboa"),
                admin,
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );

        Restaurante outroRestaurante = new Restaurante(
                "Restaurante B",
                "222222222",
                new Morada("Rua B", 2, "2222-222", "Porto"),
                admin,
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );

        AtualizarRestauranteDTO dto = atualizarDTOValido();
        dto.setNif("222222222");

        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restauranteExistente));
        when(restauranteRepository.findByNif("222222222")).thenReturn(Optional.of(outroRestaurante));

        try {
            restauranteService.atualizarRestaurante(1L, dto);
            fail("Já existe um restaurante com esse nif.");
        } catch (IllegalArgumentException e) {
            assertEquals("Já existe um restaurante com esse nif.", e.getMessage());
        }
    }


    //M) Atualizar restaurante com sucesso.
    @Test
    @DisplayName("M: Atualizar restaurante com sucesso. Persiste nome, nif e morada.")
    void updateRestauranteSuccessPersistsNomeNifMorada() {
        Restaurante restauranteExistente = new Restaurante(
                "Burger King",
                "111111111",
                new Morada("Rua bk", 5, "1304-131", "Lisboa"),
                adminValido(),
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );

        AtualizarRestauranteDTO dto = atualizarDTOValido();
        dto.setNome("Mc Donald's");
        dto.setNif(" 123456789 ");
        dto.getMorada().setRua("Rua mc");
        dto.getMorada().setNumeroPorta(77);
        dto.getMorada().setCodigoPostal("4000-123");
        dto.getMorada().setLocalidade("Bragança");

        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restauranteExistente));
        when(restauranteRepository.findByNif("123456789")).thenReturn(Optional.empty());
        when(restauranteRepository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Restaurante atualizado = restauranteService.atualizarRestaurante(1L, dto);

        assertThat(atualizado.getNome()).isEqualTo("Mc Donald's");
        assertThat(atualizado.getNif()).isEqualTo("123456789");
        assertThat(atualizado.getMorada().getRua()).isEqualTo("Rua mc");
        assertThat(atualizado.getMorada().getNumeroPorta()).isEqualTo(77);
        assertThat(atualizado.getMorada().getCodigoPostal()).isEqualTo("4000-123");
        assertThat(atualizado.getMorada().getLocalidade()).isEqualTo("Bragança");
    }

    //N) Atualizar estado com DTO nulo ou estado nulo.
    @Test
    @DisplayName("N: Atualizar estado com DTO nulo ou estado nulo.")
    void atualizarEstadoComNullDTOOuNullEstado() {
        try {
            restauranteService.atualizarEstado(1L, null);
            fail("É necessário existir estado do restaurante.");
        } catch (IllegalArgumentException e) {
            assertEquals("É necessário existir estado do restaurante.", e.getMessage());
        }

        try {
            AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
            dto.setEstado(null);
            restauranteService.atualizarEstado(1L, dto);
            fail("É necessário existir estado do restaurante.");
        } catch (IllegalArgumentException e) {
            assertEquals("É necessário existir estado do restaurante.", e.getMessage());
        }
    }


    //O) Atualizar estado do restaurante com restaurante inexistente.
    @Test
    @DisplayName("O: Atualizar estado com restaurante inexistente.")
    void atualizarEstadoRestauranteInexistente() {
        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(EstadoRestaurante.ABERTO);

        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            restauranteService.atualizarEstado(99L, dto);
            fail("O restaurante não foi encontrado.");
        } catch (IllegalArgumentException e) {
            assertEquals("O restaurante não foi encontrado.", e.getMessage());
        }
    }

    //P) Atualizar estado do restaurante para o mesmo estado atual.
    @Test
    @DisplayName("P: Atualizar estado do restaurante para o mesmo estado atual.")
    void atualizarEstadoMesmoEstado() {
        Restaurante restaurante = new Restaurante(
                "Tasca Eats",
                "123456789",
                new Morada("Rua", 1, "1234-000", "Lisboa"),
                adminValido(),
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );

        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(EstadoRestaurante.FECHADO);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));

        try {
            restauranteService.atualizarEstado(1L, dto);
            fail("O restaurante já está nesse estado. Não é possível atualizar.");
        } catch (IllegalStateException e) {
            assertEquals("O restaurante já está nesse estado. Não é possível atualizar.", e.getMessage());
        }
    }

    //Q) Atualizar restaurante para estado ABERTO
    @Test
    @DisplayName("Q: Atualizar estado do restaurante para ABERTO.")
    void atualizarEstadoParaAberto() {
        Restaurante restaurante = new Restaurante(
                "Tasca Eats",
                "123456789",
                new Morada("Rua", 1, "1234-111", "Lisboa"),
                adminValido(),
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );

        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(EstadoRestaurante.ABERTO);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));

        try {
            restauranteService.atualizarEstado(1L, dto);
        } catch (Exception e) {
            fail("Não devia lançar exceção.");
        }

        verify(restauranteRepository).save(any(Restaurante.class));
        assertEquals(EstadoRestaurante.ABERTO, restaurante.getEstado());
    }

    //R) Atualizar restaurante para estado FECHADO
    @Test
    @DisplayName("R: Atualizar estado para FECHADO.")
    void atualizarEstadoParaFechado() {
        Restaurante restaurante = new Restaurante(
                "Tasca Etas",
                "123456789",
                new Morada("Rua", 1, "1000-000", "Lisboa"),
                adminValido(),
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );
        restaurante.abrir();

        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(EstadoRestaurante.FECHADO);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));

        try {
            restauranteService.atualizarEstado(1L, dto);
        } catch (Exception e) {
            fail("Não devia lançar exceção.");
        }

        verify(restauranteRepository).save(any(Restaurante.class));
        assertEquals(EstadoRestaurante.FECHADO, restaurante.getEstado());
    }

    //S) Atualizar restaurante para estado RESERVADO
    @Test
    @DisplayName("S: Atualizar estado para RESERVADO.")
    void atualizarEstadoParaReservado() {
        Restaurante restaurante = new Restaurante(
                "Tasca Eats",
                "123456789",
                new Morada("Rua", 1, "1000-000", "Lisboa"),
                adminValido(),
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );

        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(EstadoRestaurante.RESERVADO);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));

        try {
            restauranteService.atualizarEstado(1L, dto);
        } catch (Exception e) {
            fail("Não devia lançar exceção.");
        }

        verify(restauranteRepository).save(any(Restaurante.class));
        assertEquals(EstadoRestaurante.RESERVADO, restaurante.getEstado());
    }

    //T) Procurar restaurante por nome, com nome nulo/vazio deve devolver todos os restaurantes.



    //**********Funções auxiliares************

    //Criar restaurante válido
    private CriarRestauranteDTO criarDTOValido() {
        CriarRestauranteDTO dto = new CriarRestauranteDTO();
        dto.setNome("Tasca Eats");
        dto.setNif("123456789");

        MoradaDTO morada = new MoradaDTO();
        morada.setRua("Rua 123");
        morada.setNumeroPorta(2);
        morada.setCodigoPostal("2765-543");
        morada.setLocalidade("Porto");

        dto.setMorada(morada);
        return dto;
    }

    //Criar administrador válido
    private RestauranteAdmin adminValido() {
        return new RestauranteAdmin("Admin", "admin@gmail.com", "password:123");
    }

    private AtualizarRestauranteDTO atualizarDTOValido() {
        AtualizarRestauranteDTO dto = new AtualizarRestauranteDTO();
        dto.setNome("Tasca Atualizada");
        dto.setNif("123456789");

        MoradaDTO morada = new MoradaDTO();
        morada.setRua("Rua Atualizada");
        morada.setNumeroPorta(10);
        morada.setCodigoPostal("3000-300");
        morada.setLocalidade("Coimbra");

        dto.setMorada(morada);
        return dto;
    }
}
