// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.events;

public record EntregaAtualizadaEvent(
        Long pedidoId,
        Long entregadorId,
        String estadoPedido,
        String estadoEntrega
) {}
