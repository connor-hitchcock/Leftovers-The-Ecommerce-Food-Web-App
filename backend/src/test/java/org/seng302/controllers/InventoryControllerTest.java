package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.entities.*;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import aj.org.objectweb.asm.Type;

import org.springframework.test.web.servlet.MvcResult;
import net.minidev.json.parser.JSONParser;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private InventoryController inventoryController;
    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();

    @Mock
    private HttpServletRequest request;
    @MockBean
    private BusinessRepository businessRepository;
    @MockBean
    private InventoryItemRepository inventoryItemRepository;
    @MockBean
    private ProductRepository productRepository;

    private User testUser;
    @Mock
    private Business testBusiness;
    @Mock
    private Business mockBusiness;
    @Mock
    private List<Product> mockProductList;
    @Mock
    private HttpSession session;
    private Product testProduct;
    private Product testProduct2;
    private Product testProduct3;
    private Product testProductNull;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        testUser = new User.Builder()
                .withFirstName("Andy")
                .withMiddleName("Percy")
                .withLastName("Elliot")
                .withNickName("Ando")
                .withEmail("123andyelliot@gmail.com")
                .withPassword("password123")
                .withDob("1987-04-12")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .build();
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withDescription("DESCRIPTION")
                .withName("BUSINESS_NAME")
                .withAddress(Location.covertAddressStringToLocation("108,Albert Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withPrimaryOwner(testUser)
                .build();
        testProduct = new Product.Builder()
                .withProductCode("BEANS")
                .withBusiness(testBusiness)
                .withDescription("some description")
                .withManufacturer("manufacturer")
                .withName("some Name")
                .withRecommendedRetailPrice("15")
                .build();
        testProduct2 = new Product.Builder()
                .withProductCode("HAM")
                .withBusiness(testBusiness)
                .withDescription("some description 2")
                .withManufacturer("manufacturer 2")
                .withName("some Name 2")
                .withRecommendedRetailPrice("16")
                .build();
        testProduct3 = new Product.Builder()
                .withProductCode("VEGE")
                .withBusiness(testBusiness)
                .withDescription("another description")
                .withManufacturer("another manufacturer")
                .withName("another Name")
                .withRecommendedRetailPrice("17")
                .build();
        // this product will only have the bare minimum details to test null values for
        // sorting(some sort options are optional fields)
        testProductNull = new Product.Builder()
                .withProductCode("ZZZ")
                .withBusiness(testBusiness)
                .withName("zzz")
                .build();

        List<InventoryItem> inventory = new ArrayList<>();
        addSeveralInventoryItemsToAnInventory(inventory);
        Business businessSpy = spy(testBusiness);
        when(businessSpy.getId()).thenReturn(1L);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication
        when(productRepository.findAllByBusiness(any())).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(any())).thenReturn(inventory);

        var controller = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Generates a valid inventory item creation request body
     * @return JSONObject for a valid creation request
     */
    private JSONObject generateInventoryCreateInfo() {
        var inventory = new JSONObject();
        inventory.put("productId", "BEANS");
        inventory.put("quantity", 4);
        inventory.put("pricePerItem", 6.5);
        inventory.put("totalPrice", 21.99);
        inventory.put("manufactured", "2021-05-12");
        inventory.put("sellBy",     LocalDate.now().plus(10, ChronoUnit.DAYS).toString());
        inventory.put("bestBefore", LocalDate.now().plus(20, ChronoUnit.DAYS).toString());
        inventory.put("expires",    LocalDate.now().plus(30, ChronoUnit.DAYS).toString());
        return inventory;
    }

    @Test
    void addInventory_validPermission_canAddInventory() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isOk());
    }


    @Test
    void addInventory_normalUser_cannotAddInventory403() throws Exception {
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager
                    .when(() -> AuthenticationTokenManager
                            .checkAuthenticationToken(any()))
                    .then(invocation -> null); // mock a valid session
            Business businessSpy = spy(testBusiness);
            when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);
            sessionAuthToken.put("accountId", 1L); // use a random account

            mockMvc.perform(MockMvcRequestBuilders
                    .post("/businesses/1/inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generateInventoryCreateInfo().toString())
                    .sessionAttrs(sessionAuthToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void addInventory_notLoggedIn_cannotAddInventory401() throws Exception {
        Business businessSpy = spy(testBusiness);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addInventory_invalidBusinessId_406Thrown() throws Exception {
        when(businessRepository.getBusinessById(any())).thenCallRealMethod();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/999/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_noRequestBody_400Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication


        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addInventory_productNotExist_406Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenCallRealMethod(); // use real method
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void addInventory_productOfWrongBusiness_403Thrown() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        Optional<Product> optionalProduct = Optional.of(productSpy);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)); // make this method always throw
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isForbidden());
    }


    @Test
    void addInventory_inventoryCreated_inventorySavedToDatabase() throws Exception {
        Business businessSpy = spy(testBusiness);
        Product productSpy = spy(testProduct);
        when(businessRepository.getBusinessById(any())).thenReturn(businessSpy); // use our business
        when(productRepository.getProductByBusinessAndProductCode(any(), any())).thenReturn(productSpy); // use our product
        doNothing().when(businessSpy).checkSessionPermissions(any()); // mock successful authentication

        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateInventoryCreateInfo().toString()))
                .andExpect(status().isOk());

        verify(inventoryItemRepository, times(1)).save(any(InventoryItem.class));

    }

    @Test
    void getInventory_unverifiedAccessToken_401Thrown() throws Exception {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new AccessTokenException()).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventory(1L, request, null, null, null, null));
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getInventoryCount_unverifiedAccessToken_401Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new AccessTokenException()).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventoryCount(1L, request));
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getInventory_insufficientPermissions_403Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventory(1L, request, null, null, null, null));
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getInventoryCount_insufficientPermissions_403Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(mockBusiness).checkSessionPermissions(any());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventoryCount(1L, request));
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getInventory_businessNotFound_406Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventory(1L, request, null, null, null, null));
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatus());
    }

    @Test
    void getInventoryCount_businessNotFound_406Thrown() {
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> inventoryController.getInventoryCount(1L, request));
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatus());
    }

    @Test
    void getInventory_emptyInventory_emptyArrayReturned() {
        List<InventoryItem> emptyInventory = new ArrayList<>();
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(emptyInventory);
        JSONArray result = inventoryController.getInventory(1L, request, null, null, null, null);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void getInventoryCount_emptyInventory_zeroReturned() {
        List<InventoryItem> emptyInventory = new ArrayList<>();
        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(emptyInventory);
        JSONObject result = inventoryController.getInventoryCount(1L, request);
        Assertions.assertTrue(result.containsKey("count"));
        Assertions.assertEquals(0, result.getAsNumber("count"));
    }

    @Test
    void getInventory_multipleItems_correctArrayReturned() throws Exception {
        String futureDate = LocalDate.now().plus(50, ChronoUnit.DAYS).toString();
        List<InventoryItem> inventory = new ArrayList<>();
        JSONArray expectedResponse = new JSONArray();
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires(futureDate).build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(39).withExpires(futureDate).build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(54).withExpires(futureDate).build());
        for (InventoryItem item : inventory) {
            expectedResponse.appendElement(item.constructJSONObject());
        }

        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(inventory);
        JSONArray result = inventoryController.getInventory(1L, request, null, null, null, null);
        Assertions.assertEquals(expectedResponse, result);
    }

    @Test
    void getInventoryCount_multipleItems_correctCountReturned() throws Exception {
        String futureDate = LocalDate.now().plus(50, ChronoUnit.DAYS).toString();

        List<InventoryItem> inventory = new ArrayList<>();
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires(futureDate).build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(39).withExpires(futureDate).build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(54).withExpires(futureDate).build());

        inventoryController = new InventoryController(businessRepository, inventoryItemRepository, productRepository);
        when(businessRepository.getBusinessById(1L)).thenReturn(mockBusiness);
        when(productRepository.findAllByBusiness(mockBusiness)).thenReturn(mockProductList);
        when(inventoryItemRepository.getInventoryByCatalogue(mockProductList)).thenReturn(inventory);

        JSONObject result = inventoryController.getInventoryCount(1L, request);
        Assertions.assertTrue(result.containsKey("count"));
        Assertions.assertEquals(3, result.getAsNumber("count"));
    }

    @Test
    void retrievePaginatedInventory_firstPage_firstPageOfInventoryItems() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/businesses/1/inventory").param("page", "1").param("resultsPerPage", "2"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        // Check length should be 2 products
        assertEquals(2, responseBody.size());

        // Check the two products are the expected ones
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);

        // Because the default sorting option is by Product code, the two product codes
        // we have are "BEANS"
        // "HAM" and "VEGE" respectively, so the order should still stay the same as per
        // how they were added
        // in addSeveralInventoryItemsToAnInventory()
        // using the differing quantities of the inventory items to identify that the
        // pagination
        // works as intended
        assertEquals("1", firstInventory.getAsString("quantity"));
        assertEquals("2", secondInventory.getAsString("quantity"));
    }

    @Test
    void retrievePaginatedInventory_secondPage_secondPageOfInventoryItems() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/businesses/1/inventory").param("page", "2").param("resultsPerPage", "2"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        // Check length should be 2 products
        assertEquals(2, responseBody.size());

        // Check the two products are the expected ones
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);

        // Because the default sorting option is by Product code, the two product codes
        // we have are "BEANS"
        // "HAM" and "VEGE" respectively, so the order should still stay the same as per
        // how they were added
        // in addSeveralInventoryItemsToAnInventory()
        // using the differing quantities of the inventory items to identify that the
        // pagination
        // works as intended
        assertEquals("3", firstInventory.getAsString("quantity"));
        assertEquals("4", secondInventory.getAsString("quantity"));
    }

    @Test
    void retrieveSortedInventory_byName_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "name"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);

        // the ordering is as such because the names are "another name", "some Name" and
        // "some Name 2"
        assertEquals(testProduct3.getName(), ((JSONObject) firstInventory.get("product")).getAsString("name"));
        assertEquals(testProduct.getName(), ((JSONObject) secondInventory.get("product")).getAsString("name"));
        assertEquals(testProduct2.getName(), ((JSONObject) thirdInventory.get("product")).getAsString("name"));
    }

    @Test
    void retrieveSortedInventory_byNameReverse_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/businesses/1/inventory").param("reverse", "true").param("orderBy", "name"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // the ordering is as such because the names are "some Name 2", "some Name" and
        // "another name"
        assertEquals(testProductNull.getName(), ((JSONObject) firstInventory.get("product")).getAsString("name"));
        assertEquals(testProduct2.getName(), ((JSONObject) secondInventory.get("product")).getAsString("name"));
        assertEquals(testProduct.getName(), ((JSONObject) thirdInventory.get("product")).getAsString("name"));
        assertEquals(testProduct3.getName(), ((JSONObject) fourthInventory.get("product")).getAsString("name"));
    }

    @Test
    void retrieveSortedInventory_byDescription_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "description"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // the ordering is as such because the names are "another description", "some
        // description" and "some description 2"
        assertEquals(testProduct3.getDescription(),
                ((JSONObject) firstInventory.get("product")).getAsString("description"));
        assertEquals(testProduct.getDescription(),
                ((JSONObject) secondInventory.get("product")).getAsString("description"));
        assertEquals(testProduct2.getDescription(),
                ((JSONObject) thirdInventory.get("product")).getAsString("description"));
        assertEquals(testProductNull.getDescription(),
                ((JSONObject) fourthInventory.get("product")).getAsString("description"));
    }

    @Test
    void retrieveSortedInventory_byDescriptionReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "description")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // the ordering is as such because the names are "some description", "some
        // description 2" and "another description"
        assertEquals(testProductNull.getDescription(),
                ((JSONObject) firstInventory.get("product")).getAsString("description"));
        assertEquals(testProduct2.getDescription(),
                ((JSONObject) secondInventory.get("product")).getAsString("description"));
        assertEquals(testProduct.getDescription(),
                ((JSONObject) thirdInventory.get("product")).getAsString("description"));
        assertEquals(testProduct3.getDescription(),
                ((JSONObject) fourthInventory.get("product")).getAsString("description"));
    }

    @Test
    void retrieveSortedInventory_byManufacturer_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "manufacturer"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // same idea as the above tests
        assertEquals(testProduct3.getManufacturer(),
                ((JSONObject) firstInventory.get("product")).getAsString("manufacturer"));
        assertEquals(testProduct.getManufacturer(),
                ((JSONObject) secondInventory.get("product")).getAsString("manufacturer"));
        assertEquals(testProduct2.getManufacturer(),
                ((JSONObject) thirdInventory.get("product")).getAsString("manufacturer"));
        assertEquals(testProductNull.getManufacturer(),
                ((JSONObject) fourthInventory.get("product")).getAsString("manufacturer"));
    }

    @Test
    void retrieveSortedInventory_byManufacturerReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "manufacturer")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals(testProductNull.getManufacturer(),
                ((JSONObject) firstInventory.get("product")).getAsString("manufacturer"));
        assertEquals(testProduct2.getManufacturer(),
                ((JSONObject) secondInventory.get("product")).getAsString("manufacturer"));
        assertEquals(testProduct.getManufacturer(),
                ((JSONObject) thirdInventory.get("product")).getAsString("manufacturer"));
        assertEquals(testProduct3.getManufacturer(),
                ((JSONObject) fourthInventory.get("product")).getAsString("manufacturer"));
    }

    @Test
    void retrieveSortedInventory_byRecommendedRetailPrice_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "recommendedRetailPrice"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // same idea as the above tests
        assertEquals(testProduct.getRecommendedRetailPrice().toString(),
                ((JSONObject) firstInventory.get("product")).getAsString("recommendedRetailPrice"));
        assertEquals(testProduct2.getRecommendedRetailPrice().toString(),
                ((JSONObject) secondInventory.get("product")).getAsString("recommendedRetailPrice"));
        assertEquals(testProduct3.getRecommendedRetailPrice().toString(),
                ((JSONObject) thirdInventory.get("product")).getAsString("recommendedRetailPrice"));
        // Cannot toString a null value, so just compare with null instead
        assertEquals(null, ((JSONObject) fourthInventory.get("product")).getAsString("recommendedRetailPrice"));
    }

    @Test
    void retrieveSortedInventory_byRecommendedRetailPriceReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "recommendedRetailPrice")).andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        // Cannot toString a null value, so just compare with null instead
        assertEquals(null, ((JSONObject) firstInventory.get("product")).getAsString("recommendedRetailPrice"));
        assertEquals(testProduct3.getRecommendedRetailPrice().toString(),
                ((JSONObject) secondInventory.get("product")).getAsString("recommendedRetailPrice"));
        assertEquals(testProduct2.getRecommendedRetailPrice().toString(),
                ((JSONObject) thirdInventory.get("product")).getAsString("recommendedRetailPrice"));
        assertEquals(testProduct.getRecommendedRetailPrice().toString(),
                ((JSONObject) fourthInventory.get("product")).getAsString("recommendedRetailPrice"));
    }

    @Test
    void retrieveSortedInventory_byCreated_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "created"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);

        // same idea as the above tests
        assertEquals(testProduct.getCreated().toString(), ((JSONObject) firstInventory.get("product")).get("created"));
        assertEquals(testProduct2.getCreated().toString(), ((JSONObject) secondInventory.get("product")).get("created"));
        assertEquals(testProduct3.getCreated().toString(), ((JSONObject) thirdInventory.get("product")).get("created"));
    }

    @Test
    void retrieveSortedInventory_byCreatedReverse_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "created")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals(testProductNull.getCreated().toString(), ((JSONObject) firstInventory.get("product")).get("created"));
        assertEquals(testProduct3.getCreated().toString(), ((JSONObject) secondInventory.get("product")).get("created"));
        assertEquals(testProduct2.getCreated().toString(), ((JSONObject) thirdInventory.get("product")).get("created"));
        assertEquals(testProduct.getCreated().toString(), ((JSONObject) fourthInventory.get("product")).get("created"));
    }

    @Test
    void retrieveSortedInventory_byQuantity_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "quantity"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);

        // same idea as the above tests
        assertEquals("1", firstInventory.getAsString("quantity"));
        assertEquals("3", secondInventory.getAsString("quantity"));
        assertEquals("5", thirdInventory.getAsString("quantity"));
    }

    @Test
    void retrieveSortedInventory_byQuantityReverse_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "quantity")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals("7", firstInventory.getAsString("quantity"));
        assertEquals("6", secondInventory.getAsString("quantity"));
        assertEquals("4", thirdInventory.getAsString("quantity"));
        assertEquals("2", fourthInventory.getAsString("quantity"));
    }

    @Test
    void retrieveSortedInventory_byPricePerItem_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "pricePerItem"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // same idea as the above tests
        assertEquals("1", firstInventory.getAsString("pricePerItem"));
        assertEquals("3", secondInventory.getAsString("pricePerItem"));
        assertEquals("5", thirdInventory.getAsString("pricePerItem"));
        assertEquals(null, fourthInventory.getAsString("pricePerItem"));
    }

    @Test
    void retrieveSortedInventory_byPricePerItemReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "pricePerItem")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals(null, firstInventory.getAsString("pricePerItem"));
        assertEquals("6", secondInventory.getAsString("pricePerItem"));
        assertEquals("4", thirdInventory.getAsString("pricePerItem"));
        assertEquals("2", fourthInventory.getAsString("pricePerItem"));
    }

    @Test
    void retrieveSortedInventory_byTotalPrice_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "totalPrice"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // same idea as the above tests
        assertEquals("1", firstInventory.getAsString("totalPrice"));
        assertEquals("3", secondInventory.getAsString("totalPrice"));
        assertEquals("5", thirdInventory.getAsString("totalPrice"));
        assertEquals(null, fourthInventory.getAsString("totalPrice"));
    }

    @Test
    void retrieveSortedInventory_byTotalPriceReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "totalPrice")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals(null, firstInventory.getAsString("totalPrice"));
        assertEquals("6", secondInventory.getAsString("totalPrice"));
        assertEquals("4", thirdInventory.getAsString("totalPrice"));
        assertEquals("2", fourthInventory.getAsString("totalPrice"));
    }

    @Test
    void retrieveSortedInventory_byManufactured_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "manufactured"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // same idea as the above tests

        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2020-01-01", firstInventory.get("manufactured").toString());
        assertEquals("2020-03-01", secondInventory.get("manufactured").toString());
        assertEquals("2020-06-06", thirdInventory.get("manufactured").toString());
        assertEquals(null, fourthInventory.get("manufactured"));
    }

    @Test
    void retrieveSortedInventory_byManufacturedReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "manufactured")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals(null, firstInventory.get("manufactured"));
        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2020-06-06", secondInventory.get("manufactured").toString());
        assertEquals("2020-03-01", thirdInventory.get("manufactured").toString());
        assertEquals("2020-01-01", fourthInventory.get("manufactured").toString());
    }

    @Test
    void retrieveSortedInventory_bySellBy_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "sellBy"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2026-02-01", firstInventory.get("sellBy").toString());
        assertEquals("2027-02-01", secondInventory.get("sellBy").toString());
        assertEquals("2028-02-01", thirdInventory.get("sellBy").toString());
        assertEquals(null, fourthInventory.get("sellBy"));
    }

    @Test
    void retrieveSortedInventory_bySellByReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "sellBy")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals(null, firstInventory.get("sellBy"));
        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2028-02-01", secondInventory.get("sellBy").toString());
        assertEquals("2027-02-01", thirdInventory.get("sellBy").toString());
        assertEquals("2026-02-01", fourthInventory.get("sellBy").toString());
    }

    @Test
    void retrieveSortedInventory_byBestBefore_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "bestBefore"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2027-03-01", firstInventory.get("bestBefore").toString());
        assertEquals("2028-03-01", secondInventory.get("bestBefore").toString());
        assertEquals("2029-02-01", thirdInventory.get("bestBefore").toString());
        assertEquals(null, fourthInventory.get("bestBefore"));
    }

    @Test
    void retrieveSortedInventory_byBestBeforeReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "bestBefore")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals(null, firstInventory.get("bestBefore"));
        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2029-02-01", secondInventory.get("bestBefore").toString());
        assertEquals("2028-03-01", thirdInventory.get("bestBefore").toString());
        assertEquals("2027-03-01", fourthInventory.get("bestBefore").toString());
    }

    @Test
    void retrieveSortedInventory_byExpiry_correctOrderOfInventoryWithNullBottom() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("orderBy", "expiry"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);
        JSONObject fourthInventory = (JSONObject) responseBody.get(6);

        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2028-01-01", firstInventory.get("expires").toString());
        assertEquals("2029-01-01", secondInventory.get("expires").toString());
        assertEquals("2030-06-06", thirdInventory.get("expires").toString());
        assertEquals("2031-06-06", fourthInventory.get("expires").toString());
    }

    @Test
    void retrieveSortedInventory_byExpiryReverse_correctOrderOfInventoryWithNullTop() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory")
                .param("reverse", "true").param("orderBy", "expires")).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        // same idea as the above tests
        assertEquals("2031-06-06", firstInventory.get("expires").toString());
        // the dates below correspond to the dates created in
        // addSeveralInventoryItemsToAnInventory(),
        // just that it is in the JSON format from the database
        assertEquals("2030-06-06", secondInventory.get("expires").toString());
        assertEquals("2029-01-01", thirdInventory.get("expires").toString());
        assertEquals("2028-01-01", fourthInventory.get("expires").toString());
    }

    @Test
    void retrieveSortedInventory_byDefaultProductCode_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/businesses/1/inventory"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(2);
        JSONObject thirdInventory = (JSONObject) responseBody.get(4);

        assertEquals(testProduct.getProductCode(),
                ((JSONObject) firstInventory.get("product")).getAsString("id"));
        assertEquals(testProduct2.getProductCode(),
                ((JSONObject) secondInventory.get("product")).getAsString("id"));
        assertEquals(testProduct3.getProductCode(),
                ((JSONObject) thirdInventory.get("product")).getAsString("id"));
    }

    @Test
    void retrieveSortedInventory_byDefaultProductCodeReverse_correctOrderOfInventory() throws Exception {
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get("/businesses/1/inventory").param("reverse", "true"))
                .andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONArray responseBody = (JSONArray) parser.parse(result.getResponse().getContentAsString());

        assertEquals(7, responseBody.size());

        // there are different types of inventory items, so test the index positions of
        // the those types
        JSONObject firstInventory = (JSONObject) responseBody.get(0);
        JSONObject secondInventory = (JSONObject) responseBody.get(1);
        JSONObject thirdInventory = (JSONObject) responseBody.get(3);
        JSONObject fourthInventory = (JSONObject) responseBody.get(5);

        assertEquals(testProductNull.getProductCode(),
                ((JSONObject) firstInventory.get("product")).getAsString("id"));
        assertEquals(testProduct3.getProductCode(),
                ((JSONObject) secondInventory.get("product")).getAsString("id"));
        assertEquals(testProduct2.getProductCode(),
                ((JSONObject) thirdInventory.get("product")).getAsString("id"));
        assertEquals(testProduct.getProductCode(),
                ((JSONObject) fourthInventory.get("product")).getAsString("id"));
    }

    /**
     * Creates several inventory items based on a product. These items have
     * differing attributes to identify them.
     * 
     * @throws Exception
     */
    public void addSeveralInventoryItemsToAnInventory(List<InventoryItem> inventory) throws Exception {
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(1).withExpires("2028-01-01")
                .withPricePerItem("1").withTotalPrice("1").withManufactured("2020-01-01").withSellBy("2026-02-01")
                .withBestBefore("2027-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct).withQuantity(2).withExpires("2028-01-01")
                .withPricePerItem("2").withTotalPrice("2").withManufactured("2020-01-01").withSellBy("2026-02-01")
                .withBestBefore("2027-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct2).withQuantity(3).withExpires("2029-01-01")
                .withPricePerItem("3").withTotalPrice("3").withManufactured("2020-03-01").withSellBy("2027-02-01")
                .withBestBefore("2028-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct2).withQuantity(4).withExpires("2029-01-01")
                .withPricePerItem("4").withTotalPrice("4").withManufactured("2020-03-01").withSellBy("2027-02-01")
                .withBestBefore("2028-03-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct3).withQuantity(5).withExpires("2030-06-06")
                .withPricePerItem("5").withTotalPrice("5").withManufactured("2020-06-06").withSellBy("2028-02-01")
                .withBestBefore("2029-02-01").build());
        inventory.add(new InventoryItem.Builder().withProduct(testProduct3).withQuantity(6).withExpires("2030-06-06")
                .withPricePerItem("6").withTotalPrice("6").withManufactured("2020-06-06").withSellBy("2028-02-01")
                .withBestBefore("2029-02-01").build());
        // inventory item with the bare minimum to exist as an inventory item
        inventory.add(new InventoryItem.Builder().withProduct(testProductNull).withQuantity(7).withExpires("2031-06-06")
                .build());
    }
}
