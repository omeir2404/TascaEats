package pt.ul.fc.css.tascaeats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizadorRespostaDTO {
    private Long id;
    private String name;
    private String email;
    private String type;
    private Boolean available;
    private MoradaResponseDTO address;
}

