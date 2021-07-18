package org.seng302.entities;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductTests {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    UserRepository userRepository;

    private User testUser1;
    private Business testBusiness1;
    private Business testBusiness2;

    /**
     * Created a business objects for testing
     */
    void createTestBusinesses() {
        testBusiness1 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName1")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);

        testBusiness2 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description 2")
                .withName("BusinessName2")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness2 = businessRepository.save(testBusiness2);
    }

    @BeforeAll
    void setUp() throws ParseException {
        businessRepository.deleteAll();
        userRepository.deleteAll();

        // Add a test user that will be the owner of both businesses
        testUser1 = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith98@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser1 = userRepository.save(testUser1);
        createTestBusinesses();
    }

    @AfterAll
    void tearDown() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    /**
     * Test that a product object can be created and all its attributes are what they are expected to be.
     */
    @Test
    void createValidProduct() {
        Product product = new Product.Builder()
                .withProductCode("ORANGE-69")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.01")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product);

        assertEquals("ORANGE-69", product.getProductCode());
        assertEquals("Fresh Orange", product.getName());
        assertEquals("This is a fresh orange", product.getDescription());
        assertEquals("Apple", product.getManufacturer());
        assertEquals(new BigDecimal("2.01"), product.getRecommendedRetailPrice());
        assertEquals(testBusiness1.getAddress().getCountry(), product.getCountryOfSale());
    }

    /**
     * Test that the auto generated IDs of the products are unique and a newly created product's id does not match
     * the previously created product's id.
     */
    @Test
    void checkTwoProductsDoNotHaveTheSameIDs() {
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("ORANGE-70")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.02")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product1);
        productRepository.save(product2);

        assertNotEquals(product1.getID(), product2.getID());
    }

    /**
     * Test the date that is generated when a product is created is today (when the product is created).
     */
    @Test
    void checkDate() {
        LocalDate now = LocalDate.now();
        Product product = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product);
        Instant productDate = product.getCreated();

        assertTrue(ChronoUnit.SECONDS.between(Instant.now(), productDate) < 20);
    }

    /**
     * Check that a product with the same code as one in a catalogue cannot be added to said catalogue, because two
     * products with the same code cannot be in the same catalogue.
      */
    @Test
    void checkNoTwoSameProductCodesWithinSameCatalogue() {
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple maybe")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.02")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product1);

        assertThrows(Exception.class, () -> productRepository.save(product2));
    }

    /**
     * Check that two products with the same code can be added to different catalogues
     */
    @Test
    void checkTwoSameProductCodesWithinDifferentCatalogues() {
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple maybe")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.02")
                .withBusiness(testBusiness2)
                .build();
        productRepository.save(product1);

        assertDoesNotThrow(() -> productRepository.save(product2));
    }

    /**
     * Checks that two products with different product codes can be added to the same catalogue.
     */
    @Test
    void checkTwoDifferentProductCodesWithinSameCatalogue() {
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple maybe")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.02")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product1);

        assertDoesNotThrow(() -> productRepository.save(product2));
    }

    /**
     * Checks the product is connected to the business's catalogue by checking the mentioned product is the same as
     * the one in the catalogue.
     */
    @Test
    void checkTheProductIsConnectedToTheBusinessCatalogue() {
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product1);
        testBusiness1 = businessRepository.findByName("BusinessName1");
        List<Product> catalogue = productRepository.getAllByBusiness(testBusiness1);

        assertEquals(product1.getID(), catalogue.get(0).getID());
    }

    /**
     * Checks that several products are successfully added to the business's catalogue
     */
    @Test
    void createAValidListOfProductsInACatalogue() {
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        Product product2 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-71")
                .withName("The Nathan Apple Two")
                .withDescription("Ever wonder why Nathan has an apple too")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.04")
                .withBusiness(testBusiness1)
                .build();
        Product product3 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-72")
                .withName("The Nathan Apple Three")
                .withDescription("Ever wonder why Nathan has an apple too maybe")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.05")
                .withBusiness(testBusiness1)
                .build();
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        testBusiness1 = businessRepository.findByName("BusinessName1");
        List<Product> catalogue = productRepository.getAllByBusiness(testBusiness1);

        assertEquals(3, catalogue.size());
    }

    /**
     * Checks that deleting a business while it still has remaining products doesn't result in an error
     */
    @Test
    void testDeletingBusinessWithProducts() {
        Business tempBusinessInitial = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("This business will be deleted")
                .withName("Temp Name")
                .withPrimaryOwner(testUser1)
                .build();
        Business tempBusiness = businessRepository.save(tempBusinessInitial);
        Product product1 = new Product.Builder()
                .withProductCode("NATHAN-APPLE-69")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.01")
                .withBusiness(tempBusiness).build();
        productRepository.save(product1);

        assertDoesNotThrow(() -> businessRepository.delete(tempBusiness));
    }

    /**
     * Tests that a product can be found using "findByBusinessAndProductCode"
     */
    @Test
    void testFindByBusinessAndProductCode() {
        Product product = productRepository.save(
                new Product.Builder()
                    .withProductCode("NATHAN-APPLE-70")
                    .withName("The Nathan Apple")
                    .withDescription("Ever wonder why Nathan has an apple")
                    .withManufacturer("Apple")
                    .withRecommendedRetailPrice("9000.03")
                    .withBusiness(testBusiness1)
                    .build()
        );

        Product foundProduct = productRepository.findByBusinessAndProductCode(testBusiness1, "NATHAN-APPLE-70").get();
        assertNotNull(foundProduct);

        assertEquals(product.getID(), foundProduct.getID());
        assertEquals(product.getProductCode(), foundProduct.getProductCode());
        assertEquals(product.getName(), foundProduct.getName());
        assertEquals(product.getDescription(), foundProduct.getDescription());
        assertEquals(product.getManufacturer(), foundProduct.getManufacturer());
        assertEquals(product.getRecommendedRetailPrice(), foundProduct.getRecommendedRetailPrice());
        assertEquals(product.getBusiness().getId(), foundProduct.getBusiness().getId());
    }

    /**
     * Tests that "findByBusinessAndProductCode" returns null if no product exists with the given business and product code
     */
    @Test
    void testFindByBusinessAndProductCodeIsNullIfNoProduct() {
        // Product with same code saved to a different business
        productRepository.save(
                new Product.Builder()
                        .withProductCode("NATHAN-APPLE-70")
                        .withName("The Nathan Apple")
                        .withDescription("Ever wonder why Nathan has an apple")
                        .withManufacturer("Apple")
                        .withRecommendedRetailPrice("9000.03")
                        .withBusiness(testBusiness2)
                        .build()
        );

        Optional<Product> foundProduct = productRepository.findByBusinessAndProductCode(testBusiness1, "NATHAN-APPLE-70");
        assertTrue(foundProduct.isEmpty());
    }

    /**
     * Tests that "findByBusinessAndProductCode" returns null if the product is deleted
     */
    @Test
    void testFindByBusinessAndProductCodeIsNullIfProductHasBeenDeleted() {
        // Product with same code saved to a different business
        Product product = productRepository.save(
                new Product.Builder()
                        .withProductCode("NATHAN-APPLE-70")
                        .withName("The Nathan Apple")
                        .withDescription("Ever wonder why Nathan has an apple")
                        .withManufacturer("Apple")
                        .withRecommendedRetailPrice("9000.03")
                        .withBusiness(testBusiness1)
                        .build()
        );

        productRepository.delete(product);

        Optional<Product> foundProduct = productRepository.findByBusinessAndProductCode(testBusiness1, "NATHAN-APPLE-70");
        assertTrue(foundProduct.isEmpty());
    }

    /**
     * Tests that trying the change a product's business using "addToCatalogue" fails.
     */
    @Test
    void testUsingAddToCatalogueFails() {
        Product product = productRepository.save(
                new Product.Builder()
                        .withProductCode("NATHAN-APPLE-70")
                        .withName("The Nathan Apple")
                        .withDescription("Ever wonder why Nathan has an apple")
                        .withManufacturer("Apple")
                        .withRecommendedRetailPrice("9000.03")
                        .withBusiness(testBusiness1)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () -> testBusiness2.addToCatalogue(product));
    }




    /**
     * Tests that a product built without a product code throws a ResponseStatusException
     */
    @Test
    void buildWithoutProductCode() {
        var builder = new Product.Builder()
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that a product built with a product code that is too long (>15 characters) throws a ResponseStatusException
     */
    @Test
    void buildWithLongProductCode() {
        var builder = new Product.Builder()
                .withProductCode("A".repeat(16))
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that a product built with a product code that is empty throws a ResponseStatusException
     */
    @Test
    void buildWithEmptyProductCode() {
        var builder = new Product.Builder()
                .withProductCode("")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that a product built with a product code that contains an invalid character
     * (not a uppercase letter, number or dash) throws a ResponseStatusException
     */
    @ParameterizedTest
    @ValueSource(strings = {"a", "z", "_", ",", ".", " ", "\t", "\n"})
    void buildWithInvalidCharactersInProductCode(String character) {
        var builder = new Product.Builder()
                .withProductCode(character)
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that a product built without a name throws a ResponseStatusException
     */
    @Test
    void buildWithoutName() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that a product built with an empty name throws a ResponseStatusException
     */
    @Test
    void buildWithEmptyName() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that the Product name cannot have characters that are neither a letter, number, space or punctuation.
     * @param name The test name to use
     */
    @ParameterizedTest
    @ValueSource(strings = {"\n", "\t", "\uD83D\uDE02", "\uFFFF"})
    void buildWithInvalidCharactersInName(String name) {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName(name)
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that the Product can have a name using letters, numbers, spaces and punctuation
     * @param name The test name to use
     */
    @ParameterizedTest
    @ValueSource(strings = {" ", ":", ",", "7", "é", "树"})
    void buildWithValidCharactersInName(String name) {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName(name)
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertDoesNotThrow(builder::build);
    }

    /**
     * Tests that a product built with a long name (>50 characters) throws a ResponseStatusException
     */
    @Test
    void buildWithLongName() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("a".repeat(51))
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that the Product description cannot have characters that are neither a letter, number, whitespace or punctuation.
     * @param name The test name to use
     */
    @ParameterizedTest
    @ValueSource(strings = {"\uD83D\uDE02", "\uFFFF"})
    void buildWithInvalidCharactersInDescription(String name) {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription(name)
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that the Product can have a description using letters, numbers, whitespace and punctuation
     * @param description The test description to use
     */
    @ParameterizedTest
    @ValueSource(strings = {" ", "\n", "\t",  ":", ",", "7", "é", "树"})
    void buildWithValidCharactersInDescription(String description) {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription(description)
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertDoesNotThrow(builder::build);
    }

    /**
     * Tests that a product built with a long description (>200 characters) throws a ResponseStatusException
     */
    @Test
    void buildWithLongDescription() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("a".repeat(201))
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that the Product manufacturer cannot have characters that are neither a letter, number, space or punctuation.
     * @param manufacturer The test manufacturer to use
     */
    @ParameterizedTest
    @ValueSource(strings = {"\n", "\t", "\uD83D\uDE02", "\uFFFF"})
    void buildWithInvalidCharactersInManufacturer(String manufacturer) {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer(manufacturer)
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that the Product can have a manufacturer using letters, numbers, spaces and punctuation
     * @param manufacturer The test manufacturer to use
     */
    @ParameterizedTest
    @ValueSource(strings = {" ", ":", ",", "7", "é", "树"})
    void buildWithValidCharactersInManufacturer(String manufacturer) {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer(manufacturer)
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertDoesNotThrow(builder::build);
    }

    /**
     * Tests that a product built with a long manufacturer (>100 characters) throws a ResponseStatusException
     */
    @Test
    void buildWithLongManufacturer() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("a".repeat(101))
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that a product built with a recommended retail price that is not parsable into a number throws a
     * ResponseStatusException
     */
    @Test
    void buildWithNonNumberPrice() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withBusiness(testBusiness1);

        // Maybe it is worth delaying this exception until build
        assertThrows(ResponseStatusException.class, () -> builder.withRecommendedRetailPrice("pricen't"));
    }

    /**
     * Tests that a product built with a negative recommended retail price throws a ResponseStatusException
     */
    @Test
    void buildWithNegativePrice() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("-1")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Tests that a product built with a large recommended retail price (>=100000) throws a ResponseStatusException
     */
    @Test
    void buildWithLargePrice() {
        var builder = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("100000")
                .withBusiness(testBusiness1);
        assertThrows(ResponseStatusException.class, builder::build);
    }

    /**
     * Check that when setCountryOfSale is called with a string that is not blank, contains only letters and spaces
     * and is less than 100 characters, the product's country of sale is set to that country.
     */
    @Test
    void setCountryOfSaleValidCountryTest() {
        String[] validCountries = {"New Zealand", "MADEUPCOUNTRY", "another country"};
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        for (String country : validCountries) {
            testProduct.setCountryOfSale(country);
            assertEquals(country, testProduct.getCountryOfSale());
        }
    }

    /**
     * Check that when setCountryOfSale is called with a string that contains characters which are not letters or spaces,
     * a response status exception is thrown and the product's country is not set to that string.
     */
    @Test
    void setCountryOfSaleInvalidCharactersTest() {
        String[] invalidCountries = {"-", "country123", "New Zealand!"};
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        for (String country : invalidCountries) {
            assertThrows(ResponseStatusException.class, ()  -> testProduct.setCountryOfSale(country));
            assertEquals(testBusiness1.getAddress().getCountry(), testProduct.getCountryOfSale());
        }
    }

    /**
     * Check that when setCountryOfSale is called with a string that is over 100 characters long,
     * a response status exception is thrown and the product's country is not set to that string.
     */
    @Test
    void setCountryOfSaleTooManyCharactersTest() {
        String[] invalidCountries = new String[2];
        invalidCountries[0] = "a".repeat(101);
        invalidCountries[1] = "Long Country Name ".repeat(100);
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        for (String country : invalidCountries) {
            assertThrows(ResponseStatusException.class, ()  -> testProduct.setCountryOfSale(country));
            assertEquals(testBusiness1.getAddress().getCountry(), testProduct.getCountryOfSale());
        }
    }

    /**
     * Check that when setCountryOfSale is called with a string that is null, empty or blank,
     * a response status exception is thrown and the product's country is not set to that string.
     */
    @Test
    void setCountryOfSaleEmptyTest() {
        String[] invalidCountries = {null, "", "     "};
        invalidCountries[1] = "Long Country Name ".repeat(100);
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        for (String country : invalidCountries) {
            assertThrows(ResponseStatusException.class, ()  -> testProduct.setCountryOfSale(country));
            assertEquals(testBusiness1.getAddress().getCountry(), testProduct.getCountryOfSale());
        }
    }

    @Test
    void constructJsonObject_noNullAttributes_allExpectedFieldsPresent() {
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        JSONObject testJson = testProduct.constructJSONObject();
        assertTrue(testJson.containsKey("id"));
        assertTrue(testJson.containsKey("name"));
        assertTrue(testJson.containsKey("description"));
        assertTrue(testJson.containsKey("manufacturer"));
        assertTrue(testJson.containsKey("recommendedRetailPrice"));
        assertTrue(testJson.containsKey("created"));
        assertTrue(testJson.containsKey("images"));
        assertTrue(testJson.containsKey("countryOfSale"));
    }

    @Test
    void constructJsonObject_noNullAttributes_noUnexpectedFieldsPresent() {
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        JSONObject testJson = testProduct.constructJSONObject();
        testJson.remove("id");
        testJson.remove("name");
        testJson.remove("description");
        testJson.remove("manufacturer");
        testJson.remove("recommendedRetailPrice");
        testJson.remove("created");
        testJson.remove("images");
        testJson.remove("countryOfSale");
        assertTrue(testJson.isEmpty());
    }

    @Test
    void constructJsonObject_noNullAttributes_allHaveExpectedValue() {
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withDescription("Ever wonder why Nathan has an apple")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("9000.03")
                .withBusiness(testBusiness1)
                .build();
        JSONArray images = new JSONArray();
        for (Image image : testProduct.getProductImages()) {
            images.add(image.constructJSONObject());
        }
        String imageString = images.toString();
        JSONObject testJson = testProduct.constructJSONObject();
        assertEquals(testProduct.getProductCode(), testJson.getAsString("id"));
        assertEquals(testProduct.getName(), testJson.getAsString("name"));
        assertEquals(testProduct.getDescription(), testJson.getAsString("description"));
        assertEquals(testProduct.getManufacturer(), testJson.getAsString("manufacturer"));
        assertEquals(testProduct.getRecommendedRetailPrice().toString(), testJson.getAsString("recommendedRetailPrice"));
        assertEquals(testProduct.getCreated().toString(), testJson.getAsString("created"));
        assertEquals(imageString, testJson.getAsString("images"));
        assertEquals(testProduct.getCountryOfSale(), testJson.getAsString("countryOfSale"));
    }

    @Test
    void constructJsonObject_optionalAttributesNull_allExpectedFieldsPresent() {
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withBusiness(testBusiness1)
                .build();
        JSONObject testJson = testProduct.constructJSONObject();
        assertTrue(testJson.containsKey("id"));
        assertTrue(testJson.containsKey("name"));
        assertTrue(testJson.containsKey("created"));
        assertTrue(testJson.containsKey("images"));
        assertTrue(testJson.containsKey("countryOfSale"));
    }

    @Test
    void constructJsonObject_optionalAttributesNull_noUnexpectedFieldsPresent() {
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withBusiness(testBusiness1)
                .build();
        JSONObject testJson = testProduct.constructJSONObject();
        testJson.remove("id");
        testJson.remove("name");
        testJson.remove("created");
        testJson.remove("images");
        testJson.remove("countryOfSale");
        assertTrue(testJson.isEmpty());
    }

    @Test
    void constructJsonObject_optionalAttributesNull_allHaveExpectedValue() {
        Product testProduct = new Product.Builder()
                .withProductCode("NATHAN-APPLE-70")
                .withName("The Nathan Apple")
                .withBusiness(testBusiness1)
                .build();
        JSONArray images = new JSONArray();
        for (Image image : testProduct.getProductImages()) {
            images.add(image.constructJSONObject());
        }
        String imageString = images.toString();
        JSONObject testJson = testProduct.constructJSONObject();
        assertEquals(testProduct.getProductCode(), testJson.getAsString("id"));
        assertEquals(testProduct.getName(), testJson.getAsString("name"));
        assertEquals(testProduct.getCreated().toString(), testJson.getAsString("created"));
        assertEquals(imageString, testJson.getAsString("images"));
        assertEquals(testProduct.getCountryOfSale(), testJson.getAsString("countryOfSale"));
    }

}
