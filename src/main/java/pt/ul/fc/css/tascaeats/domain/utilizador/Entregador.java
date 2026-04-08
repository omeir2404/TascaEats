package pt.ul.fc.css.tascaeats.domain.utilizador;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Entregador extends Utilizador {

    @Getter
    @Setter
    boolean available;

     public Entregador(String name, String email, String password) {
        super(name, email, password);
    }

    public Entregador() {
    }

    @Override
    public boolean equals(Object obj)    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Entregador driver = (Entregador) obj;
        return this.getEmail().equals(driver.getEmail());
    }

    @Override
    public int hashCode() {
        return this.getEmail().hashCode();
    }
}
