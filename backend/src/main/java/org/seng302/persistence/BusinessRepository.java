package org.seng302.persistence;

import org.seng302.entities.Business;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Repository
public interface BusinessRepository extends CrudRepository<Business, Long> {
    Business findByName(@Param("name") String name);

    /**
     * Gets a business from the database matching a given Business Id
     * Performs sanity checks to ensure the business is not null
     * Throws ResponseStatusException if business does not exist
     * @param businessId The id of the business to retrieve
     * @return The business matching the given Id
     */
    default Business getBusinessById(Long businessId) {
        // check business exists
        Optional<Business> business = this.findById(businessId);
        if (business.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "The given business does not exist");
        }
        return business.get();
    }
}