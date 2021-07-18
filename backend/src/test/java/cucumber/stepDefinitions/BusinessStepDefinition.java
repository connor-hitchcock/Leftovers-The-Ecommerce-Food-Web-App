package cucumber.stepDefinitions;

import cucumber.BusinessContext;
import cucumber.UserContext;
import io.cucumber.java.en.Given;
import org.seng302.entities.Business;
import org.seng302.entities.Location;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;

public class BusinessStepDefinition {
    @Autowired
    private BusinessContext businessContext;

    @Autowired
    private UserContext userContext;

    @Given("the business {string} exists")
    public void businessExists(String name) throws ParseException {

        var business = new Business.Builder()
                .withName(name)
                .withDescription("Sells stuff")
                .withBusinessType("Retail Trade")
                .withAddress(Location.covertAddressStringToLocation("1,Bob Street,Bob,Bob,Bob,Bob,1010"))
                .withPrimaryOwner(userContext.getLast())
                .build();
        businessContext.save(business);
    }
}
