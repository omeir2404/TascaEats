package pt.ul.fc.css.tascaeats.service.utilizadorServices;

import org.springframework.stereotype.Service;
import pt.ul.fc.css.tascaeats.domain.Utilizador.*;
import pt.ul.fc.css.tascaeats.dto.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.UtilizadorLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.dto.UtilizadorUpdateDTO;
import pt.ul.fc.css.tascaeats.repository.UtilizadorRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UtilizadorService {

    private final UtilizadorRepository UtilizadorRepository;
    private final AutorService authService;

    public UtilizadorService(UtilizadorRepository UtilizadorRepository, AutorService authService) {
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

        if (existing instanceof Cliente client && dto.getMorada() != null && client.getMorada() != null) {
            // Keep Morada update minimal here; creating Morada requires cleaner mapping in the domain.
            applyMorada(client, dto.getMorada());
        }

        return UtilizadorRepository.save(existing);
    }

    public boolean deleteUtilizador(Long id) {
        if (!UtilizadorRepository.existsById(id)) {
            return false;
        }
        UtilizadorRepository.deleteById(id);
        return true;
    }

    public boolean login(UtilizadorLoginDTO UtilizadorLoginDTO) {
        if (UtilizadorLoginDTO == null || UtilizadorLoginDTO.getEmail() == null || UtilizadorLoginDTO.getEmail().isBlank()) {
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
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new IllegalArgumentException("Type is required");
        }
        if ("client".equalsIgnoreCase(dto.getType()) && (dto.getMorada() == null)) {
            throw new IllegalArgumentException("Morada is required for client Utilizadors");
        }
    }

    private Utilizador buildUtilizadorFromType(String type) {
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "client" -> new Cliente();
            case "driver" -> new Entregador();
            case "restaurantadmin", "restaurant_admin", "restaurant owner", "restaurantowner", "admin" -> new RestauranteAdmin();
            default -> throw new IllegalArgumentException("Invalid Utilizador type: " + type);
        };
    }

    private void applyCommonFields(Utilizador Utilizador, UtilizadorRegistoDTO dto) {
        Utilizador.setName(dto.getName());
        Utilizador.setEmail(dto.getEmail());
        Utilizador.setPassword(dto.getPassword());
        applyMorada(Utilizador, dto.getMorada());
    }

    private void applyMorada(Utilizador Utilizador, MoradaDTO dto) {
        if (Utilizador instanceof Cliente client && dto != null) {
            Morada address = new Morada();

            address.setStreet(dto.getStreet());
            // optional fields
            if (dto.getNumber() != null) address.setNumber(dto.getNumber());
            if (dto.getComplement() != null) address.setComplement(dto.getComplement());
            //
            address.setCountry(dto.getCountry());
            address.setDistrict(dto.getDistrict());
            address.setCity(dto.getCity());
            address.setPostalCode(dto.getPostalCode());

            client.setMorada(address);
        }
    }
}
