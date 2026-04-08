package pt.ul.fc.css.tascaeats.dto.restauranteDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;

public class AtualizarRestauranteDTO {

    @NotBlank(message = "O 'nome' do restaurante é obrigatório.")
    private String nome;

    @NotBlank(message = "O 'nif' do restaurante é obrigatório.")
    private String nif;

    @NotNull(message = "A 'morada' do restaurante é obrigatória.")
    @Valid
    private MoradaDTO morada;


    // *********GETTERS E SETTERS**********

    //Nome
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    //Nif
    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    //Morada
    public MoradaDTO getMorada() {
        return morada;
    }

    public void setMorada(MoradaDTO morada) {
        this.morada = morada;
    }
}