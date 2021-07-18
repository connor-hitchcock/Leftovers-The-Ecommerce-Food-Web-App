package cucumber.stepDefinitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.seng302.entities.Location;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationStepDefinition {

    @Autowired
    private ObjectMapper objectMapper;

    private Location theAddress;

    @Given("the address {string} does not exist")
    public void theAddressDoesNotExist(String address) {
        Assert.assertNotSame(theAddress, Location.covertAddressStringToLocation(address));
    }

    @When("the address {string} is created")
    public void theAddressIsCreated(String address) {
        theAddress = Location.covertAddressStringToLocation(address);
    }

    @Then("the address {string} exists")
    public void theAddressExists(String address) {
        Assert.assertEquals(theAddress, Location.covertAddressStringToLocation(address));
    }

    @Then("the address has the street number {string}")
    public void theAddressHasTheStreetNumber(String streetNumber) {
        Assert.assertEquals(theAddress.getStreetNumber(), streetNumber);
    }

    @Then("the address has the street name {string}")
    public void theAddressHasTheStreetName(String streetName) {
        Assert.assertEquals(theAddress.getStreetName(), streetName);
    }

    @Then("the address has the district {string}")
    public void theAddressHasTheDistrict(String district) {
        Assert.assertEquals(theAddress.getDistrict(), district);
    }

    @Then("the address has the city name {string}")
    public void theAddressHasTheCityName(String city) {
        Assert.assertEquals(theAddress.getCity(), city);
    }

    @Then("the address has the region name {string}")
    public void theAddressHasTheRegionName(String region) {
        Assert.assertEquals(theAddress.getRegion(), region); }

    @Then("the address has the country name {string}")
    public void theAddressHasTheCountryName(String country) {
        Assert.assertEquals(theAddress.getCountry(), country);
    }

    @Then("the address has the post code {string}")
    public void theAddressHasThePostCode(String postCode) {
        Assert.assertEquals(theAddress.getPostCode(), postCode);
    }
}
