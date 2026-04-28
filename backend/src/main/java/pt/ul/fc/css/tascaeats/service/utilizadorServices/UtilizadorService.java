// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.utilizadorServices;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorLoginDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorUpdateDTO;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;

@Service
public class UtilizadorService {

    private final IUtilizadorRepository UtilizadorRepository;
    private final AutorService authService;

    public UtilizadorService(IUtilizadorRepository UtilizadorRepository, AutorService authService) {
        this.UtilizadorRepository = UtilizadorRepository;
        this.authService = authService;
    }

    public Utilizador createUtilizador(UtilizadorRegistoDTO dto) {
        validateRegistration(dto);

        if (UtilizadorRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }

        Utilizador Utilizador = buildUtilizadorFromType(dto.getType());
        applyCommonFields(Utilizador, dto);
        return UtilizadorRepository.save(Utilizador);
    }

    public List<Utilizador> getAllUtilizadors() {
        return UtilizadorRepository.findAll();
    }

    public Utilizador getUtilizadorById(Long id) {
        return UtilizadorRepository.findById(id).orElse(null);
    }

    public Utilizador getUtilizadorByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return UtilizadorRepository.findByEmail(email).orElse(null);
    }

    public Utilizador updateEntregador(Long id, UtilizadorUpdateDTO dto) {
        Utilizador existing = UtilizadorRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        // updated availability before using geral updateUtilizador
        if (existing instanceof Entregador driver) {
            driver.setAvailable(dto.isAvailable());
        }
        return updateUtilizador(id, dto);
    }

    public Utilizador updateUtilizador(Long id, UtilizadorUpdateDTO dto) {
        Utilizador existing = UtilizadorRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            Optional<Utilizador> byEmail = UtilizadorRepository.findByEmail(dto.getEmail());
            if (byEmail.isPresent() && byEmail.get().getId() != existing.getId()) {
                throw new IllegalStateException("Email already exists");
            }
            existing.setEmail(dto.getEmail());
        }

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getPassword() != null) {
            existing.setPassword(dto.getPassword());
        }

        if (existing instanceof Cliente client && dto.getAddress() != null && client.getAddress() != null) {
            // Keep Morada update minimal here; creating Morada requires cleaner mapping in the domain.
            applyMorada(client, dto.getAddress());
        }

        return UtilizadorRepository.save(existing);
    }

    public boolean deleteUtilizador(Long id) {
        if (!UtilizadorRepository.existsById(id)) {
            return false;
        }
        try {
            UtilizadorRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Utilizador is referenced by other records");
        }
        return true;
    }

    public boolean login(UtilizadorLoginDTO UtilizadorLoginDTO) {
        if (UtilizadorLoginDTO == null || UtilizadorLoginDTO.getEmail() == null || UtilizadorLoginDTO.getEmail().isBlank() || !UtilizadorLoginDTO.getEmail().contains("@")) {
            return false;
        }
        // Mock login: any password is accepted as long as the Utilizador exists.
        return authService.login(UtilizadorLoginDTO.getEmail()) != null;
    }

    private void validateRegistration(UtilizadorRegistoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank() || dto.getEmail().chars().filter(ch -> ch == '@').count() != 1 || dto.getEmail().contains(" ") || dto.getEmail().contains("\t") || dto.getEmail().contains("..")) {
            throw new IllegalArgumentException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new IllegalArgumentException("Type is required");
        }
        if ("client".equalsIgnoreCase(dto.getType()) && (dto.getAddress() == null)) {
            throw new IllegalArgumentException("Morada is required for client Utilizadors");
        }
        if (dto.getAddress() != null && dto.getAddress().getCodigoPostal() != null
                && dto.getAddress().getCodigoPostal().chars().filter(ch -> ch == '-').count() != 1) {
            throw new IllegalArgumentException("Codigo Postal so pode ter 1 '-' ");
        }
    }

    private Utilizador buildUtilizadorFromType(String type) {
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "client", "cliente" -> new Cliente();
            case "driver", "entregador" -> new Entregador();
            case "restaurantadmin", "restauranteadmin", "restaurant_admin", "restaurante_admin", "restaurant owner",
                 "restaurantowner", "admin", "administrador", "donodorestaurante", "dono_restaurante" ->
                    new RestauranteAdmin();
            default -> throw new IllegalArgumentException("Invalid Utilizador type: " + type);
        };
    }

    private void applyCommonFields(Utilizador Utilizador, UtilizadorRegistoDTO dto) {
        Utilizador.setName(dto.getName());
        Utilizador.setEmail(dto.getEmail());
        Utilizador.setPassword(dto.getPassword());
        applyMorada(Utilizador, dto.getAddress());
    }

    private void applyMorada(Utilizador Utilizador, MoradaDTO dto) {
        if (Utilizador instanceof Cliente client && dto != null) {
            Morada address = new Morada();

            address.setRua(dto.getRua());
            address.setNumero(dto.getNumero() == null ? null : dto.getNumero());
            address.setCodigoPostal(dto.getCodigoPostal());
            address.setCidade(dto.getCidade());

            client.setAddress(address);
        }
    }
}
