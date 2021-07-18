package cucumber;

import io.cucumber.java.Before;
import org.seng302.entities.MarketplaceCard;
import org.seng302.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CardContext {
    private MarketplaceCard lastCard = null;

    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;

    @Before
    public void setup() {lastCard = null;}

    /**
     * Returns the last modified Card
     * @return last modified Card
     */
    public MarketplaceCard getLast() {return lastCard;}

    /**
     * Saves a card using the marketplaceCard repository
     * Also sets the last card
     * @param card Card to save
     * @return Saved card
     */
    public MarketplaceCard save(MarketplaceCard card) {
        lastCard = marketplaceCardRepository.save(card);
        return lastCard;
    }
}
