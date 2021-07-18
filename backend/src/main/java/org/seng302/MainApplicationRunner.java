package org.seng302;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Keyword;
import org.seng302.persistence.KeywordRepository;
import org.seng302.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * NOTE: Use this class to setup application
 * Avoid using Main.class
 */
@Component
public class MainApplicationRunner implements ApplicationRunner {
    private static final List<String> DEFAULT_KEYWORD_NAMES = List.of("Free", "Home Baking", "With Love", "Inspired", "Bulk", "Gluten Free", "Vegan", "Vegetarian", "Fun", "Keto", "Plant Based", "Fat Free", "Italian", "French", "Local Produce", "Fresh", "Eco", "Sustainable", "Asian", "Curry", "Dairy", "Perishable", "Non Perishable", "Free Range", "Natural", "Organic", "Connor", "Kosher", "Paleo", "Home Grown");

    @Resource
    private StorageService storageService;

    @Autowired
    private KeywordRepository keywordRepository;

    private static final Logger logger = LogManager.getLogger(MainApplicationRunner.class.getName());


    /**
     * By overriding the run method, we tell Spring to run this code at startup. See
     * https://dzone.com/articles/spring-boot-applicationrunner-and-commandlinerunne
     */
    @Override
    public void run(ApplicationArguments args) {
        logger.info("Startup application with {}", args);
        storageService.init();
        addDemoKeywordsIfNoneExist();
    }


    /**
     * Adds the default keywords to the keyword repository if no keywords currently exist
     */
    private void addDemoKeywordsIfNoneExist() {
        if (keywordRepository.count() != 0) return;

        for (String keywordName : DEFAULT_KEYWORD_NAMES) {
            keywordRepository.save(new Keyword(keywordName));
        }
    }
}
