package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Address {
    @Id
    private Long id;

    private String street;
    private String city;
    private String postalCode;
    private String country;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Client user;

}