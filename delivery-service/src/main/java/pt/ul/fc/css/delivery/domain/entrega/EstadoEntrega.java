package pt.ul.fc.css.delivery.domain.entrega;

public enum EstadoEntrega {
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
