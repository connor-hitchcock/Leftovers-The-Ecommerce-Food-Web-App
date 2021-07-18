package org.seng302.persistence;

import org.seng302.entities.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interfaces are used to declare accessors to JPA objects.
 *
 * Spring will scan the project files for its own annotations and perform some
 * startup operations (e.g., instantiating classes).
 *
 * By declaring a "repository rest resource", we can expose repository (JPA)
 * objects through REST API calls. However, This is discouraged in a
 * Model-View-Controller (or similar patterns).
 *
 * See https://docs.spring.io/spring-data/rest/docs/current/reference/html/
 */

public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     *
     * @param email the email to search for
     * @return a list of Account object with email matching the search parameter.
     * Should have length of one or zero as email is unique
     */
    User findByEmail(@Param("email") String email);


    /**
     * Searches for users where the query partially matches one of firstName, middleName, lastName, nickname or partially matches firstName lastName
     * @param query The search term
     * @return List of matching Users
     */
    @Query("SELECT u from User u WHERE lower(concat(u.firstName, u.lastName, u.middleName, u.nickname)) like concat('%',lower(:query), '%') or lower(concat(u.firstName, ' ', u.lastName)) like concat('%',lower(:query), '%') or lower(concat(u.firstName, ' ', u.middleName, ' ', u.lastName)) like concat('%',lower(:query), '%')")
    List<User> findAllByQuery(@Param("query") String query);




}







