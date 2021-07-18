package org.seng302.persistence;

import org.seng302.entities.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Defines a specification for type User
 * Used to create search predicates
 * Can be chained together to form a specification
 */
public class UserSpecification implements Specification<User> {

    @Transient
    private SearchCriteria criteria;

    /**
     * Specification
     * @param criteria The search criteria for this predicate
     */
    public UserSpecification(SearchCriteria criteria){
        this.criteria = criteria;
    }

    /**
     * Constructs a predicate of type User
     * @param root Criteria root
     * @param query The search criteria
     * @param builder Criteria Builder
     * @return A predicate matching the search criteria
     */
    @Override
    public Predicate toPredicate
            (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        if (criteria.getOperation().equalsIgnoreCase(">")) {
            return builder.greaterThanOrEqualTo(
                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase("<")) {
            return builder.lessThanOrEqualTo(
                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase(":")) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return builder.like(
                        builder.lower(root.<String>get(criteria.getKey())), "%" + criteria.getValue().toString().toLowerCase() + "%");
            }
        }
        else if (criteria.getOperation().equalsIgnoreCase("=") &&
                 root.get(criteria.getKey()).getJavaType() == String.class) {
            return builder.like(
                    root.<String>get(criteria.getKey()), criteria.getValue().toString());

        }
        return null;
    }
}

