package pt.ul.fc.css.tascaeats.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @Column(unique = true)
    private String email;
    // TODO store password hash not real password? also not needed for this phase of project
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;


    protected User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
