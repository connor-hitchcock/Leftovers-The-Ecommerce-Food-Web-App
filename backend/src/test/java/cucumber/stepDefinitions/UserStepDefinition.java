package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.UserContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.Assert;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.persistence.AccountRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class UserStepDefinition {

    @Autowired
    private UserContext userContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    private User theUser;
    private String userFirstName = "Bob";
    private String userMiddleName = "Bob";
    private String userLastName = "Bob";
    private String userNickname = "Bob";
    private String userEmail = "Bob@bob.com";
    private String userPassword = "B0bbbbbbb76#bb";
    private String userBio = "I am Bob";
    private String userDob = "1999-01-01";
    private String userPhNum = "0270000000";
    private Location userAddress = Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010");

    private Long userID;

    @Given("a user exists")
    public void a_user_exists() throws ParseException {
        var user = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("here@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        userContext.save(user);
    }

    @Given("the user with the email {string} does not exist")
    public void theUserWithTheEmailDoesNotExist(String email) {
        Assert.assertNull(userRepository.findByEmail(email));
    }

    @When("a user is getting created the first name is {string}")
    public void aUserIsGettingCreatedTheFirstNameIs(String firstName) { userFirstName = firstName; }

    @And("their middle name is {string}")
    public void theirMiddleNameIs(String middleName) { userMiddleName = middleName; }

    @And("their last name is {string}")
    public void theirLastNameIs(String lastName) { userLastName = lastName; }

    @And("their nickname is {string}")
    public void theirNicknameIs(String nickname) { userNickname = nickname; }

    @Given("a user with the email {string} is created")
    @And("their email is {string}")
    public void theirEmailIs(String email) { userEmail = email; }

    @And("their password is {string}")
    public void theirPasswordIs(String password) { userPassword = password; }

    @And("their bio is {string}")
    public void theirBioIs(String bio) { userBio = bio; }

    @And("their date of birth is {string}")
    public void theirDobIs(String dob) { userDob = dob; }

    @And("their phone number is {string}")
    public void theirPhNumIs(String phNum) { userPhNum = phNum; }

    @And("their address is {string}")
    public void theirAddressIs(String address) { userAddress = Location.covertAddressStringToLocation(address); }

    @And("the user is created")
    public void theUserIsCreated() throws ParseException {
        try {
            theUser = new User.Builder()
                    .withFirstName(userFirstName)
                    .withMiddleName(userMiddleName)
                    .withLastName(userLastName)
                    .withNickName(userNickname)
                    .withEmail(userEmail)
                    .withPassword(userPassword)
                    .withBio(userBio)
                    .withDob(userDob)
                    .withPhoneNumber(userPhNum)
                    .withAddress(userAddress)
                    .build();
            userRepository.save(theUser);
        } catch (ResponseStatusException | DataIntegrityViolationException ignored) {}
    }

    @Then("a user with the email {string} exists")
    public void aUserWithTheEmailExists(String email) { Assert.assertNotNull(userRepository.findByEmail(email)); }

    @Then("the user has the first name {string}")
    public void theUserHasTheFirstName(String firstName) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getFirstName(), firstName);
    }

    @Then("the user has the middle name {string}")
    public void theUserHasTheMiddleName(String middleName) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getMiddleName(), middleName);
    }

    @Then("the user has the last name {string}")
    public void theUserHasTheLastName(String lastName) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getLastName(), lastName);
    }

    @Then("the user has the nickname {string}")
    public void theUserHasTheNickname(String nickname) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getNickname(), nickname);
    }

    @Then("the user has the email {string}")
    public void theUserHasTheEmail(String email) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getEmail(), email);
    }

    @Then("the user has the password {string}")
    public void theUserHasThePassword(String password) throws NoSuchAlgorithmException {
        User user = userRepository.findByEmail(userEmail);
        String authenticationCode = PasswordAuthenticator.generateAuthenticationCode(password);
        Assert.assertEquals(user.getAuthenticationCode(), authenticationCode);
    }

    @Then("the user has the bio {string}")
    public void theUserHasTheBio(String bio) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getBio(), bio);
    }

    @Then("the user has the date of birth {string}")
    public void theUserHasTheDob(String dob) {
        User user = userRepository.findByEmail(userEmail);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Assert.assertEquals(user.getDob(), LocalDate.parse(dob, dateTimeFormatter));
    }

    @Then("the user has the phone number {string}")
    public void theUserHasThePhNum(String phNum) {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getPhNum(), phNum);
    }

    @Then("the user has the address {string}")
    public void theUserHasTheAddress(String address) {
        User user = userRepository.findByEmail(userEmail);
        Location location = Location.covertAddressStringToLocation(address);
        location.setId(user.getAddress().getId());
        Assert.assertEquals(user.getAddress(), location);
    }

    @Given("the user possesses the email {string}")
    public void theUserPossessesTheEmail(String email) { userEmail = email; }

    @And("the user possesses the password {string}")
    public void theUserPossessesThePassword(String password) { userPassword = password; }

    @When("the user logs in")
    public void theUserLogsInWithTheEmail() throws JsonProcessingException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("password", userPassword);
        requestBody.put("email", userEmail);

        userID = null;
        try {
             MvcResult result = mockMvc.perform(post("/login")
                    .content(requestBody.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

             JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
             JSONObject jsonObject = (JSONObject) parser.parse(result.getResponse().getContentAsString());
             // Refuses to cast from integer to long. So used the below work around
             Integer userId = (Integer) jsonObject.getAsNumber("userId");
             userID = userId.longValue();
        } catch (Exception ignored) {}
    }

    @When("the user logs in badly")
    public void theUserLogsInBadly() throws JsonProcessingException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("password", userPassword);
        requestBody.put("email", userEmail);

        userID = null;
        try {
            MvcResult result = mockMvc.perform(post("/login")
                    .content(requestBody.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONObject jsonObject = (JSONObject) parser.parse(result.getResponse().getContentAsString());
            // Refuses to cast from integer to long. So used the below work around
            Integer userId = (Integer) jsonObject.getAsNumber("userId");
            userID = userId.longValue();
        } catch (Exception ignored) {}
    }

    @Then("the user is logged in")
    public void theUserWithTheEmailIsLoggedIn() {
        User user = userRepository.findByEmail(userEmail);
        Assert.assertEquals(user.getUserID(), userID);
    }

    @Then("the user is not logged in")
    public void theUserWithTheEmailIsNotLoggedIn() {
        Assert.assertNull(userID);
    }

    @Then("a user with the email {string} does not exist")
    public void aUserWithTheEmailDoesNotExist(String email) {
        Assert.assertNull(userRepository.findByEmail(email));
    }

    @Then("only the first account with {string} remains with bio {string}")
    public void onlyTheFirstAccountWithRemains(String email, String bio) {
        User user = userRepository.findByEmail(email);
        Assert.assertEquals(user.getBio(), bio);
    }

    @And("no optional values are set")
    public void noOptionalValuesAreSet() {
        userMiddleName = null;
        userNickname = null;
        userBio = null;
        userPhNum = null;
    }

    @Then("the user {string} was created now")
    public void theUserWasCreatedNow(String email) {
        theUser = userRepository.findByEmail(email);
        Instant created = theUser.getCreated();
        assert(ChronoUnit.SECONDS.between(Instant.now(), created) < 20);
    }
}
