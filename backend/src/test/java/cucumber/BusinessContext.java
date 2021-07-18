package cucumber;

import io.cucumber.java.Before;
import org.seng302.entities.Business;
import org.seng302.persistence.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class BusinessContext {

    private final Map<String, Business> businessMap = new HashMap<>();
    private Business lastBusiness = null;

    @Autowired
    private BusinessRepository businessRepository;

    @Before
    public void setup() {
        businessMap.clear();
        lastBusiness = null;
    }

    /**
     * Gets the last modified business
     * @return Last modified business
     */
    public Business getLast() {
        return lastBusiness;
    }

    /**
     * Gets a business from the name
     * @param name Business name
     * @return Business with matching name otherwise null
     */
    public Business getByName(String name) {
        return businessMap.get(name);
    }

    /**
     * Saves a business using the business repository
     * Will update the last business
     * @param business Business to save
     * @return Saved business
     */
    public Business save(Business business) {
        lastBusiness = businessRepository.save(business);
        businessMap.put(lastBusiness.getName(), lastBusiness);
        return lastBusiness;
    }
}
