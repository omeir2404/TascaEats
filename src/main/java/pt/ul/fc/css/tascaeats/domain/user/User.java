package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @Column(unique = true)
    private String email;
    // TODO store password hash not real password? also not needed for this phase of project
    private String password;


    protected User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
