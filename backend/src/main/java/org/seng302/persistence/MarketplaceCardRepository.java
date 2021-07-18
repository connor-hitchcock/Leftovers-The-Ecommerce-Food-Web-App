package org.seng302.persistence;

import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceCardRepository extends CrudRepository<MarketplaceCard, Long> {
    /**
     * Finds all the marketplace cards created by a given user
     * @param user User that the cards belong to
     * @return List of created cards
     */
    List<MarketplaceCard> getAllByCreator(@Param("Creator") User user);

    /**
     * Finds all the marketplace cards with the given keyword
     * @param keyword Keyword to search for
     * @return List of cards with the keyword
     */
    List<MarketplaceCard> getAllByKeywords(@Param("keywords") Keyword keyword);

    List<MarketplaceCard> getAllBySection(@Param("section") MarketplaceCard.Section section);
}
