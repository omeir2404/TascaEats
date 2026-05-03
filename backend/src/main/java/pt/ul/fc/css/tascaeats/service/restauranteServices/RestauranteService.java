// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.restauranteServices;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.TipoRestaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarEstadoRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.CriarRestauranteDTO;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IRestauranteAdminRepository;

@Service
@Transactional
public class RestauranteService {

	private final IRestauranteRepository restauranteRepository;
	private final IPedidoRepository pedidoRepository;
	private final IRestauranteAdminRepository administradorRepository;

	public RestauranteService(IRestauranteRepository restauranteRepository,
							  IPedidoRepository pedidoRepository,
							  IRestauranteAdminRepository administradorRepository) {
		this.restauranteRepository = restauranteRepository;
		this.pedidoRepository = pedidoRepository;
		this.administradorRepository = administradorRepository;
	}

    //Criar restaurante
	public Restaurante criarRestaurante(CriarRestauranteDTO dto, String adminEmail) {
		if (dto == null) {
			throw new IllegalArgumentException("Os dados do restaurante são obrigatórios.");
		}
		if (adminEmail == null || adminEmail.isBlank()) {
			throw new IllegalArgumentException("O email do administrador é obrigatório.");
		}

		RestauranteAdmin administrador = administradorRepository.findByEmail(adminEmail.trim());
		if (administrador == null) {
			throw new IllegalArgumentException("O administrador não foi encontrado.");
		}

		String nome = stringNormalizada(dto.getNome(), "O nome do restaurante é obrigatório.");
		String nif = normalizarNif(dto.getNif());
		validarNif(nif, null);
		Morada morada = moradaObtida(dto.getMorada());

		CatalogoEntregas catalogoEntregas = new CatalogoEntregas();

		Restaurante novoRestaurante = new Restaurante(nome, nif, morada, administrador, catalogoEntregas);
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

	// Remover restaurante
	@Transactional(rollbackFor = Exception.class)
	public boolean removerRestaurante(Long id) {
		Optional<Restaurante> restaurant = restauranteRepository.findById(id);
		if (restaurant == null) {
			return false; // Restaurante não encontrado, não é possível remover
		}
		restauranteRepository.delete(restaurant.get());
		return true;
	}

	//Procurar restaurante por cidade
	@Transactional(readOnly = true)
	public List<Restaurante> procurarRestaurantePorCidade(String cidade) {
		if (cidade == null || cidade.isBlank()) {
			return restauranteRepository.findAll(); //Se o utilizador não escrever nada, devolvo todos os restaurantes.
		}
		return restauranteRepository.findByMoradaCidadeIgnoreCase(cidade.trim());
	}

	@Transactional(readOnly = true)
	public List<Restaurante> listarPorEstado(EstadoRestaurante estado) {
		if (estado == null) {
			return restauranteRepository.findAll(); //Se o utilizador não escrever estado, devolvo todos os estados.
		}
		return restauranteRepository.findByEstado(estado);
	}


    //Funções auxiliares
	@Transactional(readOnly = true)
	public List<Restaurante> pesquisarRestaurantes(String nome,
												   TipoRestaurante tipoRestaurante,
												   EstadoRestaurante estado,
												   Integer pedidosRealizados,
												   Double avaliacaoMinima,
												   Integer numeroAvaliacoesMinimo,
												   String morada,
												   Double precoMedio) {
		String nomeNormalizado = normalizarTexto(nome);
		String moradaNormalizada = normalizarTexto(morada);

		List<Restaurante> candidatos = obterRestaurantesCandidatos(nomeNormalizado, tipoRestaurante, estado);
		Map<Long, Long> pedidosPorRestaurante = null;

		if (pedidosRealizados != null) {
			pedidosPorRestaurante = contarPedidosPorRestaurante();
		}

		final Map<Long, Long> pedidos = pedidosPorRestaurante;
		return candidatos.stream()
				.filter(restaurante -> pedidosRealizados == null || pedidos.getOrDefault(restaurante.getId(), 0L) >= pedidosRealizados)
				.filter(restaurante -> avaliacaoMinima == null || restaurante.getAvaliacaoMedia() >= avaliacaoMinima)
				.filter(restaurante -> numeroAvaliacoesMinimo == null || restaurante.getNumeroAvaliacoes() >= numeroAvaliacoesMinimo)
				.filter(restaurante -> correspondeMorada(restaurante, moradaNormalizada))
				.filter(restaurante -> precoMedio == null || restaurante.getPrecoMedio() <= precoMedio)
				.toList();
	}

	private List<Restaurante> obterRestaurantesCandidatos(String nomeNormalizado, TipoRestaurante tipoRestaurante, EstadoRestaurante estado) {
		if (nomeNormalizado != null) {
			return restauranteRepository.findByNomeContainingIgnoreCase(nomeNormalizado);
		} else if (estado != null) {
			return restauranteRepository.findByEstado(estado);
		} else if (tipoRestaurante != null) {
			return restauranteRepository.findByTipoRestaurante(tipoRestaurante);
		} else {
			return restauranteRepository.findAll();
		}
	}

	private Map<Long, Long> contarPedidosPorRestaurante() {
		Map<Long, Long> contagem = new HashMap<>();
		for (Pedido pedido : pedidoRepository.findAll()) {
			for (Long restauranteId : pedido.getRestaurantesIds()) {
				contagem.merge(restauranteId, 1L, Long::sum);
			}
		}
		return contagem;
	}

	private boolean correspondeMorada(Restaurante restaurante, String moradaNormalizada) {
		if (moradaNormalizada == null) {
			return true;
		}
		if (restaurante.getMorada() == null) {
			return false;
		}
		String textoMorada = juntarTexto(
				restaurante.getMorada().getRua(),
				restaurante.getMorada().getCidade(),
				restaurante.getMorada().getDistrito(),
				restaurante.getMorada().getPais(),
				restaurante.getMorada().getCodigoPostal()
		);
		return textoMorada.contains(moradaNormalizada);
	}

	private String juntarTexto(String... partes) {
		StringBuilder texto = new StringBuilder();
		for (String parte : partes) {
			String parteNormalizada = normalizarTexto(parte);
			if (parteNormalizada == null) {
				continue;
			}
			if (!texto.isEmpty()) {
				texto.append(' ');
			}
			texto.append(parteNormalizada);
		}
		return texto.toString();
	}

	private String normalizarTexto(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		return texto.trim().toLowerCase(Locale.ROOT);
	}

    private void validarNif(String nif, Long idIgnorar) {
        Optional<Restaurante> nifBom = restauranteRepository.findByNif(nif);
        if (nifBom.isPresent()) {
            Restaurante existe = nifBom.get();
			if (!(idIgnorar != null && idIgnorar.equals(existe.getId()))) {
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
		Integer numero = dto.getNumero();
		String complemento = dto.getComplemento() == null ? null : dto.getComplemento().trim(); // O complemento é opcional, então pode ser nulo.
		String pais = stringNormalizada(dto.getPais(), "O país é obrigatório.");
		String distrito = stringNormalizada(dto.getDistrito(), "O distrito é obrigatório.");
		String cidade = stringNormalizada(dto.getCidade(), "A cidade é obrigatória.");
		String codigoPostal = stringNormalizada(dto.getCodigoPostal(), "O código postal é obrigatório.");
		if (numero == null || numero <= 0) {
			throw new IllegalArgumentException("O número da porta não pode ser negativo e tem de existir.");
		}
		Morada morada = new Morada();
		morada.setRua(rua);
		morada.setNumero(numero);
		morada.setComplemento(complemento);
		morada.setPais(pais);
		morada.setDistrito(distrito);
		morada.setCidade(cidade);
		morada.setCodigoPostal(codigoPostal);

		return morada;
	}


}
