package pt.ul.fc.css.tascaeats.service.restauranteServices;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.produto.CatalogoProdutos;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Morada;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarEstadoRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.CriarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IRestauranteAdminRepository;

@Service
@Transactional
public class RestauranteService {

	private final IRestauranteRepository restauranteRepository;
	private final IRestauranteAdminRepository administradorRepository;

	public RestauranteService(IRestauranteRepository restauranteRepository, IRestauranteAdminRepository administradorRepository) {
		this.restauranteRepository = restauranteRepository;
		this.administradorRepository = administradorRepository;
	}

    //Criar restaurante
	public Restaurante criarRestaurante(CriarRestauranteDTO dto, String adminEmail) {
		if (dto == null) {
			throw new IllegalArgumentException("Os dados do restaurante sao obrigatórios.");
		}
		if (adminEmail == null || adminEmail.isBlank()) {
			throw new IllegalArgumentException("O email do administrador e obrigatório.");
		}

		RestauranteAdmin administrador = administradorRepository.findByEmail(adminEmail.trim());
		if (administrador == null) {
			throw new IllegalArgumentException("O administrador não foi encontrado.");
		}

		String nome = stringNormalizada(dto.getNome(), "O nome do restaurante é obrigatório.");
		String nif = normalizarNif(dto.getNif());
		validarNif(nif, null);
		Morada morada = moradaObtida(dto.getMorada());

		CatalogoProdutos catalogoProdutos = new CatalogoProdutos();
		CatalogoEntregas catalogoEntregas = new CatalogoEntregas();

		Restaurante novoRestaurante = new Restaurante(nome, nif, morada, administrador, catalogoProdutos, catalogoEntregas);
		return restauranteRepository.save(novoRestaurante);
	}

    //Atualizar Restaurante
	public Restaurante atualizarRestaurante(Long id, AtualizarRestauranteDTO dto) {
		if (dto == null) {
			throw new IllegalArgumentException("É necessário haver dados para atualizar o restaurante.");
		}

		Restaurante restaurante = obterRestaurantePorId(id);
		String nome = stringNormalizada(dto.getNome(), "O nome do restaurante e obrigatório.");
		String nif = normalizarNif(dto.getNif());
		validarNif(nif, restaurante.getId());
		Morada morada = moradaObtida(dto.getMorada());

		restaurante.setNome(nome);
		restaurante.setNif(nif);
		restaurante.setMorada(morada);
		return restauranteRepository.save(restaurante);
	}

    //Atualizar estado Restaurante
	public Restaurante atualizarEstado(Long id, AtualizarEstadoRestauranteDTO dto) {
		if (dto == null || dto.getEstado() == null) {
			throw new IllegalArgumentException("É necessário existir estado do restaurante.");
		}

		Restaurante restaurante = obterRestaurantePorId(id);
		EstadoRestaurante estadoAtualizado = dto.getEstado();
		if (restaurante.getEstado() == estadoAtualizado) {
			throw new IllegalStateException("O restaurante já está nesse estado. Não é possível atualizar.");
		}

        if(estadoAtualizado == EstadoRestaurante.ABERTO){
            restaurante.abrir();
        }else if(estadoAtualizado == EstadoRestaurante.FECHADO){
            restaurante.fechar();
        }else if(estadoAtualizado == EstadoRestaurante.RESERVADO){
            restaurante.reservar();
        }else{
			throw new IllegalArgumentException("O estado do restaurante é inválido.");
        }

		return restauranteRepository.save(restaurante);
	}

    //Obter restaurante por id
    @Transactional(readOnly = true)
    public Restaurante obterRestaurantePorId(Long id) {
        Optional<Restaurante> restaurantePorId = restauranteRepository.findById(id);
        if (restaurantePorId.isEmpty()) {
            throw new IllegalArgumentException("O restaurante não foi encontrado.");
        }
        return restaurantePorId.get();
    }

    //Listar restaurantes
	@Transactional(readOnly = true)
	public List<Restaurante> listarRestaurantes() {
		return restauranteRepository.findAll();
	}

    //Procurar restaurante por nome
	@Transactional(readOnly = true)
	public List<Restaurante> procurarRestaurantePorNome(String nome) {
		if (nome == null || nome.isBlank()) {
			return restauranteRepository.findAll(); //Se o utilizador não escrever nada, devolvo todos os restaurantes.
		}
		return restauranteRepository.findByNomeContainingIgnoreCase(nome.trim());
	}

	//Procurar restaurante por cidade
	@Transactional(readOnly = true)
	public List<Restaurante> procurarRestaurantePorCidade(String cidade) {
		if (cidade == null || cidade.isBlank()) {
			return restauranteRepository.findAll(); //Se o utilizador não escrever nada, devolvo todos os restaurantes.
		}
		return restauranteRepository.findByMoradaLocalidadeIgnoreCase(cidade.trim());
	}

	@Transactional(readOnly = true)
	public List<Restaurante> listarPorEstado(EstadoRestaurante estado) {
		if (estado == null) {
			return restauranteRepository.findAll(); //Se o utilizador não escrever estado, devolvo todos os estados.
		}
		return restauranteRepository.findByEstado(estado);
	}


    //Funções auxiliares
    private void validarNif(String nif, Long idIgnorar) {
        Optional<Restaurante> nifBom = restauranteRepository.findByNif(nif);
        if (nifBom.isPresent()) {
            Restaurante existe = nifBom.get();
			boolean validacoes = idIgnorar != null && idIgnorar.equals(existe.getId());
            boolean mesmoRestaurante = validacoes;
            if (!mesmoRestaurante) {
                throw new IllegalArgumentException("Já existe um restaurante com esse nif.");
            }
        }
    }

	private String normalizarNif(String nif) {
		String nifNormalizado = stringNormalizada(nif, "O nif é obrigatório.");
		if (!nifNormalizado.matches("\\d{9}")) {
			throw new IllegalArgumentException("Nif inválido. O nif deve ter 9 dígitos.");
		}
		return nifNormalizado;
	}

	private String stringNormalizada(String st, String errorMessage) {
		if (st == null || st.isBlank()) {
			throw new IllegalArgumentException(errorMessage);
		}
		return st.trim();
	}

	private Morada moradaObtida(MoradaDTO dto) {
		if (dto == null) {
			throw new IllegalArgumentException("A morada é obrigatória.");
		}

		String rua = stringNormalizada(dto.getRua(), "A rua da morada é obrigatória.");
		String codigoPostal = stringNormalizada(dto.getCodigoPostal(), "O código postal é obrigatório.");
		String localidade = stringNormalizada(dto.getLocalidade(), "A localidade é obrigatória.");

		Integer numeroPorta = dto.getNumeroPorta();
		if (numeroPorta == null || numeroPorta <= 0) {
			throw new IllegalArgumentException("O número da porta não pode ser negativo e tem de existir.");
		}

		return new Morada(rua, numeroPorta, codigoPostal, localidade);
	}


}