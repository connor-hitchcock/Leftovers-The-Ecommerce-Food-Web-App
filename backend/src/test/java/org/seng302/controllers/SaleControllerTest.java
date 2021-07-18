package org.seng302.controllers;

import io.cucumber.java.sl.In;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.tomcat.jni.Local;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.seng302.entities.*;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.*;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class SaleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private SaleItemRepository saleItemRepository;
    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private Business business;
    @Mock
    private InventoryItem inventoryItem;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    private SaleController saleController;

    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        // Setup mock business
        when(business.getId()).thenReturn(1L);

        when(businessRepository.getBusinessById(1L)).thenReturn(business);
        when(businessRepository.getBusinessById(not(eq(1L)))).thenThrow(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE));

        // Setup mock inventory item
        when(inventoryItem.getId()).thenReturn(2L);
        when(inventoryItem.getExpires()).thenReturn(LocalDate.now());
        when(inventoryItem.getBusiness()).thenReturn(business);

        when(inventoryItemRepository.getInventoryItemByBusinessAndId(any(Business.class), anyLong())).thenCallRealMethod();
        when(inventoryItemRepository.findById(2L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.findById(not(eq(2L)))).thenReturn(Optional.empty());

        // Setup mock sale item repository
        when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(x -> x.getArgument(0));

        saleController = spy(new SaleController(businessRepository, saleItemRepository, inventoryItemRepository));
        mockMvc = MockMvcBuilders.standaloneSetup(saleController).build();
    }

    private JSONObject generateSalesItemInfo() {
        var object = new JSONObject();
        object.put("inventoryItemId", 2);
        object.put("quantity", 3);
        object.put("price", 10.5);
        object.put("moreInfo", "This is some more info about the product");
        object.put("closes", LocalDate.now().plus(100, ChronoUnit.DAYS).toString());
        return object;
    }

    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
    }

    @Test
    void addSaleItemToBusiness_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                    .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the POST request
        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateSalesItemInfo().toString()))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void addSaleItemToBusiness_validAuthToken_not401Response() throws Exception {
        mockMvc.perform(post("/businesses/1/listings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generateSalesItemInfo().toString()))
                    .andExpect(status().is(Matchers.not(401)))
                    .andReturn();
        // Checks that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void addSaleItemToBusiness_businessDoesNotExist_406Response() throws Exception {
        mockMvc.perform(post("/businesses/100/listings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generateSalesItemInfo().toString()))
                    .andExpect(status().isNotAcceptable())
                    .andReturn();
    }

    @Test
    void addSaleItemToBusiness_cannotActAsBusiness_403Response() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(business).checkSessionPermissions(any());

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateSalesItemInfo().toString()))
                .andExpect(status().isForbidden())
                .andReturn();

        verify(business).checkSessionPermissions(any());
    }

    @Test
    void addSaleItemToBusiness_noRequestBody_400Response() throws Exception {
        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemIdNotProvided_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.remove("inventoryItemId");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemIdNotNumber_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.put("inventoryItemId", "seven");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemNotFound_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.put("inventoryItemId", 100);

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_inventoryItemDifferentBusiness_400Response() throws Exception {
        var otherBusiness = mock(Business.class);
        when(otherBusiness.getId()).thenReturn(100L);

        when(inventoryItem.getBusiness()).thenReturn(otherBusiness);

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(generateSalesItemInfo().toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_quantityNotProvided_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.remove("quantity");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_quantityNotInteger_400Response() throws Exception {
        var object = generateSalesItemInfo();
        object.put("quantity", "seven");

        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void addSaleItemToBusiness_validInput_201Response() throws Exception {
        var object = generateSalesItemInfo();
        mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<SaleItem> captor = ArgumentCaptor.forClass(SaleItem.class);
        verify(saleItemRepository).save(captor.capture());
        SaleItem saleItem = captor.getValue();

        assertSame(inventoryItem, saleItem.getInventoryItem());
        assertEquals(object.get("quantity"), saleItem.getQuantity());
        assertEquals(new BigDecimal(object.getAsString("price")), saleItem.getPrice());
        assertEquals(object.get("moreInfo"), saleItem.getMoreInfo());
        assertEquals(object.getAsString("closes"), saleItem.getCloses().toString());
    }

    @Test
    void addSaleItemToBusiness_validInput_ReturnsId() throws Exception {
        SaleItem mockItem = mock(SaleItem.class);
        when(mockItem.getSaleId()).thenReturn(400L);
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(mockItem);

        var object = generateSalesItemInfo();
        MvcResult result = mockMvc.perform(post("/businesses/1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(object.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject actualResponse = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        JSONObject expectedResponse = new JSONObject();
        expectedResponse.put("listingId", 400);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getSaleItemsForBusiness_noAuthToken_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void getSaleItemsForBusiness_invalidBusiness_406Response() throws Exception {
        mockMvc.perform(get("/businesses/999/listings"))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

    @Test
    void getSaleItemsForBusiness_validBusiness_doesNotCheckSessionPermissions() throws Exception {
        mockMvc.perform(get("/businesses/1/listings"))
                .andReturn();

        verify(business, times(0)).checkSessionPermissions(any(HttpServletRequest.class));
    }

    @Test
    void getSaleItemsForBusiness_validBusinessNoSalesItem_returnsEmptyList() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        assertEquals(new JSONArray(), response);
    }

    @Test
    void getSaleItemsForBusiness_withSortOrder_usesSortOrder() throws Exception {
        mockMvc.perform(get("/businesses/1/listings")
                .param("orderBy", "someOrderBy"))
                .andReturn();

        verify(saleController).getSaleItemComparator("someOrderBy");
    }

    @Test
    void getSaleItemsForBusiness_noSortOrder_usesNullSortOrder() throws Exception {
        mockMvc.perform(get("/businesses/1/listings"))
                .andReturn();

        verify(saleController).getSaleItemComparator(null);
    }

    /**
     * Generates a consistently shuffled list of mock sale items
     * @return Mock sale item list
     */
    List<SaleItem> generateMockSaleItems() {
        List<SaleItem> mockItems = new ArrayList<>();
        for (long i = 0; i<6; i++) {
            SaleItem saleItem = mock(SaleItem.class);
            when(saleItem.getSaleId()).thenReturn(i);

            var json = new JSONObject();
            json.put("id", i);
            when(saleItem.constructJSONObject()).thenReturn(json);

            mockItems.add(saleItem);
        }
        // Ensure determinism
        Collections.shuffle(mockItems, new Random(7));
        return mockItems;
    }

    @Test
    void getSaleItemsForBusiness_noReverse_itemsAscending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAllForBusiness(any(Business.class))).thenReturn(items);
        when(saleController.getSaleItemComparator(nullable(String.class)))
                .thenReturn(Comparator.comparing(SaleItem::getSaleId));

        MvcResult result = mockMvc.perform(get("/businesses/1/listings"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();
        for (int i = 0; i<6; i++) {
            var object = new JSONObject();
            object.put("id", i);
            expected.add(object);
        }
        assertEquals(expected, response);
    }

    @Test
    void getSaleItemsForBusiness_reverseFalse_itemsAscending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAllForBusiness(any(Business.class))).thenReturn(items);
        when(saleController.getSaleItemComparator(nullable(String.class)))
                .thenReturn(Comparator.comparing(SaleItem::getSaleId));

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("reverse", "false"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();
        for (int i = 0; i<6; i++) {
            var object = new JSONObject();
            object.put("id", i);
            expected.add(object);
        }
        assertEquals(expected, response);
    }

    @Test
    void getSaleItemsForBusiness_reverseTrue_itemsDescending() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAllForBusiness(any(Business.class))).thenReturn(items);
        when(saleController.getSaleItemComparator(nullable(String.class)))
                .thenReturn(Comparator.comparing(SaleItem::getSaleId));

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("reverse", "true"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();
        for (int i = 0; i<6; i++) {
            var object = new JSONObject();
            object.put("id", 5 - i);
            expected.add(object);
        }
        assertEquals(expected, response);
    }

    @Test
    void getSaleItemsForBusiness_resultsPerPageSet_firstPageReturned() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAllForBusiness(any(Business.class))).thenReturn(items);
        when(saleController.getSaleItemComparator(nullable(String.class)))
                .thenReturn(Comparator.comparing(SaleItem::getSaleId));

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("resultsPerPage", "4"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();
        for (int i = 0; i<4; i++) {
            var object = new JSONObject();
            object.put("id", i);
            expected.add(object);
        }
        assertEquals(expected, response);
    }

    @Test
    void getSaleItemsForBusiness_secondPageRequested_secondPageReturned() throws Exception {
        var items = generateMockSaleItems();
        when(saleItemRepository.findAllForBusiness(any(Business.class))).thenReturn(items);
        when(saleController.getSaleItemComparator(nullable(String.class)))
                .thenReturn(Comparator.comparing(SaleItem::getSaleId));

        MvcResult result = mockMvc.perform(get("/businesses/1/listings")
                .param("resultsPerPage", "4")
                .param("page", "2"))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();
        for (int i = 4; i<6; i++) {
            var object = new JSONObject();
            object.put("id", i);
            expected.add(object);
        }
        assertEquals(expected, response);
    }

    @Test
    void getSaleItemComparator_orderByCreated_comparesCreatedCorrectly(){
        SaleItem saleItem1 = mock(SaleItem.class);
        SaleItem saleItem2 = mock(SaleItem.class);
        SaleItem saleItem3 = mock(SaleItem.class);

        Instant now = Instant.now();
        when(saleItem1.getCreated()).thenReturn(now);
        when(saleItem2.getCreated()).thenReturn(now);
        when(saleItem3.getCreated()).thenReturn(now.plusSeconds(1));

        var comparator = saleController.getSaleItemComparator("created");
        assertEquals(0, comparator.compare(saleItem1, saleItem2));
        assertTrue(comparator.compare(saleItem1, saleItem3) < 0);
    }

    @Test
    void getSaleItemComparator_orderByNull_comparesCreatedCorrectly(){
        SaleItem saleItem1 = mock(SaleItem.class);
        SaleItem saleItem2 = mock(SaleItem.class);
        SaleItem saleItem3 = mock(SaleItem.class);

        Instant now = Instant.now();
        when(saleItem1.getCreated()).thenReturn(now);
        when(saleItem2.getCreated()).thenReturn(now);
        when(saleItem3.getCreated()).thenReturn(now.plusSeconds(1));

        var comparator = saleController.getSaleItemComparator(null);
        assertEquals(0, comparator.compare(saleItem1, saleItem2));
        assertTrue(comparator.compare(saleItem1, saleItem3) < 0);
    }

    @Test
    void getSaleItemComparator_orderByCloses_comparesClosesCorrectly() {
        SaleItem saleItem1 = mock(SaleItem.class);
        SaleItem saleItem2 = mock(SaleItem.class);
        SaleItem saleItem3 = mock(SaleItem.class);

        LocalDate now = LocalDate.now();
        when(saleItem1.getCloses()).thenReturn(now);
        when(saleItem2.getCloses()).thenReturn(now);
        when(saleItem3.getCloses()).thenReturn(now.plusDays(1));

        var comparator = saleController.getSaleItemComparator("closing");
        assertEquals(0, comparator.compare(saleItem1, saleItem2));
        assertTrue(comparator.compare(saleItem1, saleItem3) < 0);
    }

    @Test
    void getSaleItemComparator_orderByProductCode_comparesProductCodeCorrectly() {
        SaleItem saleItem1 = mock(SaleItem.class);
        SaleItem saleItem2 = mock(SaleItem.class);
        SaleItem saleItem3 = mock(SaleItem.class);

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);
        Product product3 = mock(Product.class);

        when(saleItem1.getProduct()).thenReturn(product1);
        when(saleItem2.getProduct()).thenReturn(product2);
        when(saleItem3.getProduct()).thenReturn(product3);

        when(product1.getProductCode()).thenReturn("AAA");
        when(product2.getProductCode()).thenReturn("AAA");
        when(product3.getProductCode()).thenReturn("BBB");

        var comparator = saleController.getSaleItemComparator("productCode");
        assertEquals(0, comparator.compare(saleItem1, saleItem2));
        assertTrue(comparator.compare(saleItem1, saleItem3) < 0);
    }

    @Test
    void getSaleItemComparator_orderByProductName_comparesProductNameCorrectly() {
        SaleItem saleItem1 = mock(SaleItem.class);
        SaleItem saleItem2 = mock(SaleItem.class);
        SaleItem saleItem3 = mock(SaleItem.class);

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);
        Product product3 = mock(Product.class);

        when(saleItem1.getProduct()).thenReturn(product1);
        when(saleItem2.getProduct()).thenReturn(product2);
        when(saleItem3.getProduct()).thenReturn(product3);

        when(product1.getName()).thenReturn("AAA");
        when(product2.getName()).thenReturn("AAA");
        when(product3.getName()).thenReturn("BBB");

        var comparator = saleController.getSaleItemComparator("productName");
        assertEquals(0, comparator.compare(saleItem1, saleItem2));
        assertTrue(comparator.compare(saleItem1, saleItem3) < 0);
    }

    @Test
    void getSaleItemComparator_orderByQuantity_comparesQuantityCorrectly() {
        SaleItem saleItem1 = mock(SaleItem.class);
        SaleItem saleItem2 = mock(SaleItem.class);
        SaleItem saleItem3 = mock(SaleItem.class);

        when(saleItem1.getQuantity()).thenReturn(1);
        when(saleItem2.getQuantity()).thenReturn(1);
        when(saleItem3.getQuantity()).thenReturn(2);

        var comparator = saleController.getSaleItemComparator("quantity");
        assertEquals(0, comparator.compare(saleItem1, saleItem2));
        assertTrue(comparator.compare(saleItem1, saleItem3) < 0);
    }

    @Test
    void getSaleItemComparator_orderByPrice_comparesPriceCorrectly() throws Exception {
        SaleItem saleItem1 = mock(SaleItem.class);
        SaleItem saleItem2 = mock(SaleItem.class);
        SaleItem saleItem3 = mock(SaleItem.class);

        when(saleItem1.getPrice()).thenReturn(new BigDecimal("1.0"));
        when(saleItem2.getPrice()).thenReturn(new BigDecimal("1.0"));
        when(saleItem3.getPrice()).thenReturn(new BigDecimal("2.0"));

        var comparator = saleController.getSaleItemComparator("price");
        assertEquals(0, comparator.compare(saleItem1, saleItem2));
        assertTrue(comparator.compare(saleItem1, saleItem3) < 0);
    }

    @Test
    void getSaleItemComparator_orderByNonExistant_throws400Exception() {
        var exception = assertThrows(ResponseStatusException.class, () -> saleController.getSaleItemComparator("anything"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid sort order", exception.getReason());
    }

    @Test
    void getSalesItemForBusinessCount_noAuthentication_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/businesses/1/listings/count"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void getSalesItemForBusinessCount_invalidBusiness_406Response() throws Exception {
        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/businesses/999/listings/count"))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

    @Test
    void getSalesItemForBusinessCount_validBusiness_doesNotCheckSessionPermissions() throws Exception {
        mockMvc.perform(get("/businesses/1/listings/count"))
                .andReturn();

        verify(business, times(0)).checkSessionPermissions(any(HttpServletRequest.class));
    }

    @Test
    void getSalesItemForBusinessCount_validBusinessWithSalesItems_returnsSalesItemCount() throws Exception {
        @SuppressWarnings("unchecked")
        List<SaleItem> saleItems = (List<SaleItem>)mock(List.class);

        when(saleItemRepository.findAllForBusiness(any(Business.class))).thenReturn(saleItems);
        when(saleItems.size()).thenReturn(500);

        MvcResult result = mockMvc.perform(get("/businesses/1/listings/count"))
                .andReturn();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        var expected = new JSONObject();
        expected.put("count", 500);

        assertEquals(expected, response);
    }
}
