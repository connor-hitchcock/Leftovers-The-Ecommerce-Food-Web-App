package org.seng302.controllers;

import net.minidev.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Keyword;
import org.seng302.persistence.KeywordRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class KeywordController {
    private static final Logger logger = LogManager.getLogger(KeywordController.class);

    private final KeywordRepository keywordRepository;

    public KeywordController(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }

    /**
     * REST GET method to retrieve all the global keyword entities
     * @param request the HTTP request
     * @return List of all the keyword entities
     */
    @GetMapping("/keywords/search")
    public JSONArray searchKeywords(HttpServletRequest request) {
        try {
            logger.info("Getting all the keywords");
            AuthenticationTokenManager.checkAuthenticationToken(request);

            JSONArray result = new JSONArray();
            for (Keyword keyword : keywordRepository.findByOrderByNameAsc()) {
                result.add(keyword.constructJSONObject());
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
