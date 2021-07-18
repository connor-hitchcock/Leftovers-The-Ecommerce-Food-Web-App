package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.BusinessContext;
import cucumber.RequestContext;
import cucumber.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.sl.In;
import net.minidev.json.JSONArray;
import org.seng302.entities.*;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InventoryStepDefinition  {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BusinessContext businessContext;
    @Autowired
    private UserContext userContext;
    @Autowired
    private RequestContext requestContext;

    private MvcResult mvcResult;
    private String productCode;
    private Integer quantity;

    @Given("the business has the following products in its catalogue:")
    public void the_business_has_the_following_products_in_its_catalogue(io.cucumber.datatable.DataTable dataTable) {
        var business = businessContext.getLast();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Product product = new Product.Builder()
                    .withProductCode(row.get("product_id"))
                    .withName(row.get("name"))
                    .withBusiness(business)
                    .build();
            business.addToCatalogue(product);
            productRepository.save(product);
        }
        businessContext.save(business);
    }

    @Given("the business has the following items in its inventory:")
    public void the_business_has_the_following_items_in_its_inventory(io.cucumber.datatable.DataTable dataTable) throws Exception {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Optional<Product> product = productRepository.findByBusinessAndProductCode(businessContext.getLast(), row.get("product_id"));
            if (product.isEmpty()) {
                throw new Exception("Product should be saved to product repository");
            }
            InventoryItem item = new InventoryItem.Builder()
                    .withProduct(product.get())
                    .withQuantity(Integer.parseInt(row.get("quantity")))
                    .withExpires(row.get("expires"))
                    .build();
            inventoryItemRepository.save(item);
        }
    }

    @Given("I am an administrator of the business")
    public void i_am_an_administrator_of_the_business() throws ParseException {
        var actor = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("actor@testing")
                .withPassword("12345678abc")
                .withDob("2001-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        userContext.save(actor);

        var business = businessContext.getLast();
        business.addAdmin(actor);
        business = businessContext.save(business);

        boolean checkAdmin = false;
        for (User user : business.getAdministrators()) {
            if (user.getUserID().equals(actor.getUserID())) {
                checkAdmin = true;
                break;
            }
        }
        assertTrue(checkAdmin);
    }

    @Given("I am an not an administrator of the business")
    public void i_am_an_not_an_administrator_of_the_business() throws ParseException {
        var actor = new User.Builder()
                .withFirstName("John")
                .withLastName("Smith")
                .withEmail("actor@testing")
                .withPassword("12345678abc")
                .withDob("2001-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        actor = userContext.save(actor);
        for (User user : businessContext.getLast().getAdministrators()) {
            assertNotEquals(user.getUserID(), actor.getUserID());
        }
    }

    @Given("I am logged into my account")
    public void i_am_logged_into_my_account() {
        requestContext.setLoggedInAccount(userContext.getLast().getUserID());
    }

    @When("I try to access the inventory of the business")
    public void i_try_to_access_the_inventory_of_the_business() throws Exception {
        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        get(String.format("/businesses/%s/inventory", businessContext.getLast().getId()))
                )
        ).andReturn();
    }

    @Then("the inventory of the business is returned to me")
    public void the_inventory_of_the_business_is_returned_to_me() throws UnsupportedEncodingException, JsonProcessingException {
        assertEquals(200, mvcResult.getResponse().getStatus());

        List<Product> catalogue = productRepository.findAllByBusiness(businessContext.getLast());
        List<InventoryItem> inventory = inventoryItemRepository.getInventoryByCatalogue(catalogue);

        //because now the inventory sorts by product code on default, so it has to be sorted before comparing
        Comparator<InventoryItem> sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getProductCode());
        inventory.sort(sort);
        
        JSONArray jsonArray = new JSONArray();
        for (InventoryItem item : inventory) {
            jsonArray.appendElement(item.constructJSONObject());
        }
        String expectedResponse = jsonArray.toJSONString();

        String responseBody = mvcResult.getResponse().getContentAsString();
        assertEquals(objectMapper.readTree(expectedResponse), objectMapper.readTree(responseBody));
    }

    @Then("I cannot view the inventory")
    public void i_cannot_view_the_inventory() throws UnsupportedEncodingException {
        assertEquals(403, mvcResult.getResponse().getStatus());
        assertEquals("", mvcResult.getResponse().getContentAsString());
    }

    @When("I create an inventory item with product code {string} and quantity {int} and expiry {string}")
    public void i_create_inventory_with_product_code_quantity_string(String productCode, Integer quantity, String expiry) throws Exception {
        this.quantity = quantity;
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                    "\"productId\": \"%s\"," +
                    "\"quantity\": %d," +
                    "\"expires\": \"%s\"" +
                "}"
        , productCode, quantity, expiry);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                        post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                )
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I create an inventory item with product code {string} and quantity {int}, expiry {string}, price per item {int} and total price {int}")
    public void i_create_inventory_with_all_details(String productCode, Integer quantity, String expiry, Integer pricePerItem, Integer totalPrice) throws Exception {
        this.quantity = quantity;
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                    "\"productId\": \"%s\"," +
                    "\"quantity\": %d," +
                    "\"expires\": \"%s\"," +
                    "\"pricePerItem\": %d," +
                    "\"totalPrice\": %d" +
                "}"
                , productCode, quantity, expiry, pricePerItem, totalPrice);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                ).content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I create an inventory item with product code {string}, quantity {int}, expiry {string}, manufactured on {string}, sell by {string} and best before {string}")
    public void i_create_inventory_with_all_dates(String productCode, Integer quantity, String expiry, String manufactured, String sellBy, String bestBefore) throws Exception {
        this.quantity = quantity;
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                        "\"productId\": \"%s\"," +
                        "\"quantity\": %d," +
                        "\"expires\": \"%s\"," +
                        "\"manufactured\": \"%s\"," +
                        "\"sellBy\": \"%s\"," +
                        "\"bestBefore\": \"%s\"" +
                        "}"
                , productCode, quantity, expiry, manufactured, sellBy, bestBefore);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                ).content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I create an inventory item with product code {string} and no other fields")
    public void i_create_inventory_item_without_required_fields(String productCode) throws Exception {
        this.productCode = productCode;
        String postBody = String.format(
                "{ " +
                    "\"productId\": \"%s\"" +
                "}"
                , productCode);

        mvcResult = mockMvc.perform(
                requestContext.addAuthorisationToken(
                    post(String.format("/businesses/%d/inventory", businessContext.getLast().getId()))
                ).content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @Then("I expect to be prevented from creating the inventory item")
    public void i_expect_to_be_prevented_from_creating_inventory_item() {
        assertEquals(400, mvcResult.getResponse().getStatus());
        Product product = productRepository.getAllByBusiness(businessContext.getLast()).stream().filter(x -> x.getProductCode().equals(this.productCode)).collect(Collectors.toList()).get(0);
        List<InventoryItem> inventory = inventoryItemRepository.findAllByProduct(product);
        assertEquals(1, inventory.size());

    }

    @Then("I expect the inventory item to be created")
    public void i_expect_the_inventory_to_be_created() {
        assertEquals(200, mvcResult.getResponse().getStatus());
        Product product = productRepository.getAllByBusiness(businessContext.getLast()).stream().filter(x -> x.getProductCode().equals(this.productCode)).collect(Collectors.toList()).get(0);
        List<InventoryItem> inventory = inventoryItemRepository.findAllByProduct(product);
        assertTrue(inventory.stream().anyMatch(x-> x.getQuantity() == quantity));
    }
}
