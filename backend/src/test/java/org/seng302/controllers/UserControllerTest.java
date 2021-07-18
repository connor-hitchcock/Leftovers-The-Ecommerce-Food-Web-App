package org.seng302.controllers;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;
    private User testUser;

    //region Helper Functions

    /**
     * Add a user object to the userRepository and construct an authorization token to be used for this session.
     *
     * @throws ParseException
     */
    @BeforeEach
    void setUp() throws ParseException, IOException {
        setUpAuthCode();
        setUpTestUser();
    }

    /**
     * This method creates an authentication code for sessions and cookies.
     */
    private void setUpAuthCode() {
        StringBuilder authCodeBuilder = new StringBuilder();
        authCodeBuilder.append("0".repeat(64));
        String authCode = authCodeBuilder.toString();
        sessionAuthToken.put("AUTHTOKEN", authCode);
        authCookie = new Cookie("AUTHTOKEN", authCode);
    }

    /**
     * Tags a session as dgaa
     */
    private void setUpDGAAAuthCode() {
        sessionAuthToken.put("role", "defaultGlobalApplicationAdmin");
    }

    /**
     * Simulates logging in as account with given accountId
     * @param accountId ID of the account to "log-in" as
     */
    private void setUpSessionAccountId(Long accountId) {
        sessionAuthToken.put("accountId", accountId);
    }
    private void setUpSessionAsAdmin() {
        sessionAuthToken.put("role", "globalApplicationAdmin");
    }

    /**
     * This method creates a user and adds it to the repository.
     *
     * @throws ParseException
     */
    private void setUpTestUser() throws ParseException {
        testUser = new User.Builder()
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
        businessRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser);
    }

    /**
     * This method returns a list of users read from the file with the given name.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    private List<User> readUsersFromTestFile(String filename) throws IOException {
        List<User> userList = new ArrayList<User>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(filename));
        while ((row = csvReader.readLine()) != null) {
            try {
                String[] userData = row.split("\\|");
                User user = new User.Builder().withFirstName(userData[0]).withMiddleName(userData[1]).withLastName(userData[2]).withNickName(userData[3])
                        .withEmail(userData[4]).withPassword(userData[5]).withAddress(Location.covertAddressStringToLocation(userData[6])).withDob(userData[7]).build();
                userList.add(user);
            } catch (Exception e) {
            }
        }
        csvReader.close();
        return userList;
    }

    private List<JSONObject> readJSONFromTestFile(String filename) throws IOException {
        List<JSONObject> jsonList = new ArrayList<>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(filename));
        csvReader.readLine();
        while ((row = csvReader.readLine()) != null) {
            try {
                String[] userData = row.split(",");
                HashMap<String, String> jsonMap = new HashMap<>();
                jsonMap.put("firstName", userData[0]);
                jsonMap.put("middleName", userData[1]);
                jsonMap.put("lastName", userData[2]);
                jsonMap.put("nickname", userData[3]);
                jsonMap.put("email", userData[4]);
                jsonMap.put("password", userData[5]);
                jsonMap.put("homeAddress", userData[6]);
                jsonMap.put("dateOfBirth", userData[7]);
                JSONObject jsonObject = new JSONObject(jsonMap);
                jsonList.add(jsonObject);
            } catch (Exception e) {

            }
        }
        csvReader.close();
        return jsonList;
    }


    /**
     * This method asserts that the id, firstName, lastName, middleName, nickname, email, and bio
     * attributes of the given user are the same as the equivalent attributes in the given JSON object.
     *
     * @param user A User to be compared with json.
     * @param json A JSONObject to be compared with user.
     */
    private void assertUserEquivalentToJSONObject(User user, JSONObject json) {
        assertEquals(user.getFirstName(), json.getAsString("firstName"));
        assertTrue((user.getMiddleName() == null && json.getAsString("middleName") == null) || (user.getMiddleName().equals(json.getAsString("middleName"))));
        assertEquals(user.getLastName(), json.getAsString("lastName"));
        assertTrue((user.getNickname() == null && json.getAsString("nickname") == null) || (user.getNickname().equals(json.getAsString("nickname"))));
        assertEquals(user.getEmail(), json.getAsString("email"));
        assertTrue((user.getBio() == null && json.getAsString("bio") == null) || (user.getBio().equals(json.getAsString("bio"))));
    }
    //endregion

    //region GetUser Tests

    /**
     * Converts a given User object into a JSONObject
     *
     * @param user A User object to be converted
     * @return A JSONObject with the user's details
     */
    private JSONObject convertUserToJSONObject(User user) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JSONObject userJSON = new JSONObject();
        userJSON.put("firstName", user.getFirstName());
        userJSON.put("middleName", user.getMiddleName());
        userJSON.put("lastName", user.getLastName());
        userJSON.put("bio", user.getBio());
        userJSON.put("email", user.getEmail());
        userJSON.put("dateOfBirth", dateFormat.format(user.getDob()));
        userJSON.put("phoneNumber", user.getPhNum());
        userJSON.put("homeAddress", user.getAddress());
        return userJSON;
    }

    /**
     * Test that a get request on path user/{id} returns the user with the given ide number provided that the user with
     * the given id number exists and the authentication token for the session is valid.
     *
     * sa
     * @throws Exception
     */
    @Test
    void getUserWhenUserExistsAndSessionValid() throws Exception {
        User expectedUser = userRepository.findByEmail("johnsmith99@gmail.com");

        MvcResult result = mockMvc.perform(get(String.format("/users/%d", expectedUser.getUserID()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertUserEquivalentToJSONObject(testUser, json);
    }

      //endregion

    //region UserSearch Tests

    /**
     * Verify that when a GET request is made to "/users/search", and the orderBy parameter is "firstName", and the
     * reverse parameter is true, a list of users matching the search term is returned in reverse alphabetical order by
     * their firstName attribute.
     */
    @Test
    void getUserSearchOrderByFirstNameReverseTrueTest() throws Exception {
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        userRepository.deleteAll();
        for (User user : userList) {
            userRepository.save(user);
        }

        MvcResult result = mockMvc.perform(get("/users/search")
                .param("searchQuery", "andy")
                .param("orderBy", "firstName")
                .param("reverse", "true")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray jsonArray = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        JSONObject firstJsonObject = (JSONObject) jsonArray.get(0);
        String previousFirstName = firstJsonObject.getAsString("firstName");
        for (int i = 1; i < jsonArray.size(); i++) {
            JSONObject currentJsonObject = (JSONObject) jsonArray.get(i);
            String currentFirstName = currentJsonObject.getAsString("firstName");
            assertTrue(currentFirstName.compareTo(previousFirstName) <= 0);
            previousFirstName = currentFirstName;
        }
    }

    /**
     * Verify that when a GET request is made to "/users/search", and the orderBy parameter is "email", and the
     * reverse parameter is false, a list of users matching the search term is returned in alphabetical order by their
     * email attribute.
     */
    @Test
    void getUserSearchOrderByEmailReverseFalseTest() throws Exception {
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        userRepository.deleteAll();
        for (User user : userList) {
            userRepository.save(user);
        }

        MvcResult result = mockMvc.perform(get("/users/search")
                .param("searchQuery", "andy")
                .param("orderBy", "email")
                .param("reverse", "false")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray jsonArray = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        JSONObject firstJsonObject = (JSONObject) jsonArray.get(0);
        String previousEmail = firstJsonObject.getAsString("email");
        for (int i = 1; i < jsonArray.size(); i++) {
            JSONObject currentJsonObject = (JSONObject) jsonArray.get(i);
            String currentEmail = currentJsonObject.getAsString("email");
            assertTrue(currentEmail.compareTo(previousEmail) >= 0);
            previousEmail = currentEmail;
        }
    }

    /**
     * Verify that when a GET request is made to "/users/search", and no orderby parameter is specified, a list of users
     * matching the search term is returned ordered by their relevance, and then their id for users with the same relevance
     */
    @Test
    void getUserSearchOrderByRelevanceTest() throws Exception {
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        userRepository.deleteAll();
        for (User user : userList) {
            userRepository.save(user);
        }

        MvcResult result = mockMvc.perform(get("/users/search")
                .param("searchQuery", "Kirsty or Andy")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray jsonArray = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        JSONObject firstJsonObject = (JSONObject) jsonArray.get(0);
        assertUserEquivalentToJSONObject(userList.get(2), firstJsonObject);
        JSONObject secondJsonObject = (JSONObject) jsonArray.get(1);
        int previousId = Integer.parseInt(secondJsonObject.getAsString("id"));
        for (int i = 2; i < jsonArray.size(); i++) {
            JSONObject currentJsonObject = (JSONObject) jsonArray.get(i);
            int currentId = Integer.parseInt(currentJsonObject.getAsString("id"));
            assertTrue(currentId > previousId);
            previousId = currentId;
        }
    }

    /**
     * Verify that when a GET request is made to "/users/search", and the page parameter is 2, the second page in the
     * results will be returned.
     */
    @Test
    void getUserSearchPageTwoTest() throws Exception {
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        userRepository.deleteAll();
        for (User user : userList) {
            userRepository.save(user);
        }

        MvcResult result = mockMvc.perform(get("/users/search")
                .param("searchQuery", "andy")
                .param("page", "2")
                .param("resultsPerPage", "5")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray jsonArray = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        assertEquals(2, jsonArray.size());
        JSONObject firstJsonObject = (JSONObject) jsonArray.get(0);
        assertUserEquivalentToJSONObject(userList.get(6), firstJsonObject);
        JSONObject secondJsonObject = (JSONObject) jsonArray.get(1);
        assertUserEquivalentToJSONObject(userList.get(7), secondJsonObject);
    }

    /**
     * Verify that when a GET request is made to "/users/search", and the number of results per page is 5, and there are
     * more than 5 results matching the search term, 5 results will be returned.
     */
    @Test
    void getUserSearchFiveResultsPerPageTest() throws Exception {
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        userRepository.deleteAll();
        for (User user : userList) {
            userRepository.save(user);
        }

        MvcResult result = mockMvc.perform(get("/users/search")
                .param("searchQuery", "andy")
                .param("page", "1")
                .param("resultsPerPage", "5")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray jsonArray = (JSONArray) parser.parse(result.getResponse().getContentAsString());
        assertEquals(5, jsonArray.size());
    }

    /**
     * Verify that a GET request is made to "/users/search/count" it returns a json object with a single field "count".
     * Where this count field is equal to the number of users matching the query.
     */
    @Test
    void getUserSearchCountTest() throws Exception {
        List<User> userList = readUsersFromTestFile("src//test//testFiles//UsersControllerTestData.csv");
        userRepository.deleteAll();
        userRepository.saveAll(userList);

        MvcResult result = mockMvc.perform(get("/users/search/count")
                .param("searchQuery", "andy")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertEquals(7, jsonObject.getAsNumber("count"));
        assertEquals(1, jsonObject.size());
    }

    /**
     * Tests if several valid users are successively registered and stored within the database
     * @throws Exception
     */
    //Getting a strange error where a date is being created after the for loop has concluded?
    @Test @Ignore
    void registerAccountWithValidRequest() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        List<JSONObject> jsonObjectList = readJSONFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        for (int i = 0; i < jsonObjectList.size(); i++) {
            JSONObject userJSON = jsonObjectList.get(i);
            User user = userList.get(i);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
            User expectedUser = userRepository.findByEmail(user.getEmail());
            assertUserEquivalentToJSONObject(expectedUser, userJSON);
        }
    }

    /**
     * Tests if a user registering with an existing email with send back a 409 conflict error response code.
     * @throws Exception
     */
    @Test @Ignore
    void testConflictErrorOnRegisterWithSameEmail() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        List<JSONObject> jsonObjectList = readJSONFromTestFile("src/test/testFiles/UsersControllerTestData.csv");
        for (int i = 0; i < jsonObjectList.size(); i++) {
            JSONObject userJSON = jsonObjectList.get(i);
            User user = userList.get(i);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());
        }
    }

    /**
     * Tests if a user registering with an invalid first name will send back a 400 bad request response code and the
     * custom error message associated with providing an invalid first name.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidFirstName() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidFirstName" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("The first name must not be empty, be less then 16 characters, and only contain letters."));
        }
    }

    /**
     * Tests if a user registering with an invalid middle name will send back a 400 bad request response code and the
     * custom error message associated with providing an invalid middle name.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidMiddleName() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidMiddleName" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("The middle name must not be empty, be less then 16 characters, and " +
                            "only contain letters."));
        }
    }

    /**
     * Tests if a user registering with an invalid last name will send back a 400 bad request response code and the
     * custom error message associated with providing an invalid last name.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidLastName() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidLastName" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("The last name must not be empty, be less then 16 characters, and only" +
                            " contain letters."));
        }
    }

    /**
     * Tests if a user registering with an invalid nickname will send back a 400 bad request response code and the
     * custom error message associated with providing an invalid nickname.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidNickname() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidNickname" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("The nickname must not be empty, be less then 16 characters, and only " +
                            "contain letters."));
        }
    }

    /**
     * Tests if a user registering with an invalid bio will send back a 400 bad request response code and the custom
     * error message associated with providing an invalid bio.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidBio() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidBio" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("The bio must be less than 255 characters long, and only contain " +
                            "letters."));
        }
    }

    /**
     * Tests if a user registering with an invalid date of birth will send back a 400 bad request response code and the
     * custom error message associated with providing an invalid date of birth.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidDOB() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidDOB" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("Your date of birth has been entered incorrectly"));
        }
    }

    /**
     * Tests if a user registering with an invalid phone number will send back a 400 bad request response code and the
     * custom error message associated with providing an invalid phone number.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidPhNum() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidPhoneNumber" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform( MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("Your phone number has been entered incorrectly"));
        }
    }

    /**
     * Tests if a user registering with an invalid address will send back a 400 bad request response code and the
     * custom error message associated with providing an invalid address.
     * @throws Exception
     */
    @Test
    void testRegisteringUserWithInvalidAddress() throws Exception {
        userRepository.deleteAll();
        List<User> userList = readUsersFromTestFile("src/test/testFiles/UsersControllerTestDataInvalidAddress" +
                ".csv");

        for (User user : userList) {
            JSONObject userJSON = convertUserToJSONObject(user);
            mockMvc.perform(MockMvcRequestBuilders
                    .post("/users")
                    .content(userJSON.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("Your address has been entered incorrectly"));
        }
    }

    //endregion

    //region MakeUserAdmin Tests

    /**
     * Assert makeAdmin appropriately changes a users role
     * WHEN: Session is valid, session has DGAA priv
     * @throws Exception
     */
    @Test
    void makeAdminMakesUserAdminWhenPrivilege() throws Exception {
        setUpDGAAAuthCode(); // give us DGAA auth priv
        User expectedUser = userRepository.findByEmail("johnsmith99@gmail.com");

        // perform
        mockMvc.perform(MockMvcRequestBuilders.put(String.format("/users/%d/makeAdmin",expectedUser.getUserID()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        // Check if user has been updated
        User updatedUser = userRepository.findByEmail("johnsmith99@gmail.com");
        assertEquals( "globalApplicationAdmin", updatedUser.getRole());
    }

    /**
     * Assert makeAdmin throws 403 when session does not have DGAA priv
     * WHEN: Session is valid, session DOES NOT have DGAA priv
     * @throws Exception
     */
    @Test
    void makeAdminThrowsWhenNoDGAAPrivilege() throws Exception {
        User expectedUser = userRepository.findByEmail("johnsmith99@gmail.com");

        // perform
        mockMvc.perform(MockMvcRequestBuilders.put(String.format("/users/%d/makeAdmin",expectedUser.getUserID()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    /**
     * Assert makeAdmin throws 401 when session has no auth
     * WHEN: Session is invalid
     * @throws Exception
     */
    @Test
    void makeAdminThrowsWhenNoAuth() throws Exception {

        User expectedUser = userRepository.findByEmail("johnsmith99@gmail.com");

        // perform
        mockMvc.perform(MockMvcRequestBuilders.put(String.format("/users/%d/makeAdmin",expectedUser.getUserID()))
                .cookie(authCookie))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    /**
     * Assert makeAdmin throws 406 when user is not found
     * WHEN: Session is valid, session has DGAA priv
     * @throws Exception
     */
    @Test
    void makeAdminThrowsWhenUserNotExist() throws Exception {
        setUpDGAAAuthCode(); // give us dgaa auth
        // perform
        mockMvc.perform(MockMvcRequestBuilders.put("/users/99999/makeAdmin")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

    //endregion

    //region RevokeAdmin Tests
    /**
     * For these tests, we don't need to test edge cases as they are covered by MakeAdmin tests (same function)
     */
    /**
     * Verify user's role is revoked when revoke admin is called
     * @throws Exception
     */
    @Test
    void revokeAdminRevokesUserWhenValidAuth() throws Exception {
        setUpDGAAAuthCode(); // give us dgaa auth
        // Set up our user to have admin rights
        User john = userRepository.findByEmail("johnsmith99@gmail.com");
        john.setRole("globalApplicationAdmin");
        userRepository.save(john);

        // perform
        mockMvc.perform(MockMvcRequestBuilders.put(String.format("/users/%d/revokeAdmin", john.getUserID()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        User newUser = userRepository.findByEmail("johnsmith99@gmail.com");
        assertEquals( "user", newUser.getRole()); // Should be role "user"
    }

    //endregion

    //region User Permissions test

    /**
     * When logged in as self, assert user can see their own DOB, phNum and role
     * @throws Exception
     */
    @Test
    void UserCanSeeOwnPrivateDetails() throws Exception {
        User user = userRepository.findByEmail("johnsmith99@gmail.com");
        setUpSessionAccountId(user.getUserID());

        // perform
        MvcResult result = mockMvc.perform(get(String.format("/users/%d", user.getUserID()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        String role = JsonPath.read(result.getResponse().getContentAsString(), "$.role");
        assertNotNull(role);
    }
    /**
     * When logged in as self, assert user can see their own DOB, phNum and role
     * @throws Exception
     */
    @Test
    void UserCantSeePrivateDetailsOfAnotherUser() throws Exception {
        User user = userRepository.findByEmail("johnsmith99@gmail.com");


        // perform
        MvcResult result = mockMvc.perform(get(String.format("/users/%d", user.getUserID()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        org.json.JSONObject json = new org.json.JSONObject(result.getResponse().getContentAsString());
        assertFalse(json.has("role"));
    }
    /**
     * When logged in as admin, assert user can see phNum and role of another user
     * @throws Exception
     */
    @Test
    void adminCanSeePrivateDetailsOfUser() throws Exception {
        User user = userRepository.findByEmail("johnsmith99@gmail.com");
        setUpSessionAsAdmin();
        // perform
        MvcResult result = mockMvc.perform(get(String.format("/users/%d", user.getUserID()))
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        org.json.JSONObject json = new org.json.JSONObject(result.getResponse().getContentAsString());
        // Result should contain role and DOB
        assertTrue(json.has("role"));
    }
    //endregion
}
