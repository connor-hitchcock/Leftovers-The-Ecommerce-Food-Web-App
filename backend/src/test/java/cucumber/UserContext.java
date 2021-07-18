package cucumber;

import io.cucumber.java.Before;
import org.seng302.entities.Business;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class UserContext {
    private User lastUser = null;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setup() {
        lastUser = null;
    }

    /**
     * Returns the last modified user
     * @return Last modified user
     */
    public User getLast() {
        return lastUser;
    }

    /**
     * Saves a user using the user repository
     * Also sets the last user
     * @param user User to save
     * @return Saved user
     */
    public User save(User user) {
        lastUser = userRepository.save(user);
        return lastUser;
    }

}
