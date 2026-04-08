package pt.ul.fc.css.tascaeats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.css.tascaeats.domain.user.User;
import pt.ul.fc.css.tascaeats.repository.UserRepository;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public User login(String email) {
        return userRepository.findByEmail(email)
                .orElse( null);
    }
}
