// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain.utilizador;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cliente extends Utilizador {

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Morada> addresses = new ArrayList<>();

    public Cliente(String name, String email, String password, Morada address) {
        super(name, email, password);
    }
    
    public Cliente() {}

    public List <Morada> getAddresses(){
        return addresses;
    }

    public void setAddresses(List<Morada> addresses){
        this.addresses = addresses;
    }

    public void addAdresses(Morada address){
        addresses.add(address);
        address.setClient(this);
    }

    public void removeAdresses(Morada address){
        addresses.remove(address);
        address.setClient(null);
    }

}
