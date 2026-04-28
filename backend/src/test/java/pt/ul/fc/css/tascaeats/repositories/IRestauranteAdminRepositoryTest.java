// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IRestauranteAdminRepository;

@DataJpaTest
public class IRestauranteAdminRepositoryTest {

    @Autowired
    private IRestauranteAdminRepository restauranteAdminRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("A: Procurar administrador por email.")
    void findByEmailDeveDevolverAdminExistente() {
        RestauranteAdmin admin = new RestauranteAdmin("Admin", "admin@tasca.pt", "password123");
        entityManager.persist(admin);
        entityManager.flush();

        RestauranteAdmin resultado = restauranteAdminRepository.findByEmail("admin@tasca.pt");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(admin.getId());
        assertThat(resultado.getEmail()).isEqualTo("admin@tasca.pt");
        assertThat(resultado.getName()).isEqualTo("Admin");
    }

    @Test
    @DisplayName("B: Procurar administrador com email inexistente deve devolver null.")
    void findByEmailComEmailInexistenteDeveDevolverNull() {
        entityManager.persist(new RestauranteAdmin("Admin 2", "outro@tasca.pt", "password123"));
        entityManager.flush();

        RestauranteAdmin resultado = restauranteAdminRepository.findByEmail("inexistente@tasca.pt");

        assertThat(resultado).isNull();
    }

}