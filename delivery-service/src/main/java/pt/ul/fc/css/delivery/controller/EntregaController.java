package pt.ul.fc.css.delivery.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;
import pt.ul.fc.css.delivery.domain.entregador.Entregador;
import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregaDTO.EntregaResumoDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.EntregadorDTO;
import pt.ul.fc.css.delivery.service.EntregaService;

@RestController
@RequestMapping
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping("/entregadores")
    public ResponseEntity<Object> registarEntregador(@RequestBody Entregador entregador) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(toEntregadorDTO(entregaService.registarEntregador(entregador)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

