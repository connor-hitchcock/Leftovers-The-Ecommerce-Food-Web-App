package org.seng302.persistence;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.seng302.entities.Location;
import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarketplaceCardRepositoryTest {
    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    private MarketplaceCard card;
    private User user;

    @BeforeAll
    private void setUp() throws ParseException {
        businessRepository.deleteAll();
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
        Location address = new Location.Builder()
                .inCity("city")
                .inCountry("New Zealand")
                .inRegion("region")
                .onStreet("street")
                .atStreetNumber("3")
                .withPostCode("222")
                .build();
        user = new User.Builder()
                .withEmail("john@smith.com")
                .withFirstName("John")
                .withLastName("Smith")
                .withAddress(address)
                .withPassword("password123")
                .withDob("2000-08-04")
                .build();
        user = userRepository.save(user);
        card = new MarketplaceCard.Builder()
                .withTitle("Some Title")
                .withDescription("Some description")
                .withCreator(user)
                .withSection("Wanted")
                .build();
        marketplaceCardRepository.save(card);
    }

    @AfterAll
    private void tearDown() {
        marketplaceCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Returns a stream of section names
     * @return Section names
     */
    private static Stream<Arguments> sections() {
        return Stream.of(
                Arguments.of("Wanted"),
                Arguments.of("ForSale"),
                Arguments.of("Exchange")
        );
    }

    /**
     * Maps params for a cards section to a search section term
     */
    private static Stream<Arguments> invalidSections() {
        return Stream.of(
                Arguments.of("Wanted", "ForSale"),
                Arguments.of("ForSale", "Exchange"),
                Arguments.of("Exchange", "Wanted")
        );
    }

    @ParameterizedTest
    @MethodSource("sections")
    void getAllBySection_getsCardsFromCorrectSection(String sectionName) {
        MarketplaceCard.Section section = MarketplaceCard.sectionFromString(sectionName);
        card.setSection(section);
        card = marketplaceCardRepository.save(card);

        List<MarketplaceCard> cards = marketplaceCardRepository.getAllBySection(section);
        System.out.println(cards);
        System.out.println(card);
        Assertions.assertTrue(cards.contains(card));

    }

    @ParameterizedTest
    @MethodSource("invalidSections")
    void getAllBySection_doesntGetCardsFromOtherSections(String cardSectionName, String searchSectionName) {
        MarketplaceCard.Section cardSection = MarketplaceCard.sectionFromString(cardSectionName);
        card.setSection(cardSection);
        card = marketplaceCardRepository.save(card);

        MarketplaceCard.Section searchSection = MarketplaceCard.sectionFromString(searchSectionName);

        List<MarketplaceCard> cards = marketplaceCardRepository.getAllBySection(searchSection);
        Assertions.assertFalse(cards.contains(card));

    }

}
