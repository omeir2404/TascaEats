package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Driver extends User {

    @Getter
    @Setter
    boolean available;

     public Driver(String name, String email, String password) {
        super(name, email, password);
    }

    public Driver() {
    }

    @Override
    public boolean equals(Object obj)    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Driver driver = (Driver) obj;
        return this.getEmail().equals(driver.getEmail());
    }

    @Override
    public int hashCode() {
        return this.getEmail().hashCode();
    }
}
