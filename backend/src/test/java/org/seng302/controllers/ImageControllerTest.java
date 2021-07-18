package org.seng302.controllers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.seng302.entities.Image;
import org.seng302.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageControllerTest {

    @Autowired
    private ImageRepository imageRepository;

    private Image testImage;

    @BeforeAll
    private void setUp() {
        imageRepository.deleteAll();
        testImage = new Image("anImage.png", "anImage_thumbnail.png");
        imageRepository.save(testImage);
    }

    @AfterAll
    private void tearDown() {
        imageRepository.deleteAll();
    }

    /**
     * Checks that an image that exists within the database can be retrieves
     */
    @Test
    void getImage_ImageExist_getExpectedImage() {
        Image actualImage = imageRepository.getImageById(testImage.getID());
        assertEquals(testImage.getID(), actualImage.getID());
    }

    /**
     * Checks that an image that does not exists cannot be retrieved
     */
    @Test
    void getImage_ImageDoesNotExist_406ResponseException() {
        imageRepository.delete(testImage);
        long id = testImage.getID();
        assertThrows(ResponseStatusException.class, () -> {
            imageRepository.getImageById(id);
        });
    }

}
