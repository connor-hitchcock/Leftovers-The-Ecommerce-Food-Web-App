package org.seng302.entities;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.seng302.exceptions.EmailInUseException;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.PasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserTests {
    private User testUser;
    private User.Builder testBuilder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;

    @BeforeEach
    void setup() throws ParseException {
        // Example user object for tests
        testUser = new User.Builder()
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
        testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Helper function for tests involving the businesses administered attribute. Saves the user to the
     * user repository and creates two businesses, making the user a primary admin of one and a secondary
     * admin of the other.
     */
    void addBusinessesAdministeredToTestUser() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(testUser);
        User testUser2 = testBuilder.build();
        userRepository.save(testUser2);
        Business testBusiness1 = new Business.Builder().withName("Corellis").withBusinessType("Accommodation and Food Services")
        .withAddress(Location.covertAddressStringToLocation("46,Victoria Road,Ashburton,Auckland,Auckland,New Zealand,0624"))
        .withPrimaryOwner(testUser).withDescription("Great coffee").build();
        businessRepository.save(testBusiness1);
        Business testBusiness2 = new Business.Builder().withName("Cakes n Ladders").withBusinessType("Accommodation and Food Services")
        .withAddress(Location.covertAddressStringToLocation("173,Symonds Street,Ashburton,Auckland,Auckland,New Zealand,1010"))
        .withPrimaryOwner(testUser2).withDescription("Chill spot").build();
        businessRepository.save(testBusiness2);
        testBusiness2.addAdmin(testUser);
        businessRepository.save(testBusiness2);
        testUser = userRepository.findByEmail(testUser.getEmail());
    }

    @Test
    void emailTest() {
        testUser.setEmail("Hi_123@testing.com");
        assertEquals("Hi_123@testing.com", testUser.getEmail());

        List<String> badEmails = new ArrayList<String>();
        String badEmail1 = "$%#@qwer";
        String badEmail2 = " ";
        String badEmail3 = ";DROPTABLE users";
        String badEmail4 = "No Spaces@cheekybugger.co.au";
        badEmails.add(badEmail1);
        badEmails.add(badEmail2);
        badEmails.add(null);
        badEmails.add(badEmail3);
        badEmails.add(badEmail4);
        for (String email : badEmails) {
            try {
                testUser.setEmail(email);
            } catch (ResponseStatusException | NullPointerException e) {

            }
            assertEquals("Hi_123@testing.com", testUser.getEmail());
            //None of these should work, so email will be unchanged from last success case
        }
    }

    @Test
    void phoneTest() {
        List<String> goodPhones = new ArrayList<>();
        String goodPhone1 = "123-456-7890";
        String goodPhone2 = "(123) 456-7890";
        String goodPhone3 = "123 456 7890";
        String goodPhone4 = "123.456.7890";
        String goodPhone5 = "+91 (123) 456-7890";
        String goodPhone6 = "+64 3 555 0129";
        goodPhones.add(goodPhone1);
        goodPhones.add(goodPhone2);
        goodPhones.add(goodPhone3);
        goodPhones.add(goodPhone4);
        goodPhones.add(goodPhone5);
        goodPhones.add(goodPhone6);

        for (String phone : goodPhones) {
            testUser.setPhNum(phone);
            assertEquals(testUser.getPhNum(), phone);
        }

        List<String> badPhones = new ArrayList<>();
        String badPhone1 = "asdf";
        String badPhone2 = "67--123456";
        String badPhone3 = " ";
        badPhones.add(badPhone1);
        badPhones.add(badPhone2);
        badPhones.add(badPhone3);

        for (String phone : badPhones) {
            try {
                testUser.setPhNum(phone);
            } catch (ResponseStatusException ignored) {}
            assertEquals(goodPhone6, testUser.getPhNum()); // Bad wont change, so last good phone is current
        }
    }

    /**
     * Checks that when setPhNum is called with the empty string, the user's phone number is set to null when the user's
     * phone number is null or a non-empty string before setPhNum is called
     */
    @Test
    void checkPhNumEmptyStringTest() {
        testUser.setPhNum("6494456719");
        testUser.setPhNum("");
        assertNull(testUser.getPhNum());
        testUser.setPhNum(null);
        testUser.setPhNum("");
        assertNull(testUser.getPhNum());
    }

    /**
     * Checks several real life first names will be set as the user's first name
     */
    @Test
    void checkValidFirstNames() {
        String[] validFirstNames = { "Connor", "Ella", "Johnny", "Richard", "Ned", "Jefferson", "Jackson", "Oliver" };
        for (String firstName : validFirstNames) {
            testUser.setFirstName(firstName);
            assertEquals(testUser.getFirstName(), firstName);
        }
    }

    /**
     * Checks an empty first name will not be set as the user's first name
     */
    @Test
    void checkInvalidFirstNameEmpty() {
        try {
            testUser.setFirstName("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException ignored) { }
    }

    /**
     * Checks several first names that are too long (above 16 letters long) will not be set as the user's first name
     */
    @Test
    void checkInvalidFirstNameTooLong() {
        String[] invalidFirstNames = { "HippoTooLongPotamusHippoTooLongPotamus", "ConnnnnnnnnnnnnnnnnnnnnorConnnnnnnnnnnnnnnnnnnnnor", "MrsMagicalMagicalMrsMagicalMagical" };
        for (String firstName : invalidFirstNames) {
            try {
                testUser.setFirstName(firstName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException ignored) { }
        }
    }

    /**
     * Checks several first names with numbers in them will not be set as the user's first name
     */
    @Test
    void checkInvalidFirstNameNumbers() {
        String[] invalidFirstNames = { "C0nn0r", "E11a", "123456789", "1", "0", "Mohammad1" };
        for (String firstName : invalidFirstNames) {
            try {
                testUser.setFirstName(firstName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException ignored) { }
        }
    }

    /**
     * Checks several first names with characters in them will not be set as the user's first name
     */
    @Test
    void checkInvalidFirstNameCharacters() {
        String[] invalidFirstNames = { "C#nn#r", "E!!@", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String firstName : invalidFirstNames) {
            try {
                testUser.setFirstName(firstName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException ignored) { }
        }
    }

    /**
     * Checks several real life middle names will be set as the user's middle name
     */
    @Test
    void checkValidMiddleNames() {
        String[] validMiddleNames = { "Connor", "Ella", "Johnny", "Richard", "Ned", "Jefferson", "Jackson", "Oliver" };
        for (String middleName : validMiddleNames) {
            testUser.setMiddleName(middleName);
            assertEquals(testUser.getMiddleName(), middleName);
        }
    }

    /**
     * Checks that when the given middle name is empty, the user's middle name is set to null, whether the user's middle
     * name is null or a non-null string before setMiddleName is called.
     */
    @Test
    void checkMiddleNameEmpty() {
        testUser.setMiddleName("Bert");
        testUser.setMiddleName("");
        assertNull(testUser.getMiddleName());
        testUser.setMiddleName(null);
        testUser.setMiddleName("");
        assertNull(testUser.getMiddleName());
    }

    /**
     * Checks several middle names that are too long (above 16 letters long) will not be set as the user's middle name
     */
    @Test
    void checkInvalidMiddleNameTooLong() {
        String[] invalidMiddleNames = { "HippoTooLongPotamusqwertyuiopasdfg", "Connnnnnnnnnnnnnnnnnnnnorqwertyui", "MrsMagicalMagicalqwertyuiopqwerty" };
        for (String middleName : invalidMiddleNames) {
            try {
                testUser.setMiddleName(middleName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException ignored) { }
        }
    }

    /**
     * Checks several middle names with numbers in them will not be set as the user's middle name
     */
    @Test
    void checkInvalidMiddleNameNumbers() {
        String[] invalidMiddleNames = { "C0nn0r", "E11a", "123456789", "1", "0", "Mohammad1" };
        for (String middleName : invalidMiddleNames) {
            try {
                testUser.setMiddleName(middleName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several middle names with characters in them will not be set as the user's middle name
     */
    @Test
    void checkInvalidMiddleNameCharacters() {
        String[] invalidMiddleNames = { "C#nn#r", "E!!@", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String middleName : invalidMiddleNames) {
            try {
                testUser.setMiddleName(middleName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several real life last names will be set as the user's last name
     */
    @Test
    void checkValidLastNames() {
        String[] validLastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis" };
        for (String lastName : validLastNames) {
            testUser.setLastName(lastName);
            assertEquals(testUser.getLastName(), lastName);
        }
    }

    /**
     * Checks an empty last name will not be set as the user's last name
     */
    @Test
    void checkInvalidLastNameEmpty() {
        try {
            testUser.setLastName("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
    }

    /**
     * Checks several last names that are too long (above 16 letters long) will not be set as the user's last name
     */
    @Test
    void checkInvalidLastNameTooLong() {
        String[] invalidLastNames = { "HippoTooLongPotamusHippoTooLongPotamus", "HippoTooLongPotamusConnnnnnnnnnnnnnnnnnnnnor", "HippoTooLongPotamusMrsMagicalMagical" };
        for (String lastName : invalidLastNames) {
            try {
                testUser.setLastName(lastName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several last names with numbers in them will not be set as the user's last name
     */
    @Test
    void checkInvalidLastNameNumbers() {
        String[] invalidLastNames = { "Sm1th", "J0hns0n", "123456789", "1", "0", "Mohammad1" };
        for (String lastName : invalidLastNames) {
            try {
                testUser.setLastName(lastName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several last names with characters in them will not be set as the user's last name
     */
    @Test
    void checkInvalidLastNameCharacters() {
        String[] invalidLastNames = { "Sm!th", "J#hn$#n", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String lastName : invalidLastNames) {
            try {
                testUser.setLastName(lastName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several nicknames will be set as the user's nickname
     */
    @Test
    void checkValidNicknames() {
        String[] validNicknames = { "Peach", "Rose", "Built Different", "God", "That Guy", "Officer", "Pebble" };
        for (String nickname : validNicknames) {
            testUser.setNickname(nickname);
            assertEquals(testUser.getNickname(), nickname);
        }
    }

    /**
     * Checks that when setNickname is called with the empty string, the user's nickname is set to null, when the user's
     * nickname is null or a non-null string before setNickname is called
     */
    @Test
    void checkNicknameEmptyStringTest() {
        testUser.setNickname("Fred");
        testUser.setNickname("");
        assertNull(testUser.getNickname());
        testUser.setNickname(null);
        testUser.setNickname("");
        assertNull(testUser.getNickname());
    }

    /**
     * Checks several nicknames that are too long (above 16 letters long) will not be set as the user's nickname
     */
    @Test
    void checkInvalidNicknameTooLong() {
        String[] invalidNicknames = { "HippoTooLongPotamusHippoTooLongPotamus", "HippoTooLongPotamusConnnnnnnnnnnnnnnnnnnnnor", "HippoTooLongPotamusMrsMagicalMagical" };
        for (String nickname : invalidNicknames) {
            try {
                testUser.setNickname(nickname);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several nicknames with numbers in them will not be set as the user's nickname
     */
    @Test
    void checkInvalidNicknameNumbers() {
        String[] invalidNicknames = { "Sm1th", "J0hns0n", "123456789", "1", "0", "Mohammad1" };
        for (String nickname : invalidNicknames) {
            try {
                testUser.setNickname(nickname);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several nicknames with characters in them will not be set as the user's nickname
     */
    @Test
    void checkInvalidNicknameCharacters() {
        String[] invalidNicknames = { "Sm!th", "J#hn$#n", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String nickname : invalidNicknames) {
            try {
                testUser.setNickname(nickname);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several bios will be set as the user's bio
     */
    @Test
    void checkValidBio() {
        String[] validBios = { "I am a happy person when I am not studying",
                "I am a University student meaning I have no free time",
                "Do you like cats cause I like cats" };
        for (String bio : validBios) {
            testUser.setBio(bio);
            assertEquals(testUser.getBio(), bio);
        }
    }

    /**
     * Checks that when setBio is called with the empty string, the user's bio is set to null, when the user's bio is
     * null or a non-null string before setBio is called.
     */
    @Test
    void checkBioEmptyStringTest() {
        testUser.setBio("My name is Inigo Montoya You killed my father Prepare to die");
        testUser.setBio("");
        assertNull(testUser.getBio());
        testUser.setBio(null);
        testUser.setBio("");
        assertNull(testUser.getBio());
    }

    /**
     * Checks several bios that are too long (above 255 letters long) will not be set as the user's bio
     */
    @Test
    void checkInvalidBioTooLong() {
        String[] invalidBios = { "This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely",
        "This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely"};
        for (String bio : invalidBios) {
            try {
                testUser.setBio(bio);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several bios with numbers in them will be set as the user's bio
     */
    @Test
    void checkValidBioNumbers() {
        String[] invalidBios = { "I am a happy p3rs0n when 1 am n0t study1ng",
                "1 am a Un1vers1ty stud3nt meaning I have n0 fr33 t1m3",
                "D0 y0u l1k3 cats caus3 1 l1k3 cats", "0", "1", "0123456789" };
        for (String bio : invalidBios) {
            testUser.setBio(bio);
            assertEquals(bio, testUser.getBio());
        }
    }

        /**
     * Checks several bios with characters in , only set when it satisfy the condition
     */
    @Test
    void checkValidBioCharacters() {
        String[] validBios = { "! @m @ h@ppy per$on when ! @m not study!ng",
                "! @m @ Un!ver$ity $tudent meaning I have no &ree ime",
                "Do you li%e ca(s cause ) lke cats", "!", "@", "!@#$%&()" };
        for (String bio : validBios) {
            testUser.setBio(bio);
            assertEquals(bio, testUser.getBio());
        }
    }
    /**
     * Checks several bios with non ASCII chracter in them will not be set as the user's bio
     */
    @Test
    void checkInvalidBioCharacters() {
        String[] invalidBios = {"dummy भारतभारत", "bladummy网络网络", "hahadummy.קום.קום" };
        for (String bio : invalidBios) {
            try {
                testUser.setBio(bio);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }
    /**
     * Check user age > 13 when register
     * @throws ParseException
     */
    @Test
    void checkDateofBirthGreaterThanThirteen() throws ParseException {
        String dateOfBirthString = "2000-05-11";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate dateOfBirth = LocalDate.parse(dateOfBirthString, dateTimeFormatter);
            
        testUser.setDob(dateOfBirth);
        assertEquals(dateOfBirth, testUser.getDob());
    }

        /**
     * Check user age < 13 when register
     * @throws ParseException
     */
    @Test
    void checkDateofBirthLesserThanThirteen() throws ParseException {
        String dateOfBirthString = "2010-05-11";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate dateOfBirth = LocalDate.parse(dateOfBirthString, dateTimeFormatter);

        LocalDate date = LocalDate.now();
        LocalDate minDate = date.minusYears(13);
        
        assertTrue(dateOfBirth.compareTo(minDate) > 0);
        try {
            testUser.setDob(dateOfBirth);
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
        
    }

    /**
     * Checks several address strings will be set as the user's address
     */
    @Test
    void checkValidAddress() {
        String[] validAddresses = { "20,Elizabeth Street,Ashburton,Christchurch,New Zealand,Canterbury,8041",
                "10,Made Up Street,Ashburton,Los Angeles,United States of Not Real,Fakeland,99999",
                "49,You Would Not,Ashburton,Believe,Your,Eyes,11111",
                "20,Elizabeth Street,,Christchurch,New Zealand,Canterbury,8041"};
        for (String address : validAddresses) {
            testUser.setAddress(Location.covertAddressStringToLocation(address));
            Location location = Location.covertAddressStringToLocation(address);
            assertTrue(new ReflectionEquals(location).matches(testUser.getAddress()));
        }
    }

    /**
     * Checks several passwords will be set as the user's password
     */
    @Test @Ignore
    void checkValidPassword() throws NoSuchAlgorithmException {
        String[] validPasswords = { "SAASDJ3KAasdasdsa$*#", "asdjaskdj383", "77asjdksajk&&&",
                "ASJDKLASJLKDJASKLDJK234567890123", "SKLDJASKD*(*(@*(#@*(8238283999" };
        for (String password : validPasswords) {
            testUser.setAuthenticationCodeFromPassword(password);
            assertEquals(PasswordAuthenticator.generateAuthenticationCode(password), testUser.getAuthenticationCode());
        }
    }

    /**
     * Checks an empty password will not be set as the user's password
     */
    @Test
    void checkInvalidPasswordEmpty() {
        try {
            testUser.setAuthenticationCodeFromPassword("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
    }

    /**
     * Checks several passwords that are too short (below 8 characters long) will not be set as the user's password
     */
    @Test
    void checkInvalidPasswordTooShort() {
        String[] invalidPasswords = { "1", "12", "123", "1234", "12345", "123456", "1234567", "YaBoi#d", "@", "HEy" };
        for (String password : invalidPasswords) {
            try {
                testUser.setAuthenticationCodeFromPassword(password);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several passwords that are too long (above 64 characters long) will not be set as the user's password
     */
    @Test
    void checkInvalidPasswordTooLong() {
        String[] invalidPasswords = { "this password will be exactly 65 characters long seeeeeeeeeeeeeee", "" +
                "asjdklasjkldjaslkdjlasjdklasjlkd^*&$#(*&#*(W$&(*#&#*(&(*3798427329847293874982378932480923490",
                "asdjkl;asdasjkdsjakljaslkdjsa k        37498823094*()(#*()*($)#)*()#$*(#)$*()$#*DKLDJSL"};
        for (String password : invalidPasswords) {
            try {
                testUser.setAuthenticationCodeFromPassword(password);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Verify that checkEmailUniqueness does not throw an exception when called with an email that has not been added
     * to the user repository.
     */
    @Test
    void checkEmailUniquenessUniqueTest() {
        String testEmail = "johnsmith99@gmail.com";
        businessRepository.deleteAll();
        userRepository.deleteAll();
        try {
            User.checkEmailUniqueness(testEmail, userRepository);
        } catch (Exception e) {
            fail("Exception should not be thrown when checkEmailUniqueness is called with an email which " +
                    "has not been added to the user repository");
        }
    }

    /**
     * Verify that constructPublicJSON returns a JSON with the attributes present.
     */
    @Test
    void constructPublicJsonPublicAttributesPresentTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPublicJson(true);
        assertTrue(json.containsKey("id"));
        assertTrue(json.containsKey("firstName"));
        assertTrue(json.containsKey("middleName"));
        assertTrue(json.containsKey("lastName"));
        assertTrue(json.containsKey("nickname"));
        assertTrue(json.containsKey("email"));
        assertTrue(json.containsKey("bio"));
        assertTrue(json.containsKey("created"));
        assertTrue(json.containsKey("businessesAdministered"));
    }

    /**
     * Verify that constructPublicJson returns a JSON with none of the hidden attributes present.
     */
    @Test
    void constructPublicJsonHiddenAttributesNotPresentTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPublicJson(true);
        json.remove("id");
        json.remove("firstName");
        json.remove("middleName");
        json.remove("lastName");
        json.remove("nickname");
        json.remove("email");
        json.remove("bio");
        json.remove("created");
        json.remove("dateOfBirth");
        json.remove("homeAddress");
        json.remove("businessesAdministered");
        assertTrue(json.isEmpty());
    }

    /**
     * Verify that when constructPublicJson is called on a User which has none of its attributes set to null,
     * and the list of businesses it administers is empty, all of the attributes in the JSON have the expected value.
     */
    @Test
    void constructPublicJsonNoAttributesNullTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPublicJson(true);
        assertEquals(testUser.getUserID().toString(), json.getAsString("id"));
        assertEquals(testUser.getFirstName(), json.getAsString("firstName"));
        assertEquals(testUser.getMiddleName(), json.getAsString("middleName"));
        assertEquals(testUser.getLastName(), json.getAsString("lastName"));
        assertEquals(testUser.getNickname(), json.getAsString("nickname"));
        assertEquals(testUser.getBio(), json.getAsString("bio"));
        assertEquals(testUser.getEmail(), json.getAsString("email"));
        assertEquals(testUser.getCreated().toString(), json.getAsString("created"));
        String expectedAddressString = testUser.getAddress().constructPartialJson().toJSONString();
        assertEquals(expectedAddressString, json.getAsString("homeAddress"));
        assertEquals("[]", json.getAsString("businessesAdministered"));
    }

    /**
     * Verify that when constructPublicJson is called on a User which has its optional attributes set to null,
     * and the list of businesses it administers is empty, all of the attributes in the JSON have the expected value.
     */
    @Test
    void constructPublicJsonOptionalAttributesNullTest() {
        testUser.setUserID(1L);
        testUser.setMiddleName(null);
        testUser.setNickname(null);
        testUser.setBio(null);
        JSONObject json = testUser.constructPublicJson(true);
        assertEquals(testUser.getUserID().toString(), json.getAsString("id"));
        assertEquals(testUser.getFirstName(), json.getAsString("firstName"));
        assertEquals(testUser.getMiddleName(), json.getAsString("middleName"));
        assertEquals(testUser.getLastName(), json.getAsString("lastName"));
        assertEquals(testUser.getNickname(), json.getAsString("nickname"));
        assertEquals(testUser.getBio(), json.getAsString("bio"));
        assertEquals(testUser.getEmail(), json.getAsString("email"));
        assertEquals(testUser.getCreated().toString(), json.getAsString("created"));
        String expectedAddressString = testUser.getAddress().constructPartialJson().toJSONString();
        assertEquals(expectedAddressString, json.getAsString("homeAddress"));
        assertEquals("[]", json.getAsString("businessesAdministered"));
    }

    /**
     * Verify that constructPrivateJSON returns a JSON with all expected attributes present.
     */
    @Test
    void constructPrivateJsonAllExpectedAttributesPresentTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPrivateJson(true);
        assertTrue(json.containsKey("id"));
        assertTrue(json.containsKey("firstName"));
        assertTrue(json.containsKey("middleName"));
        assertTrue(json.containsKey("lastName"));
        assertTrue(json.containsKey("nickname"));
        assertTrue(json.containsKey("email"));
        assertTrue(json.containsKey("bio"));
        assertTrue(json.containsKey("created"));
        assertTrue(json.containsKey("businessesAdministered"));
        assertTrue(json.containsKey("phoneNumber"));
        assertTrue(json.containsKey("dateOfBirth"));
        assertTrue(json.containsKey("role"));
    }

    /**
     * Verify that constructPrivateJson returns a JSON with no unexpected attributes present.
     */
    @Test
    void constructPrivateJsonNoUnexpectedAttributesPresentTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPrivateJson(true);
        json.remove("id");
        json.remove("firstName");
        json.remove("middleName");
        json.remove("lastName");
        json.remove("nickname");
        json.remove("email");
        json.remove("bio");
        json.remove("created");
        json.remove("homeAddress");
        json.remove("businessesAdministered");
        json.remove("phoneNumber");
        json.remove("dateOfBirth");
        json.remove("role");
        assertTrue(json.isEmpty());
    }

    /**
     * Verify that when constructPrivateJson is called on a User which has none of its attributes set to null,
     * and the list of businesses it administers is empty, all of the attributes in the JSON have the expected value.
     */
    @Test
    void constructPrivateJsonNoAttributesNullTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPrivateJson(true);
        assertEquals(testUser.getUserID().toString(), json.getAsString("id"));
        assertEquals(testUser.getFirstName(), json.getAsString("firstName"));
        assertEquals(testUser.getMiddleName(), json.getAsString("middleName"));
        assertEquals(testUser.getLastName(), json.getAsString("lastName"));
        assertEquals(testUser.getNickname(), json.getAsString("nickname"));
        assertEquals(testUser.getBio(), json.getAsString("bio"));
        assertEquals(testUser.getEmail(), json.getAsString("email"));
        assertEquals(testUser.getCreated().toString(), json.getAsString("created"));
        String expectedAddressString = testUser.getAddress().constructFullJson().toJSONString();
        assertEquals(expectedAddressString, json.getAsString("homeAddress"));
        assertEquals("[]", json.getAsString("businessesAdministered"));
        assertEquals(testUser.getRole(), json.getAsString("role"));
        assertEquals(testUser.getPhNum(), json.getAsString("phoneNumber"));
        assertEquals(testUser.getDob().toString(), json.getAsString("dateOfBirth"));
    }

    /**
     * Verify that when constructPrivateJson is called on a User which has its optional attributes set to null,
     * and the list of businesses it administers is empty, all of the attributes in the JSON have the expected value.
     */
    @Test
    void constructPrivateJsonOptionalAttributesNullTest() {
        testUser.setUserID(1L);
        testUser.setMiddleName(null);
        testUser.setNickname(null);
        testUser.setBio(null);
        testUser.setPhNum(null);
        JSONObject json = testUser.constructPrivateJson(true);
        assertEquals(testUser.getUserID().toString(), json.getAsString("id"));
        assertEquals(testUser.getFirstName(), json.getAsString("firstName"));
        assertEquals(testUser.getMiddleName(), json.getAsString("middleName"));
        assertEquals(testUser.getLastName(), json.getAsString("lastName"));
        assertEquals(testUser.getNickname(), json.getAsString("nickname"));
        assertEquals(testUser.getBio(), json.getAsString("bio"));
        assertEquals(testUser.getEmail(), json.getAsString("email"));
        assertEquals(testUser.getCreated().toString(), json.getAsString("created"));
        String expectedAddressString = testUser.getAddress().constructFullJson().toJSONString();
        assertEquals(expectedAddressString, json.getAsString("homeAddress"));
        assertEquals("[]", json.getAsString("businessesAdministered"));
        assertEquals(testUser.getRole(), json.getAsString("role"));
        assertEquals(testUser.getPhNum(), json.getAsString("phoneNumber"));
        assertEquals(testUser.getDob().toString(), json.getAsString("dateOfBirth"));
    }

    /**
     * Test that when constructPublicJson is called with 'true' as its arguement, and the list of
     * businesses administered by the user is not empty, the 'businessesAdministered' field of the
     * resulting json will have the correct details for every business administered by the user
     */
    @Test
    void constructPublicJsonBusinessesAdministeredTrueTest() {
        addBusinessesAdministeredToTestUser();
        List<Business> testBusinesses = new ArrayList<>();
        testBusinesses.addAll(testUser.getBusinessesAdministeredAndOwned());
        Collections.sort(testBusinesses, (Business b1, Business b2) ->
                b1.getId().compareTo(b2.getId()));
        assertEquals(2, testBusinesses.size());
        JSONObject json = testUser.constructPublicJson(true);
        JSONArray expectedBusinessArray = new JSONArray();
        for (Business business : testBusinesses) {
            expectedBusinessArray.add(business.constructJson(false));
        }
        String expectedBusinessString = expectedBusinessArray.toJSONString();
        assertEquals(expectedBusinessString, json.getAsString("businessesAdministered"));
    }

    /**
     * Test that when constructPublicJson is called with 'false' as its arguement, and the list of
     * businesses administered by the user is not empty, the resulting json wil not contain the businessesAdministered
     * field.
     */
    @Test
    void constructPublicJsonBusinessesAdministeredFalseTest() {
        addBusinessesAdministeredToTestUser();
        JSONObject json = testUser.constructPublicJson(false);
        assertNull(json.get("businessesAdministered"));
    }

    /**
     * Test that when constructPublicJson is called with no arguement, and the list of
     * businesses administered by the user is not empty, the 'businessesAdministered' field will not be present
     * in the resulting JSON.
     */
    @Test
    void constructPublicJsonBusinessesAdministeredNullTest() {
        addBusinessesAdministeredToTestUser();
        JSONObject json = testUser.constructPublicJson();
        assertNull(json.get("businessesAdministered"));
    }

    /**
     * Test that when constructPrviateJson is called with 'true' as its arguement, and the list of
     * businesses administered by the user is not empty, the 'businessesAdministered' field of the
     * resulting json will have the correct details for every business administered by the user
     */
    @Test
    void constructPrivateJsonBusinessesAdministeredTrueTest() {
        addBusinessesAdministeredToTestUser();
        List<Business> testBusinesses = new ArrayList<>();
        testBusinesses.addAll(testUser.getBusinessesAdministeredAndOwned());
        Collections.sort(testBusinesses, (Business b1, Business b2) -> b1.getId().compareTo(b2.getId()));
        assertEquals(2, testBusinesses.size());
        JSONObject json = testUser.constructPrivateJson(true);
        JSONArray expectedBusinessArray = new JSONArray();
        for (Business business : testBusinesses) {
            expectedBusinessArray.add(business.constructJson(false));
        }
        String expectedBusinessString = expectedBusinessArray.toJSONString();
        assertEquals(expectedBusinessString, json.getAsString("businessesAdministered"));
    }

    /**
     * Test that when constructPrivateJson is called with 'false' as its arguement, and the list of
     * businesses administered by the user is not empty, the resulting json will not include the
     * businessesAdministered field.
     */
    @Test
    void constructPrivateJsonBusinessesAdministeredFalseTest() {
        addBusinessesAdministeredToTestUser();
        JSONObject json = testUser.constructPrivateJson(false);
        assertNull(json.get("businessesAdministered"));
    }

    /**
     * Test that when constructPrivateJson is called with no arguement, and the list of
     * businesses administered by the user is not empty, the resulting json will not contain the businessesAdministered
     * field.
     */
    @Test
    void constructPrivateJsonBusinessesAdministeredNullTest() {
        addBusinessesAdministeredToTestUser();
        JSONObject json = testUser.constructPrivateJson();
        assertNull(json.get("businessesAdministered"));
    }

    /**
     * Verify that checkEmailUniqueness throws an EmailInUseException when called with an email that has been added to
     * the user repository.
     */
    @Test
    void checkEmailUniquenessNonUniqueTest() throws ParseException {
        String testEmail = "johnsmith99@gmail.com";
        User testUser = new User.Builder()
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
        assertThrows(EmailInUseException.class, () -> {
            User.checkEmailUniqueness(testEmail, userRepository);
        });
    }

    /**
     * Verify that when user is instantiated, its created attribute is set to the current system time.
     */
    @Test
    void setCreatedTest() {
        Date now = new Date(System.currentTimeMillis());
        User testUser = testBuilder.build();
        assertTrue(ChronoUnit.SECONDS.between(Instant.now(), testUser.getCreated()) < 20);
    }

    /**
     * Verify that when User.Builder.build() is used without setting first name a ResponseStatusException is thrown
     */
    @Test
    void buildWithoutFirstNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting last name a ResponseStatusException is thrown
     */
    @Test
    void buildWithoutLastNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting email a ResponseStatusException is thrown
     */
    @Test
    void buildWithoutEmailTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting password a ResponseStatusException is thrown
     */
    @Test
    void buildWithoutPasswordTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting home address a ResponseStatusException is thrown
     */
    @Test
    void buildWithoutAddressTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 555 0129");
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting date of birth a ResponseStatusException is thrown
     */
    @Test
    void buildWithoutDateOfBirthTest() {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build is used without setting a middle name, a user is constructed with their middle
     * name set to null
     */
    @Test
    void buildWithoutMiddleNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getMiddleName());
    }

    /**
     * Verify that when User.Builder.build is used without setting a nickname, a user is constructed with their nickname
     * set to null
     */
    @Test
    void buildWithoutNickNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getNickname());
    }

    /**
     * Verify that when User.Builder.build is used without setting a bio, a user is constructed with their bio set to null
     */
    @Test
    void buildWithoutBioTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getBio());
    }

    /**
     * Verify that when User.Builder.build is used without setting a phone number, a user is constructed with their phone
     * number set to null
     */
    @Test
    void buildWithoutPhoneNumberTest() throws ParseException{
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getPhNum());
    }

    /**
     * Test that when User.Builder.build is called with the builder's first name set, the first name of the user will
     * be the same as the one set in the builder.
     */
    @Test
    void buildWithFirstNameTest() {
        User user = testBuilder.build();
        assertEquals("Joe", user.getFirstName());
    }

    /**
     * Test that when User.Builder.build is called with the builder's middle name set, the middle name of the user will
     * be the same as the one set in the builder.
     */
    @Test
    void buildWithMiddleNameTest() {
        User user = testBuilder.build();
        assertEquals("Hector", user.getMiddleName());
    }

    /**
     * Test that when User.Builder.build is called with the builder's last name set, the last name of the user will
     * be the same as the one set in the builder.
     */
    @Test
    void buildWithLastNameTest() {
        User user = testBuilder.build();
        assertEquals("Smith", user.getLastName());
    }

    /**
     * Test that when User.Builder.build is called with the builder's nickname set, the nickname of the user will
     * be the same as the one set in the builder.
     */
    @Test
    void buildWithNickNameTest() {
        User user = testBuilder.build();
        assertEquals("Jonny", user.getNickname());
    }

    /**
     * Test that when User.Builder.build is called with the builder's email set, the email of the user will
     * be the same as the one set in the builder.
     */
    @Test
    void buildWithEmailTest() {
        User user = testBuilder.build();
        assertEquals("johnsmith99@gmail.com", user.getEmail());
    }

    /**
     * Test that when User.Builder.build is called with the builder's password set, the authentication code of the user
     * will be the same as the one generated from the password set in the builder.
     */
    @Test
    void buildWithPasswordTest() throws NoSuchAlgorithmException {
        User user = testBuilder.build();
        assertEquals(PasswordAuthenticator.generateAuthenticationCode("1337-H%nt3r2"), user.getAuthenticationCode());
    }

    /**
     * Test that when User.Builder.build is called with the builder's bio set, the bio of the user will be the same as
     * the one set in the builder.
     */
    @Test
    void buildWithBioTest() {
        User user = testBuilder.build();
        assertEquals("Likes long walks on the beach", user.getBio());
    }

    /**
     * Test that when User.Builder.build is called with the builder's home address set, the home address of the user will
     * be the same as the one set in the builder.
     */
    @Test
    void buildWithAddressTest() {
        User user = testBuilder.build();
        //ReflectionEquals.match() matches the contents of the object instead of the object itself.
        assertTrue(new ReflectionEquals(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand,Canterbury,8041")).matches(user.getAddress()));
    }

    /**
     * Test that when User.Builder.build is called with the builder's phone number set, the phone number of the user will
     * be the same as the one set in the builder.
     */
    @Test
    void buildWithPhoneNumberTest() {
        User user = testBuilder.build();
        assertEquals("+64 3 555 0129", user.getPhNum());
    }

    /**
     * Test that when User.Builder.build is called with the builder's date of birth set, the date of birth of the user will
     * be equivalent to the one set in the builder.
     */
    @Test
    void buildWithDobTest() throws ParseException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate dob = LocalDate.parse("2001-03-11", dateTimeFormatter);
        User user = testBuilder.build();
        assertEquals(dob, user.getDob());
    }

}
