package org.seng302.persistence;

import org.seng302.entities.Image;
import org.seng302.entities.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Repository
public interface ImageRepository extends CrudRepository<Image, Long> {

    Optional<Image> findByFilename(@Param("filename") String filename);
    /**
     * Gets an image from the database that matches a given image Id. This method preforms a sanity check to ensure the
     * image does exist and if not throws a not accepted response status exception.
     * @param imageId the id of the image
     * @return the image object that matches the given Id
     */
    default Image getImageById(Long imageId) {
        Optional<Image> image = findById(imageId);
        if (!image.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "the given image does not exist");
        }
        return image.get();
    }
    /**
     * Gets an image for a product by Product and ID
     * Assures that if an image no longer belongs on the Product then no image is returned.
     * If the image does not exist or does not belong to a product, a 406 Not Acceptable is thrown
     * @param product The product
     * @param imageId The ID of the image to fetch
     * @return An Image or ResponseStatusException
     */
    default Image getImageByProductAndId(Product product, Long imageId) {
        Optional<Image> image = this.findById(imageId);
        if (!image.isPresent() || !product.getProductImages().contains(image.get())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "The given image does not exist");
        }
        return image.get();

    }
}
