package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Account;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.exceptions.EmailInUseException;
import org.seng302.exceptions.UserNotFoundException;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.seng302.tools.SearchHelper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(UserController.class.getName());

    public UserController(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    /**
     * Check if all the details which the user has entered in the registration form are valid. If they are, add the user's
     * information to the database's account and user tables.
     * @param userinfo A Json object containing all of the user's details from the registration form.
     */
    @PostMapping("/users")
    public void register(@RequestBody JSONObject userinfo, HttpServletRequest request, HttpServletResponse response) {
        logger.info("Register");
        logger.info(userinfo.getAsString("bio"));
        try {
            Account.checkEmailUniqueness(userinfo.getAsString("email"), userRepository);
        } catch (EmailInUseException inUseException) {
            logger.error(inUseException.getMessage());
            throw inUseException;
        }
        try {
            JSONObject rawAddress;
            try {
                rawAddress = new JSONObject((Map<String, ?>) userinfo.get("homeAddress"));
            } catch (ClassCastException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Location not provided");
            }

            Location homeAddress = Location.parseLocationFromJson(rawAddress);


            User user = new User.Builder()
                    .withFirstName(userinfo.getAsString("firstName"))
                    .withMiddleName(userinfo.getAsString("middleName"))
                    .withLastName(userinfo.getAsString("lastName"))
                    .withNickName(userinfo.getAsString("nickname"))
                    .withBio(userinfo.getAsString("bio"))
                    .withAddress(homeAddress)
                    .withPhoneNumber(userinfo.getAsString("phoneNumber"))
                    .withDob(userinfo.getAsString("dateOfBirth"))
                    .withEmail(userinfo.getAsString("email"))
                    .withPassword(userinfo.getAsString("password"))
                    .build();
            User newUser = userRepository.save(user);
            AuthenticationTokenManager.setAuthenticationToken(request, response, newUser);
            response.setStatus(201);
            logger.info("User has been registered.");
        } catch (ResponseStatusException responseError) {
            logger.error(responseError.getMessage());
            throw responseError;
        } catch (DateTimeParseException e) {
            logger.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not process date of birth.");
        }

    }

    /**
     * REST GET method to fetch a singular User
     * @param id The ID of the user
     * @return User with corresponding Id
     */
    @GetMapping("/users/{id}")
    public JSONObject getUserById(@PathVariable Long id, HttpServletRequest session) {
        logger.info("Get user by id");
        AuthenticationTokenManager.checkAuthenticationToken(session);

        logger.info(() -> String.format("Retrieving user with ID %d.", id));
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            UserNotFoundException notFound = new UserNotFoundException();
            logger.error(notFound.getMessage());
            throw notFound;
        } else {
            if (AuthenticationTokenManager.sessionCanSeePrivate(session, user.get().getUserID())) {
                return user.get().constructPrivateJson(true);
            } else {
                return user.get().constructPublicJson(true);
            }

        }
    }

    /**
     * REST GET method to get the number of users from the search query
     * @param searchQuery The search term
     * @return The total number of users
     */
    @GetMapping("/users/search/count")
    public JSONObject getSearchCount(HttpServletRequest session, @RequestParam("searchQuery") String searchQuery) {
        AuthenticationTokenManager.checkAuthenticationToken(session);
        logger.info(() -> String.format("Performing search for \"%s\" and getting search count", searchQuery));
        List<User> queryResults;
        queryResults = SearchHelper.getSearchResultsOrderedByRelevance(searchQuery, userRepository, false);

        queryResults = SearchHelper.removeDGAAAccountFromResults(queryResults);

        JSONObject count = new JSONObject();
        count.put("count", queryResults.size());
        return count;
    }


    /**
     * REST GET method to search for users matching a search query
     * @param searchQuery The search term
     * @param page The page number in the results to be returned (defaults to one)
     * @param resultsPerPage The number of results that should be in the returned list (defaults to 15).
     * @param orderBy The name of the attribute to order the search results by.
     * @param reverse String representation of boolean indicating whether results should be in reverse order.
     * @return List of matching Users
     */
    @GetMapping("/users/search")
    public JSONArray searchUsersByName(HttpServletRequest session,
                                @RequestParam("searchQuery") String searchQuery,
                                @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer resultsPerPage,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(required = false) Boolean reverse) {

        AuthenticationTokenManager.checkAuthenticationToken(session); // Check user auth

        logger.info(() -> String.format("Performing search for \"%s\"", searchQuery));
        List<User> queryResults;
        if (orderBy == null || orderBy.equals("relevance")) {
            queryResults = SearchHelper.getSearchResultsOrderedByRelevance(searchQuery, userRepository, reverse);
        } else {
            Specification<User> spec = SearchHelper.constructUserSpecificationFromSearchQuery(searchQuery);
            Sort userSort = SearchHelper.getSort(orderBy, reverse);
            queryResults = userRepository.findAll(spec, userSort);
        }

        queryResults = SearchHelper.removeDGAAAccountFromResults(queryResults);

        List<User> pageInResults = SearchHelper.getPageInResults(queryResults, page, resultsPerPage);
        JSONArray publicResults = new JSONArray();
        for (User user : pageInResults) {
            if (AuthenticationTokenManager.sessionCanSeePrivate(session, user.getUserID())) {
                publicResults.appendElement(user.constructPrivateJson(true));
            } else {
                publicResults.appendElement(user.constructPublicJson(true));
            }
        }
        return publicResults;
    }


    /**
     * Promotes a single user to role "Admin"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to promote
     */
    @PutMapping("/users/{id}/makeAdmin")
    public void makeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
        changeUserPrivilege(session, id, "globalApplicationAdmin");
    }

    /**
     * Revokes a single user from role "Admin" to role "User"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to demote
     */
    @PutMapping("/users/{id}/revokeAdmin")
    public void revokeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
        changeUserPrivilege(session, id, "user");
    }

    /**
     * Changes the role of a user
     * @param request The HTTP Request
     * @param id The id of the user
     * @param newRole The new role of the user
     */
    void changeUserPrivilege(HttpServletRequest request, long id, String newRole) {
        AuthenticationTokenManager.checkAuthenticationToken(request); // Ensure user is logged on
        AuthenticationTokenManager.checkAuthenticationTokenDGAA(request); // Ensure user is the DGAA

        logger.info(() -> String.format("Changing user %d role to %s.", id, newRole));
        long userId = id;
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            UserNotFoundException notFoundException = new UserNotFoundException("The requested user does not exist");
            logger.error(notFoundException.getMessage());
            throw notFoundException;
        } else {
            user.get().setRole(newRole);
            userRepository.save(user.get());
        }
    }
}