package pt.ul.fc.css.tascaeats.dto;

import jakarta.validation.constraints.NotNull;

public class AtualizarDisponibilidadeDTO{
    
    @NotNull
    private Boolean disponivel;

    public Boolean getDisponivel(){
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel){
        this.disponivel = disponivel;
    }
}
