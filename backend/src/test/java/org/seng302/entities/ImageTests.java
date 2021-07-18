package org.seng302.entities;

import org.junit.jupiter.api.*;
import org.seng302.persistence.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageTests {
    @Autowired
    ImageRepository imageRepository;

    Image testImage;

    final List<String> illegalCharacters = Arrays.asList(".", "\n", "\t", "\\", ",");

    /**
     * Creates a test image to be used within these tests
     */
    void createTestImage() {
        testImage = new Image("help.png", "help_thumbnail.png");
        imageRepository.save(testImage);
    }

    @BeforeEach
    void setUp() {
        imageRepository.deleteAll();
        createTestImage();
    }

    @AfterAll
    void teardown() {
        imageRepository.deleteAll();
    }

    /**
     * Tests that an image object can be created and the attributes are what they are expected to be
     */
    @Test
    void createImageObject_imageCreated_imageCreated() {
        String filename = "boi.png";
        String filenameThumbnail = "boi_thumbnail.png";
        Image newTestImage = new Image(filename, filenameThumbnail);
        assertEquals(filename, newTestImage.getFilename());
        assertEquals(filenameThumbnail, newTestImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object can be successfully save and retrieved from the database (image repository)
     */
    @Test
    void createImageObject_imageInDatabase_imageCreated() {
        Optional<Image> actualImageArray = imageRepository.findById(testImage.getID());
        assertNotNull(actualImageArray);
        Image actualImage = actualImageArray.get();
        assertEquals(testImage.getID(), actualImage.getID());
        assertEquals(testImage.getFilename(), actualImage.getFilename());
        assertEquals(testImage.getFilenameThumbnail(), actualImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object can have its directory (filename) changed
     */
    @Test
    void setFilename_changeFilename_filenameChanged() {
        String filename = "/goodboi/verygoodboi.png";
        testImage.setFilename(filename);
        assertEquals(filename, testImage.getFilename());
    }

    /**
     * Tests that an image object can have its directory for the thumbnail changed
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnail_filenameThumbnailChanged() {
        String filenameThumbnail = "/goodboi/verygoodboi_thumbnail.png";
        testImage.setFilenameThumbnail(filenameThumbnail);
        assertEquals(filenameThumbnail, testImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object cannot have its directory (filename) set to null
     */
    @Test
    void setFilename_changeFilenameToNull_BadRequestException() {
        try {
            testImage.setFilename(null);
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail set to null
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailToNull_BadRequestException() {
        try {
            testImage.setFilenameThumbnail(null);
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("No thumbnail filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) set to an empty string
     */
    @Test
    void setFilename_changeFilenameToEmpty_BadRequestException() {
        try {
            testImage.setFilename("");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An empty filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail set to an empty string
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailToEmpty_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An empty thumbnail filename was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) to include a space
     */
    @Test
    void setFilename_changeFilenameHaveSpace_BadRequestException() {
        try {
            testImage.setFilename("Happy Meal.png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Spaces are not allowed in the filename", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) exclude a dot. This is required to define what
     * type of photo it is
     */
    @Test
    void setFilename_changeFilenameHaveNoDot_BadRequestException() {
        try {
            testImage.setFilename("Connorpng");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory (filename) include an invalid or unsupported image type
     * after the dot, such as dad in this case.
     */
    @Test
    void setFilename_changeFilenameInvalidImageType_BadRequestException() {
        try {
            testImage.setFilename("Connor.yup");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object can have its directory (filename) set to include supported photo types such as PNG, JPG, etc.
     */
    @Test
    void setFilename_changeFilenameValidTypes_filenameChanged() {
        String filenamePNG = "happyboi.png";
        String filenameJPG = "happyboi.jpg";
        String filenamePNGUpper = "happyboi.PNG";
        String filenameJPGUpper = "happyboi.JPG";
        testImage.setFilename(filenamePNG);
        assertEquals(filenamePNG, testImage.getFilename());
        testImage.setFilename(filenameJPG);
        assertEquals(filenameJPG, testImage.getFilename());
        testImage.setFilename(filenamePNGUpper);
        assertEquals(filenamePNGUpper, testImage.getFilename());
        testImage.setFilename(filenameJPGUpper);
        assertEquals(filenameJPGUpper, testImage.getFilename());
    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory (filename).
     */
    @Test
    void setFilename_changeFilenameForwardSlashesBeforeDot_BadRequestException() {
        try {
            testImage.setFilename("connor./png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail to include a space
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveSpace_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("Happy Meal_thumbnail.png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Spaces are not allowed in the thumbnail filename", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail exclude a dot. This is required to define
     * what type of photo it is
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailHaveNoDot_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("Connorthumbnailpng");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object cannot have its directory for the thumbnail include an invalid or unsupported image
     * type after the dot, such as dad in this case.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailInvalidImageType_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("Connor_thumbnail.yup");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object can have its directory for the thumbnail set to include supported photo types such as
     * PNG, JPG, etc.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailValidTypes_filenameThumbnailChanged() {
        String filenameThumbnailPNG = "happyboi_thumbnail.png";
        String filenameThumbnailJPG = "happyboi_thumbnail.jpg";
        String filenameThumbnailPNGUpper = "happyboi_thumbnail.PNG";
        String filenameThumbnailJPGUpper = "happyboi_thumbnail.JPG";
        testImage.setFilenameThumbnail(filenameThumbnailPNG);
        assertEquals(filenameThumbnailPNG, testImage.getFilenameThumbnail());
        testImage.setFilenameThumbnail(filenameThumbnailJPG);
        assertEquals(filenameThumbnailJPG, testImage.getFilenameThumbnail());
        testImage.setFilenameThumbnail(filenameThumbnailPNGUpper);
        assertEquals(filenameThumbnailPNGUpper, testImage.getFilenameThumbnail());
        testImage.setFilenameThumbnail(filenameThumbnailJPGUpper);
        assertEquals(filenameThumbnailJPGUpper, testImage.getFilenameThumbnail());
    }

    /**
     * Tests that an image object cannot have a forward slash after the dot within the directory for the thumbnail.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailForwardSlashesBeforeDot_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("connor_thumbnail./png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("An invalid image format was provided", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image object must have a underscore followed before thumbnail before defining the filename for the
     * thumbnail.
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailNotIncludeUnderscoreThumbnail_BadRequestException() {
        try {
            testImage.setFilenameThumbnail("helppppppppppp.png");
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("The thumbnail filename does not contain an _thumbnail", e.getReason());
        } catch (Exception e) { fail(); }
    }

    /**
     * Tests that an image's filename must not include a newline, \t, two dots, a comma, etc.
     */
    @Test
    void setFilename_changeFilenameIncludeIllegalCharacters_BadRequestException() {
        for (String characters: illegalCharacters) {
            try {
                String filename = "connor/he" + characters + "lp.png";
                testImage.setFilename(filename);
                fail();
            } catch (ResponseStatusException e) {
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
                assertEquals("An illegal character was in the filename", e.getReason());
            } catch (Exception e) { fail(); }
        }
    }

    /**
     * Tests that an image's filename must not include a newline
     */
    @Test
    void setFilenameThumbnail_changeFilenameThumbnailIncludeIllegalCharacters_BadRequestException() {
        for (String characters: illegalCharacters) {
            try {
                String filenameThumbnail = "connor/he" + characters + "lp_thumbnail.png";
                testImage.setFilenameThumbnail(filenameThumbnail);
                fail();
            } catch (ResponseStatusException e) {
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
                assertEquals("An illegal character was in the filename", e.getReason());
            } catch (Exception e) { fail(); }
        }
    }

    /**
     * Checks that there cannot be two images with the same filename within the database.
     */
    @Test
    void createImage_ViolateUniqueFilename_Exception() {
        try {
            testImage = new Image("help.png", "original_thumbnail.png");
            imageRepository.save(testImage);
            fail();
        } catch (Exception e) { assertEquals(DataIntegrityViolationException.class, e.getClass()); }
    }

    /**
     * Checks that there cannot be two images with the same thumbnail filename within the database.
     */
    @Disabled // This is disabled because we currently do not generate image thumbnails, so all thumbnail images are null.
    @Test
    void createImage_ViolateUniqueFilenameThumbnail_BadRequest() {
        try {
            testImage = new Image("original.png", "help_thumbnail.png");
            imageRepository.save(testImage);
            fail();
        } catch (Exception e) { assertEquals(DataIntegrityViolationException.class, e.getClass()); }
    }

}

