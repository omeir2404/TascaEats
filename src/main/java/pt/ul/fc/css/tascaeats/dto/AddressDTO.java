package pt.ul.fc.css.tascaeats.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class AddressDTO {

    String street;
    @Nullable
    String number;
    @Nullable
    String complement;
    String country;
    String district;
    String city;
    String postalCode;
}
