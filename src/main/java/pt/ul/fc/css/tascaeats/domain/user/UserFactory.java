package pt.ul.fc.css.tascaeats.domain.user;


import org.springframework.stereotype.Component;
import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;

@Component
public class UserFactory {

    private static UserFactory instance;
    public static UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }
    private UserFactory() {
        /* This utility class should not be instantiated */
    }

    public static User createUser(UserRegistrationDTO userRegistrationDTO) {

        return switch (userRegistrationDTO.getType()) {
            case "Client" ->
                    new Client(userRegistrationDTO.getName(), userRegistrationDTO.getEmail(), userRegistrationDTO.getPassword(), userRegistrationDTO.getAddress());
            case "RestaurantAdmin" ->
                    new RestaurantAdmin(userRegistrationDTO.getName(), userRegistrationDTO.getEmail(), userRegistrationDTO.getPassword());
            case "Driver" ->
                    new Driver(userRegistrationDTO.getName(), userRegistrationDTO.getEmail(), userRegistrationDTO.getPassword());
            default -> throw new IllegalArgumentException("Invalid user type: " + userRegistrationDTO.getType());
        };

    }
}
