// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EstadoRestauranteTest {

    @Test
    @DisplayName("EstadoRestaurante deve conter os valores esperados")
    void deveConterOsValoresEsperados() {
        assertArrayEquals(
                new EstadoRestaurante[]{
                        EstadoRestaurante.ABERTO,
                        EstadoRestaurante.FECHADO,
                        EstadoRestaurante.RESERVADO
                },
                EstadoRestaurante.values()
        );
    }

}