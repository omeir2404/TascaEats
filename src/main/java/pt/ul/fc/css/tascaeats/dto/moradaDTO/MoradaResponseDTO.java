package pt.ul.fc.css.tascaeats.dto.moradaDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoradaResponseDTO {
    private String street;
    private String number;
    private String complement;
    private String country;
    private String district;
    private String city;
    private String postalCode;
}

