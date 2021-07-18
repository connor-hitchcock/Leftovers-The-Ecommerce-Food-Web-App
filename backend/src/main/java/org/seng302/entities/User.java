/* Subtype of Account for individual users */
package org.seng302.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.seng302.tools.JsonTools;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Entity
public class User extends Account {

    private String firstName;
    private String middleName;
    private String lastName;
    private String nickname;
    private String bio;
    private LocalDate dob;
    private String phNum;
    private Location address;
    private Instant created;
    private Set<Business> businessesAdministered = new HashSet<>();
    private Set<Business> businessesOwned = new HashSet<>();

    /* Matches:
    123-456-7890
    (123) 456-7890
    123 456 7890
    123.456.7890
    +91 (123) 456-7890
     */
    private static final String PHONE_REGEX = "^(\\+\\d{1,2}\\s)?\\(?\\d{1,3}\\)?[\\s.-]?\\d{3,4}[\\s.-]?\\d{4,5}";


    protected User() {}

    /**
     * Returns users first name
     * @return firstName
     */
    @Column(nullable = false)
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the users first name
     * Not Null
     * @param firstName users first name
     */
    public void setFirstName(String firstName) {
        if (firstName != null && firstName.length() > 0 && firstName.length() <= 32 && firstName.matches("[ a-zA-Z\\-]+")) {
            this.firstName = firstName;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The first name must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns users middle name
     * @return middle name of user
     */
    public String getMiddleName() {return middleName;}

    /**
     * Sets users middle name
     * Can be null
     * @param middleName
     */
    public void setMiddleName(String middleName) {
        if (middleName == null || (middleName.length() > 0 && middleName.length() <= 32 && middleName.matches("[ a-zA-Z\\-]+"))) {
            this.middleName = middleName;
        } else if (middleName.equals("")) {
            this.middleName = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The middle name must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns users last name
     * @return lastName
     */
    @Column(nullable = false)
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets users last name
     * Not Null
     * @param lastName users surname
     */
    public void setLastName(String lastName) {
        if (lastName != null && lastName.length() > 0 && lastName.length() <= 32 && lastName.matches("[ a-zA-Z\\-]+")) {
            this.lastName = lastName;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The last name must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns users preferred name
     * @return nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the users preferred nickname
     * @param nickname users preferred name
     */
    public void setNickname(String nickname) {
        if (nickname == null || (nickname.length() > 0 && nickname.length() <= 32 && nickname.matches("[ a-zA-Z]*"))) {
            this.nickname = nickname;
        } else if (nickname.equals("")) {
            this.nickname = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The nickname must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns the users biography
     * @return bio
     */
    public String getBio() {
        return this.bio;
    }

    /**
     * Sets the users biography - short text about themselves
     * @param bio brief description of user
     */
    public void setBio(String bio) {
        if (bio == null || (bio.length() > 0 && bio.length() <= 200 && bio.matches("[ a-zA-Z0-9\\p{Punct}]*"))) {
            this.bio = bio;
        } else if (bio.equals("")) {
            this.bio = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The bio must be less than 200 characters long," + 
            "and only contain letters, numbers, and valid special characters");
        }
    }

    /**
     * Returns the users date of birth
     * @return dob
     */
    @Column(nullable = false)
    @JsonProperty("dateOfBirth")
    public LocalDate getDob() {
        return dob;
    }

    /**
     * Sets the users date of birth
     * Not Null
     * Check the dob satisfied the condition( >= 13years)
     * Date constructor is deprecated (Date class issue)
     * LocalDate class can be used but come with time zone -- over complicated
     * @param dob date of birth (used to verify age)
     */
    public void setDob(LocalDate dob) {
        if (dob != null) {
            LocalDate now = LocalDate.now();
            LocalDate minDate = now.minusYears(13);
            
            if (dob.compareTo(minDate) < 0) {
                this.dob = dob;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be at least 13 years old to create an account");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your date of birth has been entered incorrectly");
        }
    }

    /**
     * Returns the users phone number
     * @return phNum
     */
    @JsonProperty("phoneNumber")
    public String getPhNum() {
        return phNum;
    }

    /**
     * Sets the users phone number, must be in proper ph num format
     * @param phNum users contact number
     */
    public void setPhNum(String phNum) {
        boolean validPhone = false;
        if (phNum == null || Pattern.matches(PHONE_REGEX, phNum)) {
            validPhone = true;
        }
        if (validPhone) {
            this.phNum = phNum;
        } else if (phNum.equals("")) {
            this.phNum = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your phone number has been entered incorrectly");
        }
    }

    /**
     * Gets the users country, city, street, house number etc as string
     * @return address
     */
    @OneToOne(cascade = CascadeType.ALL)
    //@Column(nullable = false)
    public Location getAddress() {
    return this.address;
    }

    /**
     * Sets the users home address
     * Not Null
     * @param address where the user lives/provides items from as a location object
     */
    public void setAddress(Location address) {
        if (address != null) {
            this.address = address;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your address has been entered incorrectly");
        }
    }

    /**
     * Date the account was created
     * @return Date the account was created
     */
    @ReadOnlyProperty
    public Instant getCreated(){
        return this.created;
    }

    /**
     * Record the date when the account is created
     * @param date of account creation
     */
    public void setCreated(Instant date){
        this.created = date;
    }

    /**
     * Gets the set of businesses this user is the primary owner of
     * @return Set of businesses owned
     */
    @OneToMany(mappedBy = "primaryOwner", fetch = FetchType.EAGER)
    public Set<Business> getBusinessesOwned() {
        return this.businessesOwned;
    }

    /**
     * Sets the set of businesses this user is the primary owner of
     * @param owned Businesses owned
     */
    private void setBusinessesOwned(Set<Business> owned) {
        this.businessesOwned = owned;
    }

    /**
     * Gets the set of businesses that the user is an admin of
     * @return Businesses administered
     */
    @ManyToMany(mappedBy = "administrators", fetch = FetchType.EAGER)
    public Set<Business> getBusinessesAdministered() {
        return this.businessesAdministered;
    }

    /**
     * For JPA only
     * Sets the businesses administered
     * @param businesses Set of businesses
     */
    private void setBusinessesAdministered(Set<Business> businesses) {
        this.businessesAdministered = businesses;
    }

    /**
     * Gets the set of businesses that the user is an admin of OR is the owner of
     * @return Businesses administered or owned
     */
    @Transient
    public Set<Business> getBusinessesAdministeredAndOwned() {
        Set<Business> mergedSet = new HashSet<>();
        mergedSet.addAll(getBusinessesOwned());
        mergedSet.addAll(getBusinessesAdministered());
        return mergedSet;
    }

    /**
     * ToString method override, helpful for testing
     * @return String representation of a user
     */
    @Override
    public String toString(){
        return String.format(
          "{id: %d, firstName: %s, lastName: %s}",
                this.getUserID(),
                this.firstName,
                this.lastName
        );
    }

    /**
     * This method constructs a JSON representation of the user's public details. These are their id number,
     * first name, middle name, last name, nickname, email, bio, the city/region/country part of their address,
     * the businesses they administer, and date the account was created. If fullBusienssDetails is true then
     * a JSON representation of each business they administer will be included, otherwise the businessesAdministered
     * field will not be present to avoid issues when nesting this json inside the administrators field of the business
     * json.
     * @param fullBusinessDetails A JSON representation of each business will be included in the businessesAdministered
     * field if this is set to true, otherwise the field will not be present.
     * @return JSONObject with attribute name as key and attribute value as value.
     */
    // Todo: Replace email with profile picture once profile pictures added.
    public JSONObject constructPublicJson(boolean fullBusinessDetails) {
        var object = new JSONObject();
        object.put("id",          getUserID());
        object.put("firstName",   getFirstName());
        object.put("lastName",    getLastName());
        object.put("email",       getEmail());
        object.put("created",     getCreated().toString());
        object.put("middleName",  getMiddleName());
        object.put("nickname",    getNickname());
        object.put("bio", getBio());
        object.put("homeAddress", getAddress().constructPartialJson());
        if (fullBusinessDetails) {
            object.put("businessesAdministered", constructBusinessJsonArray());
        }
        JsonTools.removeNullsFromJson(object);
        return object;
    }

    /**
     * Override the constructPublicJson method so that it defaults to omitting the businessesAdministered field.
     * @return A public JSON representation of the user without information on the businesses they administer
     */
    public JSONObject constructPublicJson() {
        return constructPublicJson(false);
    }

    /**
     * This method constructs a JSON representation of the user's private details. This includes all the values from
     * the public JSON, plus their full address, date of birth, phone number and role.
     * @param fullBusinessDetails A JSON representation of each business will be included in the businessesAdministered
     * field if this is set to true, otherwise the field will not be present.
     * @return JSONObject with attribute name as key and attribute value as value.
     */
    public JSONObject constructPrivateJson(boolean fullBusinessDetails) {
        JSONObject json = constructPublicJson(fullBusinessDetails);
        json.replace("homeAddress", getAddress().constructFullJson());
        json.appendField("dateOfBirth", dob.toString());
        json.appendField("phoneNumber", phNum);
        json.appendField("role", role);
        JsonTools.removeNullsFromJson(json);
        return json;
    }

     /**
     * Override the constructPrivateJson method so that it defaults omitting the businessesAdministered field
     * @return A private JSON representation of the user without information on the businesses they administer
     */
    public JSONObject constructPrivateJson() {
        return constructPrivateJson(false);
    }

    /**
     * Construct an array of JSON objects representing the businesses the user administers. The JSONs in the array
     * are sorted by the id number of the business to ensure consistency between subsequent requests.
     * @return An array of JSON representations of the businesses the user administers.
     */
    public JSONArray constructBusinessJsonArray() {
        List<Business> businesses = new ArrayList<>();
        businesses.addAll(getBusinessesAdministeredAndOwned());
        Collections.sort(businesses, (Business business1, Business business2) ->
            business1.getId().compareTo(business2.getId()));
        JSONArray businessArray = new JSONArray();
        for (Business business : businesses) {
            businessArray.add(business.constructJson());
        }
        return businessArray;
    }

    /**
     * Called before a user is removed from the database
     * Ensures that the User is not an owner of any Businesses.
     * If the User is an administrator for any businesses, they are removed from the administrator set for each business
     * @throws ResponseStatusException If User owns any businesses
     */
    @PreRemove
    public void preRemove() {
        if (!this.getBusinessesOwned().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete a user who is an owner of one or more businesses");
        }
        for (Business business : this.getBusinessesAdministered()) {
            business.removeAdmin(this);
        }
    }


    /**
     * This class uses the builder pattern to construct an instance of the User class
     */
    public static class Builder {

        private String firstName;
        private String middleName;
        private String lastName;
        private String nickname;
        private String email;
        private String bio;
        private LocalDate dob;
        private String phNum;
        private Location address;
        private String password;

        /**
         * Set the builder's first name. This field is required.
         * @param firstName a string representing the user's first name.
         * @return Builder with first name set.
         */
        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * Set the builder's middle name. This field is optional.
         * @param middleName a string representing the user's middle name.
         * @return Builder with middle name set.
         */
        public Builder withMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        /**
         * Set the builder's last name. This field is required.
         * @param lastName a string representing the user's last name.
         * @return Builder with last name set.
         */
        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        /**
         * Set the builder's nickname. This field is optional.
         * @param nickname a string representing the user's nickname.
         * @return Builder with nickname set.
         */
        public Builder withNickName(String nickname) {
            this.nickname = nickname;
            return this;
        }

        /**
         * Set the builder's email. This field is required.
         * @param email a string representing the user's first email.
         * @return Builder with email set.
         */
        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        /**
         * Set the builder's bio. This field is optional.
         * @param bio a string representing the user's bio.
         * @return Builder with bio set.
         */
        public Builder withBio(String bio) {
            this.bio = bio;
            return this;
        }

        /**
         * Set the builder's date of birth. This field is required.
         * @param dobString a string representing the user's date of birth in format yyyy-MM-dd.
         * @return Builder with date of birth set.
         */
        public Builder withDob(String dobString) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
            this.dob = LocalDate.parse(dobString, dateTimeFormatter);
            return this;
        }

        /**
         * Set the builder's phone number. This field is optional.
         * @param phoneNumber a string representing the user's phoneNumber.
         * @return Builder with phoneNumber set.
         */
        public Builder withPhoneNumber(String phoneNumber) {
            this.phNum = phoneNumber;
            return this;
        }

        /**
         * Set the builder's address. This field is required.
         * @param address a string representing the user's address.
         * @return Builder with address set.
         */
        public Builder withAddress(Location address) {
            this.address = address;
            return this;
        }

        /**
         * Set the builder's password. This field is required.
         * @param password a string representing the user's password.
         * @return Builder with password set.
         */
        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * Construct an instance of user using the attributes from the builder.
         * @return An instance of the user class with given attributes.
         */
        public User build() {
            User user = new User();
            user.setFirstName(this.firstName);
            user.setMiddleName(this.middleName);
            user.setLastName(this.lastName);
            user.setNickname(this.nickname);
            user.setEmail(this.email);
            user.setAuthenticationCodeFromPassword(this.password);
            user.setBio(this.bio);
            user.setDob(this.dob);
            user.setPhNum(this.phNum);
            user.setAddress(this.address);
            user.setCreated(Instant.now());
            user.setRole("user");
            return user;
        }

    }
}
