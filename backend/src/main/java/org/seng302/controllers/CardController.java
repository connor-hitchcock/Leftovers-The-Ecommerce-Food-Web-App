package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.seng302.tools.JsonTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.seng302.tools.SearchHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Comparator;

/**
 * This controller handles requests involving marketplace cards
 */
@RestController
public class CardController {

    private final MarketplaceCardRepository marketplaceCardRepository;
    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final Logger logger = LogManager.getLogger(CardController.class.getName());

    @Autowired
    public CardController(MarketplaceCardRepository marketplaceCardRepository, KeywordRepository keywordRepository, UserRepository userRepository) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint to create a card with the properties given in the json body of the request.
     * Will return a 401 error if the request is unauthorized, a 403 error if the user does not have perimssion to create
     * a card with the given creatorId, or a 400 error if the json body is not formatted as expected.
     * Will return a 201 response and a json with the id of the created card if the request is successful.
     * @param request The HTTP request, used for checking permissions.
     * @param response The HTTP response, used for changing response status.
     * @param cardProperties The JSON body of the request with data for creating the cards.
     * @return A json with a cardId attribute if the request is successful.
     */
    @PostMapping("/cards")
    public JSONObject createCard(HttpServletRequest request, HttpServletResponse response, @RequestBody JSONObject cardProperties) {
        logger.info("Request to create marketplace card received");
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);

            // Check that request comes from a user whose account matches the card's creator id or who is an admin
            long creatorId = JsonTools.parseLongFromJsonField(cardProperties, "creatorId");
            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, creatorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot create card for another user");
            }

            // Save the card to the database
            MarketplaceCard card = constructCardFromJson(cardProperties);
            card = marketplaceCardRepository.save(card);

            // Construct and return a json with the card id
            JSONObject json = new JSONObject();
            json.appendField("cardId", card.getID());
            response.setStatus(201);
            return json;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve the User and Keywords associated with this card from the database and use them to construct the
     * marketplace card with the properties given by the json object. A response status exception with 400 status
     * will be thrown if any part of the given json has invalid format.
     * @param cardProperties A json with the properites to be used when constructing the card.
     * @return A marketplace card constructed with the given properties.
     */
    private MarketplaceCard constructCardFromJson(JSONObject cardProperties) {
        // Retrieve the user from their id
        long creatorId = JsonTools.parseLongFromJsonField(cardProperties, "creatorId");
        Optional<User> optionalUser = userRepository.findById(creatorId);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with ID %d does not exist", creatorId));
        }
        User creator = optionalUser.get();

        // Create the card
        MarketplaceCard card = new MarketplaceCard.Builder()
                .withTitle(cardProperties.getAsString("title"))
                .withDescription(cardProperties.getAsString("description"))
                .withSection(cardProperties.getAsString("section"))
                .withCreator(creator)
                .build();

        // Retrieve all the keywords and add them to the card
        long[] keywordIds = JsonTools.parseLongArrayFromJsonField(cardProperties, "keywordIds");
        for (long keywordId : keywordIds) {
            Optional<Keyword> optional = keywordRepository.findById(keywordId);
            if (optional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Keyword with ID %d does not exist", keywordId));
            }
            card.addKeyword(optional.get());
        }

        return card;
    }

    /**
     * Retrieve all of the Marketplace Cards for a given section.
     * @param sectionName The name of the section to retrieve
     * @param page The page number of the current requested section
     * @param resultsPerPage Maximum number of results to retrieve
     * @return A JSON Array of Marketplace cards
     */
    @GetMapping("/cards")
    public JSONArray getCards(HttpServletRequest request,
                              @RequestParam(name = "section") String sectionName,
                              @RequestParam(required = false) String orderBy,
                              @RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer resultsPerPage,
                              @RequestParam(required = false) Boolean reverse) {
        
        logger.info("Request to get marketplace cards for " + sectionName);
        AuthenticationTokenManager.checkAuthenticationToken(request);

        // parse the section
        MarketplaceCard.Section section = MarketplaceCard.sectionFromString(sectionName);

        Comparator<MarketplaceCard> sort = getMarketPlaceCardComparator(orderBy, reverse);

        // database call for section
        var cards = marketplaceCardRepository.getAllBySection(section);

        cards.sort(sort);

        cards = SearchHelper.getPageInResults(cards, page, resultsPerPage);
        //return JSON Object
        JSONArray responseBody = new JSONArray();
        for (MarketplaceCard card : cards) {
            responseBody.appendElement(card.constructJSONObject());
        }
        return responseBody;
    }

    /**
     * REST GET method to retrieve the number of cards in the marketplace.
     * @param request the HTTP request
     * @param sectionName the requested section name
     * @return the card count by the requested section
     */
    @GetMapping("/cards/count")
    public JSONObject retrieveCardCount(HttpServletRequest request,
                                        @RequestParam(name = "section") String sectionName) {

        AuthenticationTokenManager.checkAuthenticationToken(request);

        //if the section is invalid, an error would already be thrown.
        MarketplaceCard.Section section = MarketplaceCard.sectionFromString(sectionName);

        var cards = marketplaceCardRepository.getAllBySection(section);

        JSONObject responseBody = new JSONObject();
        responseBody.put("count", cards.size());

        return responseBody;
    }

    /**
     * Sort marketplace cards by a key. Can reverse results.
     * 
     * @param orderBy Key to order marketplace cards by.
     * @param reverse Reverse results.
     * @return MarketplaceCard Comparator
     */
    public Comparator<MarketplaceCard> getMarketPlaceCardComparator(String orderBy, Boolean reverse) {
        if (orderBy == null) orderBy = "created";

        Comparator<MarketplaceCard> sort;
        switch (orderBy) {
            case "title":
                sort = Comparator.comparing(MarketplaceCard::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "closes":
                sort = Comparator.comparing(MarketplaceCard::getCloses);
                break;
            case "creatorFirstName":
                sort = Comparator.comparing(marketPlaceCard -> marketPlaceCard.getCreator().getFirstName(), String.CASE_INSENSITIVE_ORDER);
                break;
            case "creatorLastName":
                sort = Comparator.comparing(marketPlaceCard -> marketPlaceCard.getCreator().getLastName(), String.CASE_INSENSITIVE_ORDER);
                break;
            case "created":
            default:
                sort = Comparator.comparing(MarketplaceCard::getCreated);
                break;
        }
        if (Boolean.TRUE.equals(reverse)) {
            sort = sort.reversed();
        }

        return sort;
    }
}
