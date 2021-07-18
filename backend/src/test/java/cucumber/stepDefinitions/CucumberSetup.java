package cucumber.stepDefinitions;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.bs.A;
import org.seng302.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class CucumberSetup {

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    protected BusinessRepository businessRepository;
    @Autowired
    protected InventoryItemRepository inventoryItemRepository;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected SaleItemRepository saleItemRepository;
    @Autowired
    protected MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    protected KeywordRepository keywordRepository;

    /**
     * Set up the mockMvc object for mocking API requests, and remove everything from the repositories.
     */
    @Before(order = 1)
    public void Setup() {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        saleItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        businessRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        keywordRepository.deleteAll();
        userRepository.deleteAll();
        accountRepository.deleteAll();
    }

}
