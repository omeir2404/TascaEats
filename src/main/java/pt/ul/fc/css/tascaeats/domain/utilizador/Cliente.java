package pt.ul.fc.css.tascaeats.domain.utilizador;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Cliente extends Utilizador {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Morada address;

    public Cliente(String name, String email, String password, Morada address) {
        super(name, email, password);
        this.address = address;
    }
    
    public Cliente() {}

    public Morada getAddress() {
        return address;
    }

    public void setAddress(Morada address) {
        this.address = address;
    }

}
