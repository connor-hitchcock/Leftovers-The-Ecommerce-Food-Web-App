package org.seng302.entities;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.*;
import org.seng302.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaleItemTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryItemRepository inventoryItemRepository;
    @Autowired
    SaleItemRepository saleItemRepository;

    Business testBusiness;
    Product testProduct;
    InventoryItem inventoryItem;


    void createTestObjects() throws Exception {
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

        LocalDate today = LocalDate.now();

        inventoryItem = new InventoryItem.Builder()
                .withProduct(testProduct)
                .withQuantity(3)
                .withPricePerItem("2.69")
                .withManufactured("2021-03-11")
                .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                .build();
        inventoryItemRepository.save(inventoryItem);
    }

    /**
     * Deletes all entries from the database
     */
    void clearDatabase() {
        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeAll
    void initialise() {
        clearDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        createTestObjects();
    }

    @AfterEach
    void tearDown() {
        clearDatabase();
    }

    @Test
    void createSaleItem_AllFieldsCorrect_ObjectCreated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    @Test
    void createSaleItem_OnlyCompulsaryFieldsFilled_ObjectCreated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withQuantity(2)
                .withPrice("200.34")
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    @Test
    void createSaleItem_ClosesSetToday_ObjectCreated() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(today)
                .withMoreInfo("This expires really soon")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    @Test
    void createSaleItem_ClosesSetYesterday_ObjectNotCreated() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = formatter.format(yesterday());
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(yesterday)
                .withMoreInfo("This has already closed")
                .withPrice("200.34")
                .withQuantity(2);

        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("You cannot set close dates in the past", exception.getReason());
    }

    @Test
    void createSaleItem_NoInventoryItem_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                    .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                    .withMoreInfo("This doesn't expire for a long time")
                    .withPrice("200.34")
                    .withQuantity(2);
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cannot sell something that is not in your inventory", exception.getReason());
    }

    @Test
    void createSaleItem_SalePriceNegative_ObjectNotCreated() throws Exception {
        inventoryItem.setPricePerItem(null);
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("-200.34")
                .withQuantity(2);
        assertThrows(ResponseStatusException.class, saleItem::build);
    }

    @Test
    void createSaleItem_SalePriceNull_ObjectNotCreated() throws Exception {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice(null)
                .withQuantity(2);
        assertThrows(ResponseStatusException.class, saleItem::build);
    }

    @Test
    void createSaleItem_SalePriceUnexpectedInput_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("three dollars")
                .withQuantity(2);

        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Please enter a valid number", exception.getReason());
    }

    @Test
    void createSaleItem_QuantityNull_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("3.57");

        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Quantity must be greater than 0", exception.getReason());
    }

    @Test
    void createSaleItem_QuantityZero_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(0)
                .withPrice("3.57");
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Quantity must be greater than 0", exception.getReason());
    }

    @Test
    void createSaleItem_QuantityGreaterThanInventoryTotal_ObjectNotCreated() {
        SaleItem.Builder builder = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(2000)
                .withPrice("3.57");
        var exception = assertThrows(ResponseStatusException.class, builder::build);
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cannot sell more items than you have", exception.getReason());
    }

    @Test
    void createMultipleSaleItems_QuantityAddsToInventoryTotal_ObjectsCreated() throws Exception {
        inventoryItem.setQuantity(10);
        inventoryItem.setRemainingQuantity(10);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem);
        SaleItem saleItem2 = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem2);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
        Assertions.assertNotNull(saleItemRepository.findById(saleItem2.getSaleId()));
    }

    @Test
    void createMultipleSaleItems_QuantityAddsToGreaterThanInventoryTotal_LastObjectNotCreated() throws Exception {
        inventoryItem.setQuantity(10);
        inventoryItem.setRemainingQuantity(10);
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(5)
                .withPrice("3.57")
                .build();
        saleItemRepository.save(saleItem);
        Assertions.assertNotNull(saleItemRepository.findById(saleItem.getSaleId()));
        SaleItem.Builder saleItem2 = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withQuantity(6)
                .withPrice("3.57");
        assertThrows(ResponseStatusException.class, saleItem2::build);
    }

    @Test
    void deleteInventoryItem_SaleItemDeleted() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);

        inventoryItemRepository.deleteAll();
        Optional<SaleItem> foundItem = saleItemRepository.findById(saleItem.getSaleId());
        if (foundItem.isPresent()) { Assertions.fail(); }
    }

    @Test
    void editSaleItem_QuantityStillWithinLimits_SaleItemAndInventoryItemQuantitiesUpdated() throws Exception {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This doesn't expire for a long time")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItemRepository.save(saleItem);
        saleItem.setQuantity(3);
        assertEquals(3, saleItem.getQuantity());
        assertEquals(0, inventoryItem.getRemainingQuantity());
    }

    @Test
    void editSaleItem_QuantityGreaterThanInventoryAvailable_NotUpdated() {
        try {
            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem)
                    .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                    .withMoreInfo("This doesn't expire for a long time")
                    .withQuantity(2)
                    .withPrice("3.57")
                    .build();
            saleItemRepository.save(saleItem);
            saleItem.setQuantity(5);
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Cannot sell more items than you have", e.getReason());
        } catch (Exception unexpected) { Assertions.fail(); }
    }

    @Test
    void createSaleItem_MoreInfoTooLong_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. This description is waaaaaay too long. ")
                .withQuantity(2);
        assertThrows(ResponseStatusException.class, saleItem::build);
    }

    @Test
    void createSaleItem_MoreInfoInvalid_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1000, ChronoUnit.DAYS).toString())
                .withMoreInfo("é树\n\t\uD83D\uDE02")
                .withQuantity(2);
        assertThrows(ResponseStatusException.class, saleItem::build);
    }

    @Test
    void createSaleItem_CloseDateInvalidFormat_ObjectNotCreated() {
        SaleItem.Builder saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses("In three seconds")
                .withMoreInfo("What's the time, Mr Wolfy?")
                .withQuantity(2);
        assertThrows(DateTimeParseException.class, saleItem::build);
    }

    @Test
    void constructJSONObject_hasAllProperties_expectPropertiesPresent() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(today)
                .withMoreInfo("This expires really soon")
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        JSONObject object = saleItem.constructJSONObject();

        assertEquals(saleItem.getSaleId(), object.get("id"));
        assertEquals(saleItem.getInventoryItem().constructJSONObject(), object.get("inventoryItem"));
        assertEquals(saleItem.getQuantity(), object.get("quantity"));
        assertEquals(saleItem.getPrice(), object.get("price"));
        assertEquals(saleItem.getMoreInfo(), object.get("moreInfo"));
        assertEquals(saleItem.getCreated().toString(), object.get("created"));
        assertEquals(saleItem.getCloses().toString(), object.get("closes"));
        assertEquals(7, object.size()); // No extra properties
    }

    @Test
    void constructJSONObject_hasSomeProperties_expectRequiredPropertiesPresent() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(today)
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        JSONObject object = saleItem.constructJSONObject();
        assertFalse(object.containsKey("moreInfo"));
        assertEquals(6, object.size());
    }

    @Test
    void findAllForBusiness_saleItemExistsForBusiness_saleItemIsFound() {
        SaleItem saleItem = new SaleItem.Builder()
                .withInventoryItem(inventoryItem)
                .withCloses(LocalDate.now().plus(1, ChronoUnit.DAYS).toString())
                .withPrice("200.34")
                .withQuantity(2)
                .build();
        saleItem = saleItemRepository.save(saleItem);

        List<SaleItem> foundItems = saleItemRepository.findAllForBusiness(testBusiness);

        assertEquals(1, foundItems.size());
        SaleItem foundItem = foundItems.get(0);

        assertEquals(saleItem.getSaleId(), foundItem.getSaleId());
        assertEquals(saleItem.getInventoryItem().getId(), foundItem.getInventoryItem().getId());
        assertEquals(saleItem.getCloses(), foundItem.getCloses());
        assertEquals(saleItem.getQuantity(), foundItem.getQuantity());
    }

    @Test
    void findAllForBusiness_multipleInventoryItems_allSaleItemsAreFoundNoDuplicates() throws Exception {
        LocalDate today = LocalDate.now();

        // Creates many sale items associated with different inventory items
        Map<Long, SaleItem> saleItems = new HashMap<>();
        for (int i = 0; i<3; i++) {
            var inventoryItem = new InventoryItem.Builder()
                    .withProduct(testProduct)
                    .withQuantity(30)
                    .withPricePerItem("2.69")
                    .withManufactured("2021-03-11")
                    .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                    .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                    .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                    .build();
            inventoryItem = inventoryItemRepository.save(inventoryItem);

            for (int j = 0; j<4; j++) {
                var saleItem = new SaleItem.Builder()
                        .withInventoryItem(inventoryItem)
                        .withQuantity(1)
                        .withPrice("10.00")
                        .withMoreInfo("more_info_" + i + "_" + j)
                        .build();
                saleItem = saleItemRepository.save(saleItem);
                saleItems.put(saleItem.getSaleId(), saleItem);
            }
        }

        // Make sure that the found items correspond 1 to 1 with the generated items
        List<SaleItem> foundItems = saleItemRepository.findAllForBusiness(testBusiness);
        for (SaleItem foundItem : foundItems) {
            SaleItem matchingItem = saleItems.get(foundItem.getSaleId());
            assertNotNull(matchingItem);
            assertEquals(matchingItem.getMoreInfo(), foundItem.getMoreInfo());
        }
        assertEquals(saleItems.size(), foundItems.size());
    }

    @Test
    void findAllForBusiness_multipleProducts_allSaleItemsAreFoundNoDuplicates() throws Exception {
        LocalDate today = LocalDate.now();

        // Creates many sale items associated with different products
        Map<Long, SaleItem> saleItems = new HashMap<>();
        for (int i = 0; i<3; i++) {
            var product = new Product.Builder()
                    .withBusiness(testBusiness)
                    .withProductCode("TEST-" + i)
                    .withName("test_product")
                    .build();
            product = productRepository.save(product);
            var inventoryItem = new InventoryItem.Builder()
                    .withProduct(product)
                    .withQuantity(30)
                    .withPricePerItem("2.69")
                    .withManufactured("2021-03-11")
                    .withSellBy(today.plus(2, ChronoUnit.DAYS).toString())
                    .withBestBefore(today.plus(3, ChronoUnit.DAYS).toString())
                    .withExpires(today.plus(4, ChronoUnit.DAYS).toString())
                    .build();
            inventoryItem = inventoryItemRepository.save(inventoryItem);

            for (int j = 0; j<4; j++) {
                var saleItem = new SaleItem.Builder()
                        .withInventoryItem(inventoryItem)
                        .withQuantity(1)
                        .withPrice("10.00")
                        .withMoreInfo("more_info_" + i + "_" + j)
                        .build();
                saleItem = saleItemRepository.save(saleItem);
                saleItems.put(saleItem.getSaleId(), saleItem);
            }
        }

        // Make sure that the found items correspond 1 to 1 with the generated items
        List<SaleItem> foundItems = saleItemRepository.findAllForBusiness(testBusiness);
        for (SaleItem foundItem : foundItems) {
            SaleItem matchingItem = saleItems.get(foundItem.getSaleId());
            assertNotNull(matchingItem);
            assertEquals(matchingItem.getMoreInfo(), foundItem.getMoreInfo());
        }
        assertEquals(saleItems.size(), foundItems.size());
    }
}
