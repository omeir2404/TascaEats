package pt.ul.fc.css.tascaeats.domain.restaurante;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CatalogoEntregas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public CatalogoEntregas() {
    }

    public Long getId() {
        return id;
    }
}
