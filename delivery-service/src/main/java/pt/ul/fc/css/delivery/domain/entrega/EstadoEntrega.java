// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.delivery.domain.entrega;

public enum EstadoEntrega {
    ATRIBUIDA("Atribuida"),
    A_CAMINHO("A caminho"),
    ENTREGUE("Entregue");

    private final String descricao;

    EstadoEntrega(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
