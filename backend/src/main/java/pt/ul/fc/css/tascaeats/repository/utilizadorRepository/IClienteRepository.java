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
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;

public interface IClienteRepository extends JpaRepository<Cliente, Long> {
     Cliente findByEmail(String email);

     // Número de pedidos realizados por cada cliente
     @Query("SELECT c FROM Cliente c WHERE size(c.pedidos) = :num")
     List<Cliente> findByNumeroDePedidos(@Param("num") int num);
}
