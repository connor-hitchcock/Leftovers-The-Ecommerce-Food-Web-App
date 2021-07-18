package org.seng302.persistence;

import org.seng302.entities.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A User specification builder
 * Builds specifications made from one or more predicates to assist in searching for users
 */
public class UserSpecificationsBuilder {

    private final List<SearchCriteria> params;

    public UserSpecificationsBuilder() {
        params = new ArrayList<>();
    }

    /**
     * Adds a predicate to the specification
     * @param key The column to compare
     * @param operation The compare operation
     * @param value he value to compare against
     * @param isOrPredicate Determines if predicate will be AND / OR
     * @return The builder
     */
    public UserSpecificationsBuilder with(String key, String operation, Object value, boolean isOrPredicate) {
        params.add(new SearchCriteria(key, operation, value, isOrPredicate));
        return this;
    }

    /**
     * Builds the specification
     * @return A chained set of predicates
     */
    public Specification<User> build() {
        if (params.isEmpty()) {
            return null;
        }

        List<Specification<User>> specs = params.stream()
                .map(UserSpecification::new)
                .collect(Collectors.toList());

        Specification<User> result = specs.get(0);

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i)
                    .isOrPredicate()
                    ? Specification.where(result)
                    .or(specs.get(i))
                    : Specification.where(result)
                    .and(specs.get(i));
        }
        return result;
    }
}