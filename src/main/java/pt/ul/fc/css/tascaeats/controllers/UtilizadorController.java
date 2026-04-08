package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
import pt.ul.fc.css.tascaeats.service.utilizadorServices.UtilizadorService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UtilizadorController {

    private final UtilizadorService UtilizadorService;
    private static final String Utilizador_NOT_FOUND = "Utilizador not found";

    public UtilizadorController(UtilizadorService UtilizadorService) {
        this.UtilizadorService = UtilizadorService;
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
    public ResponseEntity<List<UtilizadorRespostaDTO>> getUtilizadors() {
        List<UtilizadorRespostaDTO> Utilizadors = UtilizadorService.getAllUtilizadors()
                .stream()
                .map(this::toUtilizadorResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Utilizadors);
    }

    @GetMapping("/Utilizadors/{id}")
    public ResponseEntity<Object> getUtilizadorById(@PathVariable("id") Long id) {
        Utilizador Utilizador = UtilizadorService.getUtilizadorById(id);
        if (Utilizador == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(Utilizador_NOT_FOUND));
        }
        return ResponseEntity.ok(toUtilizadorResponse(Utilizador));
    }

    @PutMapping("/Utilizadors/{id}")
    public ResponseEntity<Object> updateUtilizador(@PathVariable("id") Long id,
                                             @RequestBody UtilizadorUpdateDTO dto) {
        try {
            if (dto.getType() != null && dto.getType().equalsIgnoreCase("driver")) {
                return respondWithUpdatedUtilizador(UtilizadorService.updateEntregador(id, dto));
            }
            return respondWithUpdatedUtilizador(UtilizadorService.updateUtilizador(id, dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("Utilizador update conflict"));
        }
    }

    @DeleteMapping("/Utilizadors/{id}")
    public ResponseEntity<Object> deleteUtilizador(@PathVariable("id") Long id) {
        boolean deleted = UtilizadorService.deleteUtilizador(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(Utilizador_NOT_FOUND));
        }
        return ResponseEntity.noContent().build();
    }

    private String sanitizeForOutput(String value) {
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }

    private ResponseEntity<Object> respondWithUpdatedUtilizador(Utilizador updated) {
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(Utilizador_NOT_FOUND));
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
            dto.setAvailable(driver.isAvailable());
        }
        if (Utilizador instanceof Cliente client) {
            dto.setAddress(toMoradaResponse(client.getAddress()));
        }

        return dto;
    }

    private MoradaResponseDTO toMoradaResponse(Morada address) {
        if (address == null) {
            return null;
        }
        return new MoradaResponseDTO(
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getCountry(),
                address.getDistrict(),
                address.getCity(),
                address.getPostalCode()
        );
    }
}
