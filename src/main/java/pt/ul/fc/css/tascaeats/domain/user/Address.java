package pt.ul.fc.css.tascaeats.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String street;
    @Nullable
    private String number;
    @Nullable
    private String complement;
    private String country;
    private String district;
    private String city;
    private String postalCode;

    @JsonIgnore
    @OneToOne(mappedBy = "address")
    private Client client;
}