package org.seng302.persistence;

import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeywordRepository extends CrudRepository<Keyword, Long> {

    List<Keyword> findByOrderByNameAsc();

    /**
     * Finds all the keywords for the given card
     * @param card Cards to get keywords for
     * @return List of keywords for the card
     */
    List<Keyword> getAllByCards(MarketplaceCard card);
}