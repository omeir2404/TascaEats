// ##############################################################
// 					TASCAEATS      			         #
// 					CSS GRUPO: 25	     			    #
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.utilizadorRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;

public interface IRestauranteAdminRepository extends JpaRepository<RestauranteAdmin, Long> {
     RestauranteAdmin findByEmail(String email);
}
