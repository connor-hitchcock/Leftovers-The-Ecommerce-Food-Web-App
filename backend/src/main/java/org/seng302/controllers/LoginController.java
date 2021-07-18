package org.seng302.controllers;

import net.minidev.json.JSONObject;
import org.seng302.entities.Account;
import org.seng302.entities.User;
import org.seng302.persistence.AccountRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.seng302.tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * This class handles requests from the appliation's login page.
 */
@RestController
public class LoginController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public LoginController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check that the entered email exists and the password is correct. If they are, respond with status code 200: OK and
     * a cookie with the user's authentication token. If they are not, respond with status code 400: Bad request.
     * @param userinfo JSON object with user's email and password.
     * @param response HTTP response.
     */
    @PostMapping("/login")
    public void login(@RequestBody JSONObject userinfo, HttpServletRequest request, HttpServletResponse response) {
        String email = userinfo.getAsString("email");
        String password = userinfo.getAsString("password");
        Account matchingAccount = accountRepository.findByEmail(email);
        if (matchingAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no account associated with this email");
        } else {
            PasswordAuthenticator.verifyPassword(password, matchingAccount.getAuthenticationCode());

            // If they are a DGAA, set their permissions
            if (matchingAccount.getRole().equals("defaultGlobalApplicationAdmin")) {
                AuthenticationTokenManager.setAuthenticationToken(request, response);
                AuthenticationTokenManager.setAuthenticationTokenDGAA(request);
            } else {
                // Must be a user
                Optional<User> accountAsUser = userRepository.findById(matchingAccount.getUserID());
                AuthenticationTokenManager.setAuthenticationToken(request, response, accountAsUser.orElseThrow());
            }
            try {
                response.setStatus(200);
                response.setContentType("application/json");
                response.getWriter().write("{\"userId\":" + matchingAccount.getUserID() + "}");
                response.getWriter().flush();
            } catch (IOException e) {
                e.getMessage();
            }

        }
    }
}
