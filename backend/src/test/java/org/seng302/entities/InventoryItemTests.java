package org.seng302.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryItemTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryItemRepository inventoryItemRepository;

    private Business testBusiness;
    private Product testProduct;

    void createTestObjects() throws ParseException {
        clearDatabase();
        User testUser = new User.Builder()
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
        testUser = userRepository.save(testUser);
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName1")
                .withPrimaryOwner(testUser)
                .build();
        testBusiness = businessRepository.save(testBusiness);

        Product product = new Product.Builder()
                .withProductCode("ORANGE-69")
                .withName("Fresh Orange")
                .withDescription("This is a fresh orange")
                .withManufacturer("Apple")
                .withRecommendedRetailPrice("2.01")
                .withBusiness(testBusiness)
                .build();
        testProduct = productRepository.save(product);
    }
    void clearDatabase() {
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }
    @BeforeAll
    void setUp() throws ParseException {
        createTestObjects();
    }
    @AfterAll
    void tearDown() {
        clearDatabase();
    }
    @Test
    void createInventoryItem_withAllFields_objectCreated() throws Exception {
        LocalDate today = LocalDate.now();

        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withOnlyRequiredFields_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    /**
     * Test that check mandatory cant be null
    */

    @Test
    void createInventoryItem_withNullProduct_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(null)
                    .withQuantity(2)
                    .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                    .build();
            // fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No product was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    @Test
    void createInventoryItem_withNullExpires_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(1)
                    .withExpires(null)
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No expiry date was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    @Test
    void createInventoryItem_withZeroQuantity_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(0)
                    .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("A quantity less than 1 was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    @Test
    void createInventoryItem_withNegativeQuantity_exceptionThrown() throws Exception {
        try {
            InventoryItem invItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(-69)
                    .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                    .build();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("A quantity less than 1 was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }
    /**
     * The following test section will test attributes related to price which is 
     * Price per item, Total price
     * Test condition will be (Null, validPriceFormat, InvalidPriceFormat, 
     * LowerThanPriceRange, HigherThanPriceRange, ExactlyOnPriceRangeEnd)
     */

    @Test
    void createInventoryItem_withNullPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withPricePerItem(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withNullTotalPriceAndNullPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withPricePerItem(null)
                .withTotalPrice(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withNullTotalPrice_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withTotalPrice(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withPricePerItem_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withPricePerItem("2.69")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withTotalPrice_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withTotalPrice("21.69")
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withInvalidPricePerItemFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());
        assertThrows(ResponseStatusException.class, () -> {
            builder.withPricePerItem("xx0.1");
        });
    }
    @Test
    void createInventoryItem_withInvalidTotalPriceFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());
        assertThrows(ResponseStatusException.class, () -> {
            builder.withTotalPrice("xx0.1");
        });
    }
    @Test
    void createInventoryItem_withNegativePricePerItem_objectNotCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
        .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setPricePerItem(new BigDecimal ("-1"));
        });
    }
    @Test
    void createInventoryItem_withNegativeTotalPrice_objectNotCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
        .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setTotalPrice(new BigDecimal ("-2"));
        });
    }
    @Test
    void createInventoryItem_withHigherPricePerItem_objectNotCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
        .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setPricePerItem(new BigDecimal ("10001"));
        });
    }
    @Test
    void createInventoryItem_withHigherTotalPrice_objectNotCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires("2021-06-01")
        .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setTotalPrice(new BigDecimal ("1000001"));
        });
    }
    @Test
    void createInventoryItem_withHighestPricePerItem_objectNotCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
        .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setPricePerItem(new BigDecimal("10000"));
        });
    }
    @Test
    void createInventoryItem_withHighestTotalPrice_objectNotCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
        .build();
        assertThrows(ResponseStatusException.class, () -> {
            invItem.setTotalPrice(new BigDecimal ("1000001"));
        });
    }
    @Test
    void createInventoryItem_withNullManufacturedDate_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withManufactured(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withDateInPastForManufactured_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.minusDays(1);                    
        InventoryItem invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withManufactured(acceptDate.toString())
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
        .build();
        assertNotNull(invItem);
    }
    @Test
    void createInventoryItem_withDateInFutureForManufactured_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());
        assertThrows(ResponseStatusException.class, () -> {
            builder.withManufactured("2022-03-03")
            .build();
        });
    }
    @Test
    void createInventoryItem_withInvalidManufacturedFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());

        assertThrows(DateTimeParseException.class, () -> {
            builder.withManufactured("201x-09-09");
        });
    }
    @Test
    void createInventoryItem_withNullSellByDate_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withSellBy(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withDateInPastForSellBy_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());
        assertThrows(ResponseStatusException.class, () -> {
            builder.withSellBy("2000-03-03")
            .build();
        });
    }
    @Test
    void createInventoryItem_withDateInFutureForSellBy_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    
        InventoryItem builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withSellBy(acceptDate.toString())
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .build();
        assertNotNull(builder);
    }
    @Test
    void createInventoryItem_withInvalidSellByFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());

        assertThrows(DateTimeParseException.class, () -> {
            builder.withSellBy("20x-01-01");
        });
    }
    @Test
    void createInventoryItem_withNullBestBefore_objectCreated() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .withBestBefore(null)
                .build();
        inventoryItemRepository.save(invItem);
        InventoryItem testInvItem = inventoryItemRepository.findById(invItem.getId()).get();
        assertEquals(invItem, testInvItem);
    }
    @Test
    void createInventoryItem_withDateInPastForBestBefore_objectNotCreated() throws Exception {
        InventoryItem.Builder builder = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());
        assertThrows(ResponseStatusException.class, () -> {
            builder.withBestBefore("2000-03-03")
            .build();
        });
    }
    @Test
    void createInventoryItem_withDateInFutureForBestBefore_objectCreated() throws Exception {
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withBestBefore(acceptDate.toString())
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .build();
        
        assertNotNull(invItem);
    }
    @Test
    void createInventoryItem_withInvalidBestBeforeFormat_objectNotCreated() throws Exception {
        InventoryItem.Builder invItem = new InventoryItem.Builder()
        .withProduct(testProduct)
        .withQuantity(2)
        .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString());

        assertThrows(DateTimeParseException.class, () -> {
        invItem 
        .withBestBefore("2020-x-01");
        });
    }
    @Test
    void createInventoryItem_multipleInventoryItemsHaveSameProduct_objectCreated() throws Exception {
        InventoryItem invItem1 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(2)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .build();
        InventoryItem invItem2 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(100, ChronoUnit.DAYS).toString())
                .build();
        InventoryItem invItem3 = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(4)
                .withExpires(LocalDate.now().plus(150, ChronoUnit.DAYS).toString())
                .build();
        inventoryItemRepository.save(invItem1);
        inventoryItemRepository.save(invItem2);
        inventoryItemRepository.save(invItem3);
        InventoryItem testInvItem1 = inventoryItemRepository.findById(invItem1.getId()).get();
        InventoryItem testInvItem2 = inventoryItemRepository.findById(invItem2.getId()).get();
        InventoryItem testInvItem3 = inventoryItemRepository.findById(invItem3.getId()).get();
        assertEquals(invItem1, testInvItem1);
        assertEquals(invItem2, testInvItem2);
        assertEquals(invItem3, testInvItem3);
    }

    @Test
    void constructJSONObject_noNullAttributes_returnsExpectedJson() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withTotalPrice("5.32")
                .withManufactured("2021-03-11")
                .withSellBy(LocalDate.now().plus(10, ChronoUnit.DAYS).toString())
                .withBestBefore(LocalDate.now().plus(20, ChronoUnit.DAYS).toString())
                .withExpires(LocalDate.now().plus(30, ChronoUnit.DAYS).toString())
                .build();
        invItem = inventoryItemRepository.save(invItem);
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("id", invItem.getId());
        expectedJson.put("product", invItem.getProduct().constructJSONObject());
        expectedJson.put("quantity", invItem.getQuantity());
        expectedJson.put("remainingQuantity", invItem.getRemainingQuantity());
        expectedJson.put("pricePerItem", invItem.getPricePerItem());
        expectedJson.put("totalPrice", invItem.getTotalPrice());
        expectedJson.put("manufactured", invItem.getManufactured().toString());
        expectedJson.put("sellBy", invItem.getSellBy().toString());
        expectedJson.put("bestBefore", invItem.getBestBefore().toString());
        expectedJson.put("expires", invItem.getExpires().toString());
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedJson.toJSONString()), mapper.readTree(invItem.constructJSONObject().toJSONString()));
    }

    @Test
    void constructJSONObject_optionalAttributesNull_returnsExpectedJson() throws Exception {
        InventoryItem invItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .build();
        invItem = inventoryItemRepository.save(invItem);
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("id", invItem.getId());
        expectedJson.put("product", invItem.getProduct().constructJSONObject());
        expectedJson.put("quantity", invItem.getQuantity());
        expectedJson.put("remainingQuantity", invItem.getRemainingQuantity());
        expectedJson.put("expires", invItem.getExpires().toString());
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedJson.toJSONString()), mapper.readTree(invItem.constructJSONObject().toJSONString()));
    }

    @Test
    void saveInventoryItem_saveMultipleWithSameVersion_throwsException() throws Exception {
        InventoryItem originalItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .build();
        originalItem = inventoryItemRepository.save(originalItem);


        InventoryItem copy1 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();
        InventoryItem copy2 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();

        copy1.setQuantity(4);
        copy2.setQuantity(5);

        assertDoesNotThrow(() -> inventoryItemRepository.save(copy1));
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> inventoryItemRepository.save(copy2));
    }

    @Test
    void saveInventoryItem_saveMultipleWithDifferentVersion_savesCorrectly() throws Exception {
        InventoryItem originalItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withExpires(LocalDate.now().plus(50, ChronoUnit.DAYS).toString())
                .build();
        originalItem = inventoryItemRepository.save(originalItem);


        InventoryItem copy1 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();
        copy1.setQuantity(4);
        assertDoesNotThrow(() -> inventoryItemRepository.save(copy1));

        InventoryItem copy2 = inventoryItemRepository.findById(originalItem.getId()).orElseThrow();
        copy2.setQuantity(5);
        assertDoesNotThrow( () -> inventoryItemRepository.save(copy2));
    }
}
