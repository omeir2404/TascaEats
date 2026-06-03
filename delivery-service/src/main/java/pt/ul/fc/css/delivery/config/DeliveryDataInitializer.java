// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.delivery.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.delivery.domain.entregador.Entregador;
import pt.ul.fc.css.delivery.repository.EntregadorRepository;

@Component
public class DeliveryDataInitializer implements ApplicationRunner {

    private final EntregadorRepository entregadorRepository;

    public DeliveryDataInitializer(EntregadorRepository entregadorRepository) {
        this.entregadorRepository = entregadorRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureEntregador("Entregador Tasca", "courier@tascaeats.local");
        ensureEntregador("Entregador Dois", "courier2@tascaeats.local");
    }

    private void ensureEntregador(String nome, String email) {
        if (entregadorRepository.findByEmail(email) == null) {
            entregadorRepository.save(new Entregador(nome, email, true));
        }
    }
}
