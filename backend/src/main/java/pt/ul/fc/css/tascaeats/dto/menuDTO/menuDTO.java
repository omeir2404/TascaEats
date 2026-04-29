// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

public class menuDTO{

    private Long id;

    @NotBlank(message = "O nome do menu é obrigatório")
    private String nome;

    private List<Long> produtosIds;

    public menuDTO(){

    }

    public menuDTO(Long id, String nome, List<Long> produtosIds) {
        this.id = id;
        this.nome = nome;
        this.produtosIds = produtosIds;
    }


    // ###################### GETTERS e SETTERS #####################################################################

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public List<Long> getProdutosIds() { return produtosIds; }

    public void setProdutosIds(List<Long> produtosIds) { this.produtosIds = produtosIds; }

    @Override
    public String toString() {
        return "CriarMenuDTO{" + "nome='" + nome + '\'' + ", produtosIds=" + produtosIds + '}';
    }

}
