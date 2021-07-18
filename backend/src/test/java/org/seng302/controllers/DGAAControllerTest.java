package org.seng302.controllers;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DGAAControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private DGAAController dgaaController;

    @BeforeEach
    public void clean() {
        //because business repo has a foreign key in user repo, it needs to be cleared too
        inventoryItemRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * DGAA repo is empty, ensure a new DGAA is generated
     */
    @Test @Ignore
    void dgaaNotPresent() {
        dgaaController.checkDGAA();
        assert(userRepository.findByEmail("wasteless@seng302.com") != null);
    }

    /**
     * DGAA already exists, no need for new one
     * @throws ParseException
     */
    @Test @Ignore
    void dgaaPresent() throws ParseException {
        dgaaController.checkDGAA();
        User dgaa;
        Location adminAddress;
        adminAddress = new Location.Builder()
                    .atStreetNumber("1")
                    .onStreet("wasteless")
                    .inCity("wasteless")
                    .inRegion("wasteless")
                    .inCountry("wasteless")
                    .withPostCode("1111")
                    .build();
        dgaa = new User.Builder()
            .withEmail("wasteless2@seng302.com")
            .withFirstName("DGAA")
            .withLastName("DGAA")
            .withPassword("T3amThr33IsTh3B3st")
            .withDob("2000-03-11")
            .withAddress(adminAddress)
            .build();
        try {
            dgaa.setRole("defaultGlobalApplicationAdmin");
            userRepository.save(dgaa);
        } catch (IllegalArgumentException e) {
            assertEquals("Tried creating new DGAA", e.getMessage());
        }

    }
}
