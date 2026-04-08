package pt.ul.fc.css.tascaeats.domain.Utilizador;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Cliente extends Utilizador {

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Morada address;

    public Cliente(String name, String email, String password, Morada address) {
        super(name, email, password);
        this.address = address;
    }
    
    public Cliente() {}

}
