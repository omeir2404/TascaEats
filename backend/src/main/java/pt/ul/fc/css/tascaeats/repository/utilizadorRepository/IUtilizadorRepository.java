// ##############################################################
// 					    TASCAEATS      			         		#
// 					  CSS GRUPO: 25	     			    		#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.utilizadorRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;

import java.util.Optional;

@Repository
public interface IUtilizadorRepository extends JpaRepository<Utilizador, Long> {
        Optional<Utilizador> findByEmail(String email);

        //Buscar por nome
        List<Utilizador> findByNomeContainingIgnoreCase(String nome);

        //Buscar por tipo de utilizador
        @Query("SELECT u FROM Utilizador u WHERE TYPE(u) = :tipo")
        List<Utilizador> findByTipo(@Param("tipo") Class<? extends Utilizador> tipo);
}