package org.seng302.entities;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;

@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false, unique = true)
    private String filename;

    @Column(name = "filename_thumbnail", nullable = true, unique = false)
    private String filenameThumbnail;

    /**
     * The constructor for a product image
     * @param filename the directory where the image is stored
     * @param filenameThumbnail the directory where the image's thumbnail is located
     */
    public Image(String filename, String filenameThumbnail) {
        this.filename = filename;
        this.filenameThumbnail = filenameThumbnail;
    }

    /**
     * Empty constructor to make spring happy
     */
    protected Image() {

    }

    public JSONObject constructJSONObject() {
        var object = new JSONObject();
        object.put("id", getID());
        object.put("filename", "/media/images/" + getFilename());
        object.put("thumbnailFilename", "/media/images/" + getFilenameThumbnail());
        return object;
    }

    /**
     * Contains a list of all the image formats that the image can be and checks if the filename is one of the accepted
     * image formats
     * @param filename the name of the directory
     * @return true if the image format is accepted, false otherwise
     */
    private boolean checkImageFormats(String filename) {
        // List of all the image formats an image can be
        final List<String> imageFormats = Arrays.asList(".png", ".jpg");
        if (filename.length() > 4) {
            for (String format : imageFormats) {
                String imageFormat = filename.substring(filename.length() - 4).toLowerCase();
                if (format.equals(imageFormat)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A helper function to confirm that the filename thumbnail contains the string "_thumbnail" before the image type
     * @param filenameThumbnail the thumbnail filename
     * @return true if the thumbnail filename contains "_thumbnail" before the image type
     */
    private boolean checkContainsUnderscoreThumbnail(String filenameThumbnail) {
        if (filenameThumbnail.length() > 14 &&
            "_thumbnail".equals(filenameThumbnail.substring(filenameThumbnail.length() - 14, filenameThumbnail.length() - 4))
        ) {
            return true;
        }
        return false;
    }

    /**
     * A helper function to check the filename does not contain any illegal characters
     * @param filename the name of the directory
     * @return true if the the filename does not contain any illegal characters, false otherwise
     */
    private boolean checkContainsIllegalCharacters(String filename) {
        // A list of illegal characters that cannot exist within a filename directory string
        final List<String> illegalCharacters = Arrays.asList(".", "\n", "\t", "\\", ",");

        String filenameSubStr = filename.substring(0, filename.length() - 4);
        for (String characters: illegalCharacters) {
            if (filenameSubStr.contains(characters)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the id associated with the image
     * @return the image's id
     */
    public Long getID() {
        return id;
    }

    /**
     * Gets the image directory of where the image is located
     * @return the directory
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the image directory of where the image thumbnail is located
     * @return the directory
     */
    public String getFilenameThumbnail() { return filenameThumbnail; }

    /**
     * Sets the direction location of where the image file is located
     * @param filename the directory of where the image is located
     */
    public void setFilename(String filename) {
        if (filename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No filename was provided");
        } else if (filename.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An empty filename was provided");
        } else if (filename.contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Spaces are not allowed in the filename");
        } else if (!checkImageFormats(filename)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An invalid image format was provided");
        } else if (!checkContainsIllegalCharacters(filename)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An illegal character was in the filename");
        }
        this.filename = filename;
    }

    /**
     * Sets the direction location of where the image file is located
     * @param filenameThumbnail the directory of where the image thumbnail is located
     */
    public void setFilenameThumbnail(String filenameThumbnail) {
        if (filenameThumbnail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No thumbnail filename was provided");
        } else if (filenameThumbnail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An empty thumbnail filename was provided");
        } else if (filenameThumbnail.contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Spaces are not allowed in the thumbnail filename");
        } else if (!checkImageFormats(filenameThumbnail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An invalid image format was provided");
        } else if (!checkContainsUnderscoreThumbnail(filenameThumbnail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The thumbnail filename does not contain an _thumbnail");
        } else if (!checkContainsIllegalCharacters(filenameThumbnail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An illegal character was in the filename");
        }
        this.filenameThumbnail = filenameThumbnail;
    }

    @Override
    public String toString() {
        return "IMAGE_"+this.getID().toString();
    }
}
