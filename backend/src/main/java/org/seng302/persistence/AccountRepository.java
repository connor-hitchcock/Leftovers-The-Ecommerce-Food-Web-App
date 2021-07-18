package org.seng302.persistence;

import org.seng302.entities.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interfaces are used to declare accessors to JPA objects.
 *
 * Spring will scan the projet files for its own annotations and perform some
 * startup operations (e.g., instantiating classes).
 *
 * By declaring a "repository rest resource", we can expose repository (JPA)
 * objects through REST API calls. However, This is discouraged in a
 * Model-View-Controller (or similar patterns).
 *
 * See https://docs.spring.io/spring-data/rest/docs/current/reference/html/
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    /**
     *
     * @param email the email to search for
     * @return a list of Account object with email matching the search parameter.
     * Should have length of one or zero as email is unique
     */
    Account findByEmail(@Param("email") String email);
}
