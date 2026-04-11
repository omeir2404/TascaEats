package pt.ul.fc.css.tascaeats.domain.utilizador;

import jakarta.persistence.Entity;

@Entity
public class Entregador extends Utilizador {

    private boolean available = true;

     public Entregador(String name, String email, String password) {
        super(name, email, password);
        this.available = true;
    }

    public Entregador() {
        this.available = true;
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

    public boolean isAvailable(){
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
