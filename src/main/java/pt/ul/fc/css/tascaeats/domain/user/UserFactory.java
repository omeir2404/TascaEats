package pt.ul.fc.css.tascaeats.domain.user;

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

    public static AbstractUser createUser(String type) {
        return null;
    }
}
