// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;
import pt.ul.fc.css.tascaeats.dto.mensagensDTO.ApiErrorDTO;
import pt.ul.fc.css.tascaeats.dto.mensagensDTO.ApiMessageDTO;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaResponseDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorLoginDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRegistoRespostaDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRespostaDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorUpdateDTO;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.service.utilizadorServices.UtilizadorService;

@RestController
@RequestMapping("/api")
public class UtilizadorController {

    private final UtilizadorService UtilizadorService;
    private final IPedidoRepository pedidoRepository;
    private static final String UTILIZADOR_NOT_FOUND = "Utilizador not found";

    public UtilizadorController(UtilizadorService UtilizadorService, IPedidoRepository pedidoRepository) {
        this.UtilizadorService = UtilizadorService;
        this.pedidoRepository = pedidoRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UtilizadorRegistoDTO UtilizadorRegistoDTO) {
        try {
            Utilizador created = UtilizadorService.createUtilizador(UtilizadorRegistoDTO);
            UtilizadorRegistoRespostaDTO response = new UtilizadorRegistoRespostaDTO(
                    "Utilizador registered successfully",
                    created.getId(),
                    // Escape Utilizador-provided fields before returning them in the response body.
                    sanitizeForOutput(created.getEmail()),
                    created.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("Invalid registration data"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("Utilizador already exists"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("Utilizador registration conflict"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiMessageDTO> login(@RequestBody UtilizadorLoginDTO UtilizadorLoginDTO) {
        boolean ok = UtilizadorService.login(UtilizadorLoginDTO);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiMessageDTO("Invalid Utilizador"));
        }
        return ResponseEntity.ok(new ApiMessageDTO("Login successful (mock)"));
    }

    @GetMapping("/Utilizadors")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UtilizadorRespostaDTO>> getUtilizadors() {
        List<UtilizadorRespostaDTO> Utilizadors = UtilizadorService.getAllUtilizadors()
                .stream()
                .map(this::toUtilizadorResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Utilizadors);
    }

    @GetMapping("/Utilizadors/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUtilizadorById(@PathVariable("id") Long id) {
        Utilizador Utilizador = UtilizadorService.getUtilizadorById(id);
        if (Utilizador == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(UTILIZADOR_NOT_FOUND));
        }
        return ResponseEntity.ok(toUtilizadorResponse(Utilizador));
    }

    @PutMapping("/Utilizadors/{id}")
    @Transactional
    public ResponseEntity<Object> updateUtilizador(@PathVariable("id") Long id,
                                             @RequestBody UtilizadorUpdateDTO dto) {
        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(error("Request body is required"));
            }
            if (dto.getType() != null && dto.getType().equalsIgnoreCase("driver")) {
                return respondWithUpdatedUtilizador(UtilizadorService.updateEntregador(id, dto));
            }
            return respondWithUpdatedUtilizador(UtilizadorService.updateUtilizador(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("Utilizador update conflict"));
        }
    }

    @DeleteMapping("/Utilizadors/{id}")
    public ResponseEntity<Object> deleteUtilizador(@PathVariable("id") Long id) {
        try {
            boolean deleted = UtilizadorService.deleteUtilizador(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(UTILIZADOR_NOT_FOUND));
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error(e.getMessage()));
        }
    }

    private String sanitizeForOutput(String value) {
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }

    private ResponseEntity<Object> respondWithUpdatedUtilizador(Utilizador updated) {
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(UTILIZADOR_NOT_FOUND));
        }
        return ResponseEntity.ok(toUtilizadorResponse(updated));
    }

    private ApiErrorDTO error(String message) {
        return new ApiErrorDTO(message);
    }

    private UtilizadorRespostaDTO toUtilizadorResponse(Utilizador Utilizador) {
        UtilizadorRespostaDTO dto = new UtilizadorRespostaDTO();
        dto.setId(Utilizador.getId());
        dto.setName(Utilizador.getName());
        dto.setEmail(Utilizador.getEmail());
        dto.setType(Utilizador.getClass().getSimpleName());

        if (Utilizador instanceof Entregador driver) {
            boolean temEntregaAtiva = driver.getId() > 0
                    && pedidoRepository.existsEntregaAtivaByEntregadorId(driver.getId());
            dto.setAvailable(driver.isAvailable() && !temEntregaAtiva);
        }
        if (Utilizador instanceof Cliente client) {
            // Vai buscar a primeira morada da lista (se existir) para manter a retrocompatibilidade com a API
            Morada moradaPrincipal = client.getAddresses().isEmpty() ? null : client.getAddresses().get(0);
            dto.setAddress(toMoradaResponse(moradaPrincipal));
        }

        return dto;
    }

    private MoradaResponseDTO toMoradaResponse(Morada address) {
        if (address == null) {
            return null;
        }
        return new MoradaResponseDTO(
            address.getId(),
                address.getRua(),
                address.getNumero(),
                address.getComplemento(),
                address.getPais(),
                address.getDistrito(),
                address.getCidade(),
                address.getCodigoPostal()
        );
    }
}
