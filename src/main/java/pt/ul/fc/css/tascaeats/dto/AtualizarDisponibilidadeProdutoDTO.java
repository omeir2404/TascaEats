package pt.ul.fc.css.tascaeats.dto;

import jakarta.validation.constraints.NotNull;

public class AtualizarDisponibilidadeProdutoDTO{
    
    @NotNull
    private Boolean disponivel;


    // *********GETTERS E SETTERS**********

    public Boolean getDisponivel(){
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel){
        this.disponivel = disponivel;
    }
}
