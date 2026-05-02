// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.TipoRestaurante;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarEstadoRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.CriarRestauranteDTO;
import pt.ul.fc.css.tascaeats.service.restauranteServices.RestauranteService;

@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteController {

    private final RestauranteService restauranteService;
    private static final String ERROR_FIELD = "error";

    public RestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    //Criar restaurante
    @PostMapping
    public ResponseEntity<Object> criarRestaurante(@Valid @RequestBody CriarRestauranteDTO dto, @RequestParam String adminEmail) {
        try {
            Restaurante criado = restauranteService.criarRestaurante(dto, adminEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(respostaRestaurante(criado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    //Atualizar restaurante
    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizarRestaurante(@PathVariable Long id, @Valid @RequestBody AtualizarRestauranteDTO dto) {
        try {
            Restaurante atualizado = restauranteService.atualizarRestaurante(id, dto);
            return ResponseEntity.ok(respostaRestaurante(atualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    //Atualizar estado do restaurante
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Object> atualizarEstado(@PathVariable Long id, @Valid @RequestBody AtualizarEstadoRestauranteDTO dto) {
        try {
            Restaurante atualizado = restauranteService.atualizarEstado(id, dto);
            return ResponseEntity.ok(respostaRestaurante(atualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_FIELD, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    //Abrir restaurante
    @PatchMapping("/{id}/abrir")
    public ResponseEntity<Object> abrirRestaurante(@PathVariable Long id) {
        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(EstadoRestaurante.ABERTO);
        return atualizarEstado(id, dto);
    }

    //Fechar restaurante
    @PatchMapping("/{id}/fechar")
    public ResponseEntity<Object> fecharRestaurante(@PathVariable Long id) {
        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(EstadoRestaurante.FECHADO);
        return atualizarEstado(id, dto);
    }

    //Obter restaurante por id
    @GetMapping("/{id}")
    public ResponseEntity<Object> obterRestaurante(@PathVariable Long id) {
        try {
            Restaurante restaurante = restauranteService.obterRestaurantePorId(id);
            return ResponseEntity.ok(respostaRestaurante(restaurante));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }
    
    //Listar restaurantes
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarRestaurantes(@RequestParam(required = false) String nome, @RequestParam(required = false) String cidade, @RequestParam(required = false) EstadoRestaurante estado) {
        List<Restaurante> restaurantes;

        if (nome != null && !nome.isBlank()) {
            restaurantes = restauranteService.procurarRestaurantePorNome(nome);
        } else if (cidade != null && !cidade.isBlank()) {
            restaurantes = restauranteService.procurarRestaurantePorCidade(cidade);
        } else if (estado != null) {
            restaurantes = restauranteService.listarPorEstado(estado);
        } else {
            restaurantes = restauranteService.listarRestaurantes();
        }

        List<Map<String, Object>> resposta = new ArrayList<>();
        for (Restaurante restaurante : restaurantes) {
            resposta.add(resumoRestaurante(restaurante));
        }

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/pesquisa")
    public ResponseEntity<List<Map<String, Object>>> pesquisarRestaurantes(@RequestParam(required = false) String nome,
                                                                           @RequestParam(name = "tipoCozinha", required = false) TipoRestaurante tipoCozinha,
                                                                           @RequestParam(required = false) EstadoRestaurante estado,
                                                                           @RequestParam(required = false) Integer pedidosRealizados,
                                                                           @RequestParam(required = false) Double avaliacaoMinima,
                                                                           @RequestParam(required = false) Integer numeroAvaliacoes,
                                                                           @RequestParam(required = false) String morada,
                                                                           @RequestParam(required = false) Double precoMedio) {
        List<Restaurante> restaurantes = restauranteService.pesquisarRestaurantes(
                nome,
                tipoCozinha,
                estado,
                pedidosRealizados,
                avaliacaoMinima,
                numeroAvaliacoes,
                morada,
                precoMedio
        );

        List<Map<String, Object>> resposta = new ArrayList<>();
        for (Restaurante restaurante : restaurantes) {
            resposta.add(resumoRestaurante(restaurante));
        }

        return ResponseEntity.ok(resposta);
    }

    //Funções auxiliares
    private Map<String, Object> resumoRestaurante(Restaurante r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("nome", r.getNome());
        m.put("estado", r.getEstado());
        m.put("tipoRestaurante", r.getTipoRestaurante());
        m.put("numeroAvaliacoes", r.getNumeroAvaliacoes());

        Map<String, Object> moradaMap = null;
        if (r.getMorada() != null) {
            moradaMap = new HashMap<>();
            moradaMap.put("rua", r.getMorada().getRua());
            moradaMap.put("numeroPorta", r.getMorada().getNumero());
            moradaMap.put("codigoPostal", r.getMorada().getCodigoPostal());
            moradaMap.put("cidade", r.getMorada().getCidade());
            moradaMap.put("localidade", r.getMorada().getCidade());
            moradaMap.put("pais", r.getMorada().getPais());
            moradaMap.put("distrito", r.getMorada().getDistrito());
            moradaMap.put("complemento", r.getMorada().getComplemento());
        }
        m.put("morada", moradaMap);
        return m;
    }

    private Map<String, Object> respostaRestaurante(Restaurante r) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", r.getId());
        response.put("nome", r.getNome());
        response.put("nif", r.getNif());
        response.put("estado", r.getEstado());

        Map<String, Object> moradaMap = null;
        if (r.getMorada() != null) {
            moradaMap = new HashMap<>();
            moradaMap.put("rua", r.getMorada().getRua());
            moradaMap.put("numeroPorta", r.getMorada().getNumero());
            moradaMap.put("codigoPostal", r.getMorada().getCodigoPostal());
            moradaMap.put("localidade", r.getMorada().getCidade());
            moradaMap.put("pais", r.getMorada().getPais());
            moradaMap.put("distrito", r.getMorada().getDistrito());
            moradaMap.put("complemento", r.getMorada().getComplemento());
        }
        response.put("morada", moradaMap);
        return response;
    }


}
