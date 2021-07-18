package org.seng302.entities;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ImageRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class BusinessTests {

    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ImageRepository imageRepository;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpSession session;

    User testUser1;
    User testUser2;
    User testUser3;
    Business testBusiness1;

    /**
     * Sets up 3 Users and 1 Business
     * User1 is primary owner of the business
     * @throws ParseException
     */
    @BeforeEach
    void setUp() throws ParseException {
        LocalDateTime ldt = LocalDateTime.now().minusYears(15);
        String ageBelow16 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(ldt);
        businessRepository.deleteAll();
        userRepository.deleteAll();

        testUser1 = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser2 = new User.Builder()
                .withFirstName("Dave")
                .withMiddleName("Joe")
                .withLastName("Bloggs")
                .withNickName("Dave")
                .withEmail("dave@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser3 = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("Davidson")
                .withLastName("Smith")
                .withNickName("Bobby")
                .withEmail("bobbysmith99@gmail.com")
                .withPassword("1440-H%nt3r2")
                .withBio("Likes slow walks on the beach")
                .withDob(ageBelow16)
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
        testBusiness1 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);

        MockitoAnnotations.openMocks(this);

    }
    /**
     * Test that when a User is an admin of a business, the set of business admins contains that user
     * @throws Exception
     */
    @Test
    void getAdministratorsReturnsAdministrators() throws Exception {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        assertTrue(testBusiness1.getAdministrators().stream().anyMatch(user -> user.getUserID() == testUser2.getUserID()));
    }

    /**
     * Test that getting the primary owner correctly returns the primary owner
     * @throws Exception
     */
    @Test
    void getPrimaryOwnerReturnsPrimaryOwner() throws Exception {
        assertEquals(testUser1.getUserID(), testBusiness1.getPrimaryOwner().getUserID());
    }

    /**
     * No errors should be thrown when the type is a valid business type
     * @throws ResponseStatusException
     */
    @Test
    void setBusinessTypeWhenValid() throws ResponseStatusException {
        testBusiness1.setBusinessType("Retail Trade");
        assertEquals("Retail Trade", testBusiness1.getBusinessType());
    }

    /**
     * Test that an error is thrown when the business type is invalid
     * @throws ResponseStatusException
     */
    @Test
    void setBusinessTypeThrowsWhenInvalid() throws ResponseStatusException {
        try {
            testBusiness1.setBusinessType("invalid type");
            fail(); // shouldnt get here
        } catch (ResponseStatusException err) {

        }
    }

    /**
     * Test that setting the business name changes the business name
     */
    @Test
    void setNameSetsName() {
        String newName = "Cool Business";
        testBusiness1.setName(newName);
        assertEquals(newName, testBusiness1.getName());
    }

    /**
     * test that setting the business description sets the business description
     */
    @Test
    void setDescriptionSetsDescription() {
        String newDesc = "Some description";
        testBusiness1.setDescription(newDesc);
        assertEquals(newDesc, testBusiness1.getDescription());
    }

    /**
     * tests that if user is above or equal 16 years old, the user's data will be stored as the primary owner
     */
    @Test
    void setAboveMinimumAge() {
        assertNotNull(testBusiness1.getPrimaryOwner());
    }

    /**
     * tests that if user is below 16 years old, an exception will be thrown
     */
    @Test
    void setBelowMinimumAge() {
        Exception thrown = assertThrows(ResponseStatusException.class, () -> {
            Business testBusiness2 = new Business.Builder()
                    .withBusinessType("Accommodation and Food Services")
                    .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                    .withDescription("Some description")
                    .withName("BusinessName")
                    .withPrimaryOwner(testUser3)
                    .build();
            testBusiness2 = businessRepository.save(testBusiness2);
        }, "Expected Business.builder() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains("User is not of minimum age required to create a business"));
    }

    /**
     * Helper function that returns an array of valid names to use in tests.
     *
     * @return An array of valid business names various lengths and character types
     */
    private String[] getTestNames() {
        StringBuilder longBusinessNameBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longBusinessNameBuilder.append("a");
        }
        return new String[]{"Joe's cake shop", "BNZ", "X", "cool business", "big-business", "$%@&.,:;", "BUSINESS123", "" +
                "another_business", longBusinessNameBuilder.toString()};
    }


    /**
     * Check that when setName is called with a name which is 100 characters or less, contains only letters, numbers and
     * the characters "@ $ % & . , ' ; : - _", and is not empty, the businesses name is set to that value.
     */
    @Test
    void setNameValidNameTest() {
        String[] testNames = getTestNames();
        for (String name : testNames) {
            testBusiness1.setName(name);
            assertEquals(testBusiness1.getName(), name);
        }
    }

    /**
     * Check that when setName is called on a business with a valid name and that business is saved to the repository,
     * the name associated with the saved entity is updated.
     */
    @Test
    void setNameSavedToRepositoryTest() {
        String[] testNames = getTestNames();
        for (String name : testNames) {
            testBusiness1.setName(name);
            businessRepository.save(testBusiness1);
            testBusiness1 = businessRepository.findById(testBusiness1.getId()).get();
            assertEquals(name, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with a name which contains characters which are not letters, numbers or
     * the characters "! " # $ % & ' ( ) * + , - . / : ; < = > ? @ [ \ ] ^ _ ` { | } ~",
     * a response status exception with status code 400 will be thrown and the
     * business's name will not be changed.
     */
    @Test
    void setNameInvalidCharacterTest() {
        String originalName = testBusiness1.getName();
        String[] invalidCharacterNames = {"\n", "»»»»»", "business¢", "½This is not allowed", "¡or this¡"};
        for (String name : invalidCharacterNames) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setName(name);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalName, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with a name which is greater than 100 characters lonog, a response status
     * exception with status code 400 will be thrown and the business's name will not be changed.
     */
    @Test
    void setNameTooLongTest() {
        String originalName = testBusiness1.getName();

        StringBuilder justOver = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            justOver.append("x");
        }
        StringBuilder wayTooLong = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            wayTooLong.append("y");
        }
        String[] longNames = {justOver.toString(), wayTooLong.toString()};

        for (String name : longNames) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setName(name);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalName, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with null as its argument, a response status expection will be thrown with
     * status code 400 and the business's name will not be changed.
     */
    @Test
    void setNameNullTest() {
        String originalName = testBusiness1.getName();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setName(null);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalName, testBusiness1.getName());
    }

    /**
     * Check that when setName is called with the empty string as its argument, a response status expection will be thrown
     * with status code 400 and the business's name will not be changed.
     */
    @Test
    void setNameEmptyStringTest() {
        String originalName = testBusiness1.getName();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setName("");
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalName, testBusiness1.getName());
    }

    /**
     * Check that when setName is called with a blank string as its argument, a response status expection will be thrown
     * with status code 400 and the business's name will not be changed.
     */
    @Test
    void setNameBlankStringTest() {
        String originalName = testBusiness1.getName();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setName("      ");
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalName, testBusiness1.getName());
    }

    /**
     * Helper function that returns an array of valid descriptions to use in tests. Includes all the same strings as
     * returned by getTestNames plus a 200 character string to test that long descriptions are accepted.
     * @return An array of valid business names various lengths and character types
     */
    private String[] getTestDescriptions() {
        String[] testNames = getTestNames();
        String[] testDescriptions = new String[testNames.length + 1];
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longDescription.append("f");
        }
        for (int i = 0; i < testNames.length; i++) {
            testDescriptions[i] = testNames[i];
        }
        testDescriptions[testDescriptions.length - 1] = longDescription.toString();
        return testDescriptions;
    }

    /**
     * Check that when setDescription is called with a string which is 200 characters or less, contains only letters,
     * and the characters "@ $ % & . , ; : - _", and is not empty, the businesses description is set to that value.
     */
    @Test
    void setDescriptionValidDescriptionTest() {
        String[] testDescriptions = getTestDescriptions();
        for (String description : testDescriptions) {
            testBusiness1.setDescription(description);
            assertEquals(description, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called on a business with a valid name and that business is saved to the repository,
     * the description associated with the saved entity is updated.
     */
    @Test
    void setDescriptionSavedToRepositoryTest() {
        String[] testDescriptions = getTestDescriptions();
        for (String description : testDescriptions) {
            testBusiness1.setDescription(description);
            businessRepository.save(testBusiness1);
            testBusiness1 = businessRepository.findById(testBusiness1.getId()).get();
            assertEquals(description, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with a name which contains characters which are not letters, numbers or
     * the characters "@ $ % & . , ; : - _", a response status exception with status code 400 will be thrown and the
     * business's description will not be changed.
     */
    @Test
    void setDescriptionInvalidCharacterTest() {
        String originalDescription = testBusiness1.getDescription();
        String[] invalidCharacterDescriptions = {"ƒ", "»»»»»", "business¢", "½This is not allowed", "¡or this¡"};
        for (String description : invalidCharacterDescriptions) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setDescription(description);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalDescription, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with a string which is greater than 200 characters lonog, a response status
     * exception with status code 400 will be thrown and the business's description will not be changed.
     */
    @Test
    void setDescriptionTooLongTest() {
        String originalDescription = testBusiness1.getDescription();

        StringBuilder justOver = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            justOver.append("x");
        }
        StringBuilder wayTooLong = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            wayTooLong.append("y");
        }
        String[] longDescriptions = {justOver.toString(), wayTooLong.toString()};

        for (String description : longDescriptions) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setDescription(description);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalDescription, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with null as its argument, the description becomes an empty string
     */
    @Test
    void setDescriptionNullTest() {
        testBusiness1.setDescription(null);
        assertEquals("", testBusiness1.getDescription());
    }

    /**
     * Check that if someone attempts to remove a user who is currently the primary owner of a business from the database,
     * an expection is thrown and the user is not removed from the database.
     */
    @Test
    void primaryOwnerCantBeDeletedTest() {
        User primaryOwner = testBusiness1.getPrimaryOwner();
        assertThrows(ResponseStatusException.class, () -> {
            userRepository.deleteById(primaryOwner.getUserID());
        });
        assertNotNull(userRepository.findById(primaryOwner.getUserID()).get());
    }

    /**
     * Check that if addAdmin is called with a user who is already an admin of the business, a response status expection
     * with status code 400 is thrown adn the admin is not added.
     */
    @Test
    void addAdminCurrentAdminTest() {
        testBusiness1.addAdmin(testUser2);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.addAdmin(testUser2);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(1, testBusiness1.getAdministrators().size());
        assertTrue(testBusiness1.getAdministrators().contains(testUser2));
    }

    /**
     * Check that if addAdmin is called with a user who is already the primary owner of the business, a response status
     * expection with status code 400 is thrown and the admin is not added.
     */
    @Test
    void addAdminPrimaryOwnerTest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.addAdmin(testUser1);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(0, testBusiness1.getAdministrators().size());
    }

    /**
     * Check that if addAdmin is called with a user who is not currently an admin of the business, that user is added to
     * the businesses list of administrators.
     */
    @Test
    void addAdminNewAdminTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(1, testBusiness1.getAdministrators().size());
        assertTrue(testBusiness1.getAdministrators().contains(testUser2));
    }

    /**
     * Check that if someone attempts to delete a user who is currently an admin of a business from the database,
     * a DataIntegrityVioloationException will be thrown, and the user and business will not be changed.
     */
    @Test
    void adminDeletedTest() {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        long adminId = testUser2.getUserID();
        long businessId = testBusiness1.getId();
        userRepository.deleteById(testUser2.getUserID());
        assertFalse(userRepository.existsById(adminId));
        assertTrue(businessRepository.existsById(businessId));
        testBusiness1 = businessRepository.findById(businessId).get();
        assertEquals(0, testBusiness1.getAdministrators().size());
    }

    /**
     * Test that when a business which has not admins is deleted, that business is removed from the repository
     * but its primary owner entity is not deleted.
     */
    @Test
    void deleteBusinessWithoutAdminsTest() {
        long businessId = testBusiness1.getId();
        long ownerId = testBusiness1.getPrimaryOwner().getUserID();
        businessRepository.deleteById(businessId);
        assertFalse(businessRepository.existsById(businessId));
        assertTrue(userRepository.existsById(ownerId));
        assertTrue(userRepository.findById(ownerId).get().getBusinessesOwned().isEmpty());
    }

    /**
     * Test that when delete is called on a business which has administrators, a DataIntegrityViolationException
     * is thrown and the business and its admins remain in the database.
     */
    private void deleteBusinessWithAdminsTest() {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        long adminId = testUser2.getUserID();
        long businessId = testBusiness1.getId();
        assertThrows(DataIntegrityViolationException.class, () -> {
            businessRepository.deleteById(businessId);
        });
        assertTrue(userRepository.existsById(adminId));
        assertTrue(businessRepository.existsById(businessId));
        testBusiness1 = businessRepository.findById(businessId).get();
        assertEquals(1, testBusiness1.getAdministrators().size());
        for (User user : testBusiness1.getAdministrators()) {
            assertEquals(adminId, user.getUserID());
        }
    }

    /**
     * Test that when setCreated is called, and the business's created attribute is null,
     * the business's created attribute will be set to the current date and time
     */
    @Test
    void setCreatedInitialValueTest() {
        Business testBusiness2 = new Business.Builder().withBusinessType("Non-profit organisation").withName("Zesty Business")
                .withAddress(Location.covertAddressStringToLocation("101,My Street,Ashburton,Christchurch,Canterbury,New Zealand,1010"))
                .withDescription("A nice place").withPrimaryOwner(testUser2).build();
        assertTrue(ChronoUnit.SECONDS.between(Instant.now(), testBusiness2.getCreated()) < 20);
    }

    /**
     * Test that when setAddress is called with null as the address, a response status exception with status code 400
     * will be thrown and the business's address will not be changed.
     */
    @Test
    void setAddressNullTest() {
        Location originalAddress = testBusiness1.getAddress();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setAddress(null);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalAddress, testBusiness1.getAddress());
    }

    /**
     * Test than when setAddress is called with a Location object as the argument, the business's address will be set to
     * the given location.
     */
    @Test
    void setAddressValidTest() {
        Location address = Location.covertAddressStringToLocation("44,Humbug Ave,Ashburton,Hamilton,Waikato,New Zealand,1000");
        testBusiness1.setAddress(address);
        assertEquals(address, testBusiness1.getAddress());
    }


    /**
     * Helper function for constructJson tests. Creates a list containing JSONObject returned by
     * constructJson method when called on the given business with true, false or no arguement.
     * @return A lsit of JSONObjects produced by calling constructJson with true, false and no arg
     */
    private List<JSONObject> getTestJsons(Business business) {
        List<JSONObject> testJsons = new ArrayList<>();
        testJsons.add(business.constructJson(true));
        testJsons.add(business.constructJson(false));
        testJsons.add(business.constructJson());
        return testJsons;
    }

    /**
     * Test that the JSONObject returned by constructJson contains the fields id, primaryAdministratorId,
     * name, description, businessType, created, administrators and address when constructJson is
     * called with true as its arguement.
     */
    @Test
    void constructJsonHasExpectedFieldsFullDetailsTrueTest() {
       JSONObject json = testBusiness1.constructJson(true);
       assertTrue(json.containsKey("name"));
       assertTrue(json.containsKey("description"));
       assertTrue(json.containsKey("businessType"));
       assertTrue(json.containsKey("address"));
       assertTrue(json.containsKey("id"));
       assertTrue(json.containsKey("primaryAdministratorId"));
       assertTrue(json.containsKey("administrators"));
       assertTrue(json.containsKey("created"));
    }

    /**
     * Test that the JSONObject returned by constructJson contains the fields id, primaryAdministratorId,
     * name, description, businessType, created and address when constructJson is
     * called with false or no argument.
     */
    @Test
    void constructJsonHasExpectedFieldsFullDetailsFalseTest() {
        List<JSONObject> testJsons = new ArrayList<>();
        testJsons.add(testBusiness1.constructJson(false));
        testJsons.add(testBusiness1.constructJson());
        for (JSONObject json : testJsons) {
            assertTrue(json.containsKey("name"));
            assertTrue(json.containsKey("description"));
            assertTrue(json.containsKey("businessType"));
            assertTrue(json.containsKey("address"));
            assertTrue(json.containsKey("id"));
            assertTrue(json.containsKey("primaryAdministratorId"));
            assertTrue(json.containsKey("created"));
        }
    }

    /**
     * Test that the JSONObject returned by contructJson does not contain any fields other than
     * id, primaryAdministratorId, name, description, businessType, created, administraters and address,
     * whether constructJson is called with true as its argument.
     */
    @Test
    void constructJsonDoesntHaveUnexpectedFieldsFullDetailsTrueTest() {
        JSONObject json = testBusiness1.constructJson(true);
        json.remove("name");
        json.remove("description");
        json.remove("businessType");
        json.remove("address");
        json.remove("id");
        json.remove("primaryAdministratorId");
        json.remove("administrators");
        json.remove("created");
        assertTrue(json.isEmpty());
    }

    /**
     * Test that the JSONObject returned by contructJson does not contain any fields other than
     * id, primaryAdministratorId, name, description, businessType, created and address,
     * whether constructJson is called with false or no argument.
     */
    @Test
    void constructJsonDoesntHaveUnexpectedFieldsFullDetailsFalseTest() {
        List<JSONObject> testJsons = new ArrayList<>();
        testJsons.add(testBusiness1.constructJson(false));
        testJsons.add(testBusiness1.constructJson());
        for (JSONObject json : testJsons) {
            json.remove("name");
            json.remove("description");
            json.remove("businessType");
            json.remove("address");
            json.remove("id");
            json.remove("primaryAdministratorId");
            json.remove("created");
            assertTrue(json.isEmpty());
        }
    }

    /**
     * Test that id, primaryAdministratorId, name, description, businessType, created, and address
     * in the JSONObject returned by constructJson have the expecte value, whether cosntructJson is
     * called with true, false or no argument.
     */
    @Test
    void constructJsonSimpleFieldsHaveExpectedValueTest() {
        List<JSONObject> testJsons = getTestJsons(testBusiness1);
        for (JSONObject json : testJsons) {
            assertEquals(testBusiness1.getName(), json.getAsString("name"));
            assertEquals(testBusiness1.getDescription(), json.getAsString("description"));
            assertEquals(testBusiness1.getBusinessType(), json.getAsString("businessType"));
            assertEquals(testBusiness1.getAddress().constructFullJson().toString(), json.getAsString("address"));
            assertEquals(testBusiness1.getId().toString(), json.getAsString("id"));
            assertEquals(testBusiness1.getPrimaryOwner().getUserID().toString(), json.getAsString("primaryAdministratorId"));
            assertEquals(testBusiness1.getCreated().toString(), json.getAsString("created"));
        }
    }

    /**
     * Test that when constructJson is called with true as its argument, the administrators field
     * contains a list of User JSONs with the details of the business's administrators.
     */
    @Test
    void constructJsonAdministratorsFullDetailsTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(2, testBusiness1.getOwnerAndAdministrators().size());
        List<User> admins = new ArrayList<>();
        admins.addAll(testBusiness1.getOwnerAndAdministrators());
        Collections.sort(admins, (User user1, User user2) ->
           user1.getUserID().compareTo(user2.getUserID()));
        JSONArray expectedAdminArray = new JSONArray();
        for (User user : admins) {
            expectedAdminArray.add(user.constructPublicJson());
        }
        String expectedAdminString = expectedAdminArray.toJSONString();
        JSONObject testJson = testBusiness1.constructJson(true);
        assertEquals(expectedAdminString, testJson.getAsString("administrators"));
    }

    /**
     * Test that when the business has a primary owner but not administrators, the getOwnerAndAdministrators
     * method will return a set containing just the business's primary owner.
     */
    @Test
    void getOwnerAndAdministratorsNoAdministratorsTest() {
        assertEquals(1, testBusiness1.getOwnerAndAdministrators().size());
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser1));
    }

    /**
     * Test that when the business has administrators added in addition to its primary owner, the
     * getOwnerAndAdministrators method will return a set containing the owner and all administrators
     * of the business.
     */
    @Test
    void getOwnerAndAdministratorsAdminsAddedTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(2, testBusiness1.getOwnerAndAdministrators().size());
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser1));
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser2));
    }




    /**
     * Test that the checkSessionPermissions method will throw an AccessTokenException when called
     * with a HTTP request that does not contain an authentication token (i.e. the user has not logged in).
     */
    @Test
    void checkSessionPermissionsNoAuthenticationTokenTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> null);
        assertThrows(AccessTokenException.class, () -> {
            testBusiness1.checkSessionPermissions(request);
        });
    }

    /**
     * Test that the checkSessionPermissions method will throw a ResponseStatusException with status
     * code 403 when called with a request from a user who is not an admin of the business or a global
     * application admin.
     */
    @Test
    void checkSessionPermissionsUserWithoutPermissionTest() {
        Long user2Id = userRepository.findByEmail("dave@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            invocation -> "user");
        when(session.getAttribute("accountId")).thenAnswer(
            invocation -> user2Id);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.checkSessionPermissions(request);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * an admin of the business, no exception is thrown.
     */
    @Test
    void checkSessionPermissionsBusinessAdminTest() {
        Long user1Id = userRepository.findByEmail("johnsmith99@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            invocation -> "user");
        when(session.getAttribute("accountId")).thenAnswer(
            invocation -> user1Id);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            fail("No exception should be thrown when the user is an admin of the business");
        }
    }

    /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * a global application admin, no exception is thrown.
     */
    @Test
    void checkSessionPermissionsGlobalApplicationAdminTest() {
        Long user2Id = userRepository.findByEmail("dave@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            invocation -> "globalApplicationAdmin");
        when(session.getAttribute("accountId")).thenAnswer(
            invocation -> user2Id);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            fail("No exception should be thrown when the user is a global application admin");
        }
    }

    /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * a global application admin, no exception is thrown.
     */
    @Test
    void checkSessionPermissionsDefaultGlobalApplicationAdminTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
                invocation -> "defaultGlobalApplicationAdmin");
        when(session.getAttribute("accountId")).thenAnswer(
                invocation -> null);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            fail("No exception should be thrown when the user is the default global application admin");
        }
    }

    @Test
    void getCatalogue_multipleImages_noDuplicates() {
        Image testImage1 = imageRepository.save(new Image("filename1", "thumbnailFilename1"));
        Image testImage2 = imageRepository.save(new Image("filename2", "thumbnailFilename2"));
        Image testImage3 = imageRepository.save(new Image("filename3", "thumbnailFilename3"));
        Image testImage4 = imageRepository.save(new Image("filename4", "thumbnailFilename4"));

        Product testProduct1 = new Product.Builder()
            .withName("Product 1")
            .withProductCode("PROD1")
            .withBusiness(testBusiness1)
            .build();
        testProduct1 = productRepository.save(testProduct1);      
        testProduct1.addProductImage(testImage1);
        testProduct1.addProductImage(testImage2);
        testProduct1 = productRepository.save(testProduct1);
          
        Product testProduct2 = new Product.Builder()
            .withName("Product 2")
            .withProductCode("PROD2")
            .withBusiness(testBusiness1)
            .build();
        testProduct2 = productRepository.save(testProduct2);
        testProduct2.addProductImage(testImage3);
        testProduct2.addProductImage(testImage4);
        testProduct2 = productRepository.save(testProduct2);
        

        testBusiness1 = businessRepository.save(testBusiness1);
        assertEquals(2, productRepository.getAllByBusiness(testBusiness1).size());
    }
}
