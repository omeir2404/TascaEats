// ##############################################################
// 					TASCAEATS      			         #
// 					CSS GRUPO: 25	     			    #
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.utilizadorRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;

public interface IEntregadorRepository extends JpaRepository<Entregador, Long> {
     Entregador findByEmail(String email);
     Entregador findByAvailable(boolean available);

     // Filtra entregadores pelo número de entregas realizadas
     @Query("SELECT e FROM Entregador e WHERE (SELECT COUNT(en) FROM Entrega en WHERE en.entregadorId = e.id) = :num")
     List<Entregador> findByNumeroDeEntregas(@Param("num") int num);
}
