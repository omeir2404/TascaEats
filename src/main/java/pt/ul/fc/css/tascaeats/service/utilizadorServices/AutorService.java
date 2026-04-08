package pt.ul.fc.css.tascaeats.service.utilizadorServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;

@Service
public class AutorService {
    @Autowired
    private IUtilizadorRepository UtilizadorRepository;

    public Utilizador login(String email) {
        return UtilizadorRepository.findByEmail(email)
                .orElse( null);
    }
}
