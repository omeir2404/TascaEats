package pt.ul.fc.css.tascaeats.domain.user;


import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;


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
                    new RestaurantAdmin(userRegistrationDTO.getName(), userRegistrationDTO.getEmail(), userRegistrationDTO.getPassword(), userRegistrationDTO.getAddress());
            case "Driver" ->
                    new Driver(userRegistrationDTO.getName(), userRegistrationDTO.getEmail(), userRegistrationDTO.getPassword(), userRegistrationDTO.getAddress());
            default -> throw new IllegalArgumentException("Invalid user type: " + userRegistrationDTO.getType());
        };

    }
}
