package org.seng302.controllers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.seng302.entities.*;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.ImageRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class ProductControllerDeleteImageTests {

    @Autowired
    private MockMvc mockMvc;
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

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;

    /**
     * This method creates an authentication code for sessions and cookies.
     */
    private void setUpAuthCode(boolean valid) {
        StringBuilder authCodeBuilder = new StringBuilder();
        authCodeBuilder.append("0".repeat(64));
        String authCode = authCodeBuilder.toString();
        if (valid) {
            sessionAuthToken.put("AUTHTOKEN", authCode);
        } else {
            sessionAuthToken.put("AUTHTOKEN", "GIBBERISH");
        }
        authCookie = new Cookie("AUTHTOKEN", authCode);
    }

    /**
     * Mocks logging in as a particular user
     * @param userId The ID of the user to log in as
     */
    private void setActiveUser(Long userId) {
        sessionAuthToken.put("accountId", userId);
    }

    /**
     * Tags a session as dgaa
     */
    private void setUpDGAAAuthCode() {
        sessionAuthToken.put("role", "defaultGlobalApplicationAdmin");
    }

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
        businessRepository.save(testBusiness);

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
        businessRepository.save(testBusiness2);

        testProduct2 = new Product.Builder()
                .withProductCode("PIECEOFFISHY69")
                .withName("A Piece of Fishy")
                .withDescription("A fishy but only a piece of it remains")
                .withManufacturer("Tokyo Fishying LTD")
                .withRecommendedRetailPrice("4.20")
                .withBusiness(testBusiness2)
                .build();
        productRepository.save(testProduct2);
    }

    @BeforeEach
    void setUp() throws ParseException {
        setUpAuthCode(true);
        setUpTestObjects();
    }

    @AfterAll
    void tearDown() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
        imageRepository.deleteAll();
    }

    /**
     * Tests using the delete product image method to see if a product with an image will have its image deleted.
     * This is done by calling the API endpoint to delete a product image and checking if it not longer has an image
     */
    @Test
    void deleteProductImage_hasImage_imageDeleted() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk());
    }

    /**
     * Tests using the delete product image method to see if a product without an image will respond with the not
     * acceptable response code.
     */
    @Test
    void deleteProductImage_noImage_406Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), 999);
        testProduct.setProductImages(Arrays.asList());
        productRepository.save(testProduct);
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isNotAcceptable());
    }

    /**
     * Tests using the delete image method to see if a request with an invalid business ID will return a not acceptable
     * response code.
     */
    @Test
    void deleteProductImage_invalidBusinessID_406Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                999, testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isNotAcceptable());
    }

    /**
     * Tests using the delete image method to see if a request with an invalid product ID will return a not acceptable
     * response code.
     */
    @Test
    void deleteProductImage_invalidProductID_406Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), "NOTAPRODUCT999", testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isNotAcceptable());
    }

    /**
     * Tests using the delete product image method to see if a product with a valid authentication token has permission
     * to delete an image.
     */
    @Test
    void deleteProductImage_validAuthToken_hasPermission() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk());
    }

    /**
     * Tests using the delete product image method to see if a user without a authentication token cannot delete an image.
     * A unauthorised response code should be given back when the API endpoint is called under these conditions.
     */
    @Test
    void deleteProductImage_noAuthToken_401Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .cookie(authCookie))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Tests using the delete product image method to see if a user without an invalid authentication token cannot delete
     * an image. A unauthorised response code should be given back when the API endpoint is called under these conditions.
     */
    @Test
    void deleteProductImage_invalidAuthToken_401Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), testImage.getID());
        setUpAuthCode(false);
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Tests using the delete product image method to see if a user who is not a DGAA and just a regular user cannot
     * delete the image.
     */
    @Test
    void deleteProductImage_isNotDGAA_403Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests using the delete image method to see if a DGAA without being a business owner can delete images products.
     */
    @Test
    void deleteProductImage_isDGAA_imageDeleted() throws Exception {
        setUpDGAAAuthCode();
        testUser.setRole("globalApplicationAdmin");
        userRepository.save(testUser);

        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk());
    }

    /**
     * Tests using the delete image method to see if the business administrator can delete images within there
     * businesses product catalogue.
     */
    //Waiting till we redo how we do DGAA checking
    @Test
    void deleteProductImage_isBusinessAdmin_imageDeleted() throws Exception {
        setActiveUser(testUser.getUserID());
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk());
    }

    /**
     * Tests using the delete image method to see if a user who is not a business administrator cannot delete images
     * from products.
     */
    @Test
    void deleteProductImage_notBusinessAdmin_403Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness2.getId(), testProduct.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests using the delete image method to see if a user who is a business administrator cannot delete images from
     * products that exist in a different business's product catalogue
     */
    @Test
    void deleteProductImage_isBusinessAdminForWrongCatalogue_403Response() throws Exception {
        String url = String.format("/businesses/%d/products/%s/images/%d",
                testBusiness.getId(), testProduct2.getProductCode(), testImage.getID());
        mockMvc.perform( MockMvcRequestBuilders
                .delete(url)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isForbidden());
    }
}
