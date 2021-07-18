package org.seng302.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.seng302.entities.*;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.*;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private DemoController demoController;
    @Mock
    private HttpServletRequest request;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private SaleItemRepository saleItemRepository;
    @Mock
    private ImageRepository imageRepository;

    private User testUser;
    private Business testBusiness;

    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);
        demoController = new DemoController(userRepository, businessRepository, productRepository, inventoryItemRepository, saleItemRepository, imageRepository);
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
    }

    /**
     * Test that when a PUT request is made to the "/demo/load" endpoint, and the request has no authentication token,
     * a ResponseStatusException with status code 401 is thrown.
     */
    @Test
    void loadDemoData_noAuthToken_401ResponseTest() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager. when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenException());

            // Verify that a 401 response is received in response to the PUT request
            mockMvc.perform(put("/demo/load"))
                    .andExpect(status().isUnauthorized())
                    .andReturn();
        }
    }

    /**
     * Test that when a PUT request is made to the "/demo/load" endpoint, and the request has no authentication token,
     * no entities will be saved to the database.
     */
    @Test
    void loadDemoData_noAuthToken_dataNotLoadedTest() {
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager. when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).thenThrow(new AccessTokenException());

            assertThrows(ResponseStatusException.class, () -> demoController.loadDemoData(request));
        }
        verify(userRepository, times(0)).save(any(User.class));
        verify(businessRepository, times(0)).save(any(Business.class));
        verify(productRepository, times(0)).save(any(Product.class));
        verify(inventoryItemRepository, times(0)).save(any(InventoryItem.class));
        verify(saleItemRepository, times(0)).save(any(SaleItem.class));
    }

    /**
     * Test that when a PUT request is made to the "/demo/load" endpoint, and the request has an authentication token
     * which is associated with an account with role 'user', a ResponseStatusException with status code 403 is thrown.
     */
    @Test
    void loadDemoData_userAuthToken_403FailTest() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the session role is 'user'
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager. when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).then(invocation -> null);
            authenticationTokenManager. when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(false);

            // Verify that a 403 response is received in response to the PUT request
            mockMvc.perform(put("/demo/load"))
                    .andExpect(status().isForbidden())
                    .andReturn();
        }
        // Verify that entities were not loaded to the database
    }

    /**
     * Test that when a PUT request is made to the "/demo/load" endpoint, and the request has an authentication token
     * which is associated with an account with role 'user', no entities are saved to the database.
     */
    @Test
    void loadDemoData_userAuthToken_dataNotLoadedTest() {
        // Mock the AuthenticationTokenManager to respond as it would when the session role is 'user'
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager. when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).then(invocation -> null);
            authenticationTokenManager. when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(false);

            assertThrows(ResponseStatusException.class, () -> demoController.loadDemoData(request));
        }
        // Verify that entities were not loaded to the database
        verify(userRepository, times(0)).save(any(User.class));
        verify(businessRepository, times(0)).save(any(Business.class));
        verify(productRepository, times(0)).save(any(Product.class));
        verify(inventoryItemRepository, times(0)).save(any(InventoryItem.class));
        verify(saleItemRepository, times(0)).save(any(SaleItem.class));
    }

    /**
     * Test that when a PUT request is made to the "/demo/load" endpoint, and the request has an authentication token
     * which is associated with an account with an admin role, a status code of 200 is received.
     */
    @Test
    void loadDemoData_adminAuthToken_200ResponseTest() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the session role is 'admin'
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager. when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).then(invocation -> null);
            authenticationTokenManager. when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);

            // Verify that a 200 response is received in response to the PUT request
            mockMvc.perform(put("/demo/load"))
                    .andExpect(status().isOk())
                    .andReturn();
        }
    }

    /**
     * Test that when a PUT request is made to the "/demo/load" endpoint, and the request has an authentication token
     * which is associated with an account with an admin role, the demo data is loaded to the repositories.
     */
    @Test
    void loadDemoData_adminAuthToken_dataLoadedTest() {
        // Mock the AuthenticationTokenManager to respond as it would when the session role is 'admin'
        try (MockedStatic<AuthenticationTokenManager> authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class)) {
            authenticationTokenManager. when(() -> AuthenticationTokenManager.checkAuthenticationToken(any())).then(invocation -> null);
            authenticationTokenManager. when(() -> AuthenticationTokenManager.sessionIsAdmin(any())).thenReturn(true);

            // Prevent null pointer exceptions when DemoController tries to retrieve these entities from the repositories
            when(userRepository.findByEmail("123andyelliot@gmail.com")).thenReturn(null).thenReturn(testUser);
            when(businessRepository.save(any(Business.class))).thenReturn(testBusiness);
            when(productRepository.save(any(Product.class))).thenAnswer(x->x.getArgument(0));
            when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(x->x.getArgument(0));
            when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(x->x.getArgument(0));


            assertDoesNotThrow(() -> demoController.loadDemoData(request));
        }
        // Verify that expected number of entities were loaded to the database
        verify(userRepository, times(8)).save(any(User.class));
        verify(businessRepository, times(1)).save(any(Business.class));
        verify(productRepository, times(2)).save(any(Product.class));
        verify(inventoryItemRepository, times(1)).save(any(InventoryItem.class));
        verify(saleItemRepository, times(1)).save(any(SaleItem.class));
    }

}