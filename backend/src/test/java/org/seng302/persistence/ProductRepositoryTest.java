package org.seng302.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.seng302.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ImageRepository imageRepository;

    private User testUser;
    private Business testBusiness;
    private Product testProduct;
    private Image testImage;

    private User testUser2;
    private Business testBusiness2;
    private Product testProduct2;

    /**
     * Creates a user, business and product objects for use within the unit tests, where the
     * user is the owner of the business and the product exists within the business's
     * product catalogue.
     * @throws ParseException from the date attribute within the user object
     */
    private void setUpTestObjects() throws ParseException {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        imageRepository.deleteAll();
        productRepository.deleteAll();

        testUser = new User.Builder()
                .withFirstName("Fergus")
                .withMiddleName("Connor")
                .withLastName("Hitchcock")
                .withNickName("Ferg")
                .withEmail("fergus.hitchcock@gmail.com")
                .withPassword("IDoLikeBreaks69#H3!p")
                .withBio("Did you know I had a second last name Yarker")
                .withDob("1999-07-17")
                .withPhoneNumber("+64 27 370 2682")
                .withAddress(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .build();
        userRepository.save(testUser);

        testBusiness = new Business.Builder()
                .withName("Help Industries")
                .withAddress(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .withBusinessType("Accommodation and Food Services")
                .withDescription("Helps industries hopefully")
                .withPrimaryOwner(testUser)
                .build();
        testBusiness =  businessRepository.save(testBusiness);

        testImage = new Image("photo_of_connor.png", "photo_of_connor_thumbnail.png");
        imageRepository.save(testImage);

        testProduct = new Product.Builder()
                .withProductCode("PIECEOFFISH69")
                .withName("A Piece of Fish")
                .withDescription("A fish but only a piece of it remains")
                .withManufacturer("Tokyo Fishing LTD")
                .withRecommendedRetailPrice("3.20")
                .withBusiness(testBusiness)
                .build();
        testProduct.setProductImages(Arrays.asList(testImage));
        productRepository.save(testProduct);
        testBusiness = businessRepository.save(testBusiness);

        testUser2 = new User.Builder()
                .withFirstName("Ferguss")
                .withMiddleName("Connorr")
                .withLastName("Hitchcockk")
                .withNickName("Fergg")
                .withEmail("fergus.hitchcockk@gmail.com")
                .withPassword("IDoLikeBreaks69#H3!pp")
                .withBio("Did you know I had a second last name Yarker two")
                .withDob("1999-07-18")
                .withPhoneNumber("+64 27 470 2682")
                .withAddress(Location.covertAddressStringToLocation("7,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .build();
        userRepository.save(testUser2);

        testBusiness2 = new Business.Builder()
                .withName("Help Industries")
                .withAddress(Location.covertAddressStringToLocation("6,Help Street,Place,Dunedin,New Zelaand,Otago,6959"))
                .withBusinessType("Accommodation and Food Services")
                .withDescription("Helps industries hopefully")
                .withPrimaryOwner(testUser2)
                .build();
        testBusiness2 = businessRepository.save(testBusiness2);

        testProduct2 = new Product.Builder()
                .withProductCode("PIECEOFFISHY69")
                .withName("A Piece of Fishy")
                .withDescription("A fishy but only a piece of it remains")
                .withManufacturer("Tokyo Fishying LTD")
                .withRecommendedRetailPrice("4.20")
                .withBusiness(testBusiness2)
                .build();
        productRepository.save(testProduct2);
        testBusiness2 = businessRepository.save(testBusiness2);
    }

    @BeforeEach
    void setUp() throws ParseException {
        setUpTestObjects();
    }

    @AfterAll
    void tearDown() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
        imageRepository.deleteAll();
    }

    //Tests for getProduct helper function
    /**
     * Checks that a product that exists within the database and belongs to a catalogue can be retrieved.
     */
    @Test
    void getProduct_productExists_getExpectedProduct() {
        Product actualProduct = productRepository.getProductByBusinessAndProductCode(testBusiness, testProduct.getProductCode());
        assertEquals(testProduct.getProductCode(), actualProduct.getProductCode());
    }

    /**
     * Checks that a products that exists but in a different business's catalogue cannot be retrieved.
     */
    @Test
    void getProduct_productExistsInDifferentCatalogue_406ResponseException() {
        assertThrows(ResponseStatusException.class, () -> {
            productRepository.getProductByBusinessAndProductCode(testBusiness2, testProduct.getProductCode());
        });
    }

    /**
     * Checks that a product that does not exist cannot be retrieved.
     */
    @Test
    void getProduct_productDoesNotExist_406ResponseException() throws Exception {
        productRepository.delete(testProduct);
        testBusiness = businessRepository.getBusinessById(testBusiness.getId());
        assertThrows(ResponseStatusException.class, () -> {
            productRepository.getProductByBusinessAndProductCode(testBusiness, testProduct.getProductCode());
        });
    }
}
