package pt.ul.fc.css.tascaeats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.css.tascaeats.domain.Utilizador.Utilizador;
import pt.ul.fc.css.tascaeats.repository.UtilizadorRepository;

@Service
public class AuthService {
    @Autowired
    private UtilizadorRepository UtilizadorRepository;

    public Utilizador login(String email) {
        return UtilizadorRepository.findByEmail(email)
                .orElse( null);
    }
}
