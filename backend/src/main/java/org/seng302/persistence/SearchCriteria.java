package org.seng302.persistence;

/**
 * Defines a criteria for building predicates
 */
public class SearchCriteria {

    private String column;

    private String operation;

    private Object value;

    private boolean isOrPredicate = true;

    public SearchCriteria(String column, String operation, Object value) {
        setKey(column);
        setOperation(operation);
        setValue(value);

    }

    /**
     * Search criteria predicate helper
     * @param column The column to compare
     * @param operation The compare operation
     * @param value The value to compare against
     * @param isOrPredicate Determines if predicate will be AND / OR
     */
    public SearchCriteria(String column, String operation, Object value, boolean isOrPredicate) {
        setKey(column);
        setOperation(operation);
        setValue(value);
        setOrPredicate(isOrPredicate);
    }


    public void setKey(String column){
        this.column=column;
    }

    public String getKey() {
        return this.column;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isOrPredicate(){
        return this.isOrPredicate;
    }

    public void setOrPredicate(boolean orPredicate){
        this.isOrPredicate = orPredicate;
    }
}