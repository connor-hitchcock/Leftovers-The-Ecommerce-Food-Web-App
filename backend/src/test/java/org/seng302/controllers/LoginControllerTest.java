package org.seng302.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.seng302.entities.Account;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.persistence.AccountRepository;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.HttpSession;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws ParseException {
        businessRepository.deleteAll(); // Do this to prevent table constraint issues
        User john = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        Account sameEmailAccount = accountRepository.findByEmail("johnsmith99@gmail.com");
        if (sameEmailAccount != null) {
            accountRepository.delete(sameEmailAccount);
        }
        accountRepository.save(john);
    }

    /**
     * Verify that when a login request is recieved with a valid username and password the user will be logged in.
     * Also checks an authentication token of length 32 is returned in the response body
     * @throws Exception
     */
    @Test
    void loginWithValidUsernameAndPasswordTest() throws Exception {
        String loginBody = "{\"email\": \"johnsmith99@gmail.com\", \"password\": \"1337-H%nt3r2\"}";

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        String authToken = result.getResponse().getCookie("AUTHTOKEN").getValue();
        assertEquals(32, authToken.length());
        // Might change to cookies within the future
    }

    /**
     * Verify that when a login request is recieved with an email which is not stored in the account table the user
     * will not be logged in and status code 400 will be returned.
     * @throws Exception
     */
    @Test
    void loginWithIncorrectPasswordTest() throws Exception {
        String loginBody = "{\"email\": \"johnsmith99@gmail.com\", \"password\": \"Wrong password\"}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Password is incorrect"));
    }

    /**
     * Verify that when a login request is recieved with an email which is stored in the account table and an incorrect
     * password for that email the user will not be logged in and status code 400 will be returned.
     * @throws Exception
     */
    @Test
    void loginWithIncorrectEmailTest() throws Exception {
        String loginBody = "{\"email\": \"johnsmith100@gmail.com\", \"password\": \"1337-H%nt3r2\"}";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("There is no account associated with this email"));
    }

    /**
     * Verify that the session of a logged in account contains the role "user" when the logged in account
     * is a default user
     * @throws Exception
     */
    @Test
    void loginWithDefaultUserSessionRoleIsUser() throws Exception {
        String loginBody = "{\"email\": \"johnsmith99@gmail.com\", \"password\": \"1337-H%nt3r2\"}";

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        HttpSession session = result.getRequest().getSession();
        assertEquals("user", session.getAttribute("role"));
        // Might change to cookies within the future
    }

    /**
     * Verify that the session contains the logged in account's ID
     * @throws Exception
     */
    @Test
    void loginWithDefaultUserSessionIdIsUserId() throws Exception {
        String loginBody = "{\"email\": \"johnsmith99@gmail.com\", \"password\": \"1337-H%nt3r2\"}";
        Account user = accountRepository.findByEmail("johnsmith99@gmail.com");
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        HttpSession session = result.getRequest().getSession();
        assertEquals(user.getUserID(), session.getAttribute("accountId"));
        // Might change to cookies within the future
    }

    /**
     * Verify that when logging in as a user with role "globalApplicationAdmin", the session role is "globalApplicationAdmin"
     * @throws Exception
     */
    @Test
    void loginWithAdminUserSessionRoleIsAdmin() throws Exception {
        String loginBody = "{\"email\": \"johnsmith99@gmail.com\", \"password\": \"1337-H%nt3r2\"}";
        User user = userRepository.findByEmail("johnsmith99@gmail.com");
        user.setRole("globalApplicationAdmin");
        userRepository.save(user);

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        HttpSession session = result.getRequest().getSession();
        assertEquals("globalApplicationAdmin", session.getAttribute("role"));
        // Might change to cookies within the future
    }
}