package org.seng302.entities;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.seng302.tools.JsonTools;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class MarketplaceCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Section section;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Instant created;

    @Column(nullable = false)
    private Instant closes;

    @ManyToMany
    @JoinTable(name = "card_keywords")
    private List<Keyword> keywords = new ArrayList<>();


    /**
     * Gets the id (will be unique among marketplace cards)
     * @return card id
     */
    public Long getID() {
        return id;
    }

    /**
     * Gets the creator of this card
     * @return creator user
     */
    public User getCreator() {
        return creator;
    }

    /**
     * Gets the section of this card
     * @return card section
     */
    public Section getSection() {
        return section;
    }

    /**
     * Gets the title of this card
     * @return card title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the moment this card was created
     * @return creation date and time
     */
    public Instant getCreated() { return created; }

    /**
     * Gets the moment this card will close
     * @return closing date and time
     */
    public Instant getCloses() { return closes; }

    /**
     * @return Card description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Cards keywords
     */
    public List<Keyword> getKeywords() {return this.keywords;}

    /**
     * Sets the card section
     * @param section New section
     */
    public void setSection(Section section) {
        this.section = section;
    }

    /**
     * Sets and validates the card title
     * @param title New title
     */
    public void setTitle(String title) {
        if (title == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card title must be provided");
        }
        if (title.isEmpty() || title.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card title must be between 1-50 characters long");
        }
        if (!title.matches("^[ \\d\\p{Punct}\\p{L}]*$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card title must only contain letters, numbers, spaces and punctuation");
        }
        this.title = title;
    }

    /**
     * Sets and validates the card description
     * @param description New description
     */
    public void setDescription(String description) {
        if (description == null || description.isEmpty()) {
            this.description = null;
            return;
        }
        if (description.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card description must not be longer than 200 characters");
        }
        if (!description.matches("^[\\p{Space}\\d\\p{Punct}\\p{L}]*$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card description must only contain letters, numbers, whitespace and punctuation");
        }
        this.description = description;
    }

    /**
     * Sets and validates the card closing date and time
     * @param closes New closing date and time
     */
    public void setCloses(Instant closes) {
        if (closes == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing time cannot be null");
        }
        if (closes.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing time cannot be before creation");
        }
        this.closes = closes;
    }

    /**
     * Adds and validates a keyword to this Marketplace Card
     * @param keyword Keyword to add to card
     */
    public void addKeyword(Keyword keyword) {
        if (keyword == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keyword cannot be null");
        }
        keywords.add(keyword);
    }

    /**
     * Constructs the JSON representation of this card
     * @return A JSONObject containing this cards data
     */
    public JSONObject constructJSONObject() {
        JSONObject json = new JSONObject();

        json.appendField("id", this.getID());
        json.appendField("creator", this.creator.constructPublicJson());
        json.appendField("section", this.section.getName());
        json.appendField("created", this.created);
        json.appendField("displayPeriodEnd", this.closes);
        json.appendField("title", this.title);
        json.appendField("description", this.description);

        JSONArray keywordArray = new JSONArray();
        // jsonify the keywords
        for (Keyword keyword : this.getKeywords()) {
            keywordArray.appendElement(keyword.constructJSONObject());
        }
        json.appendField("keywords", keywordArray);
        JsonTools.removeNullsFromJson(json);
        return json;
    }

    /**
     * Creates a string representation of the marketplace card
     * @return string representation
     */
    @Override
    public String toString() {
        return "MarketplaceCard{" +
                "id=" + id +
                ", creator=" + creator +
                ", section=" + section +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", created=" + created +
                ", closes=" + closes +
                '}';
    }

    /**
     * Valid marketplace card sections
     */
    public enum Section {
        FOR_SALE("ForSale"),
        WANTED("Wanted"),
        EXCHANGE("Exchange");

        private final String name;

        Section(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the section.
         * Same as in api spec
         * @return section name
         */
        public String getName() {
            return this.name;
        }
    }

    /**
     * Given a string, returns the matching section Enum
     * @param sectionName The section name to get
     * @return Matching section or ResponseStatusException if none
     */
    public static Section sectionFromString(String sectionName) {
        for (Section possibleSection : Section.values()) {
            if (possibleSection.getName().equals(sectionName)) {
                return possibleSection;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid section name");
    }

    /**
     * This class uses the builder pattern to construct an instance of the MarketplaceCard class
     */
    public static class Builder {
        private User creator;
        private Section section;
        private String title;
        private String description;
        private Instant closes;
        private List<Keyword> keywords = new ArrayList<>();

        /**
         * Sets the builder's creator.
         *
         * @param creator Creator for the marketplace card
         * @return Builder with the creator set
         */
        public Builder withCreator(User creator) {
            this.creator = creator;
            return this;
        }

        /**
         * Sets the builder's section.
         *
         * @param section Section for the marketplace card
         * @return Builder with the section set
         */
        public Builder withSection(Section section) {
            this.section = section;
            return this;
        }

        /**
         * Sets the builder's section.
         *
         * @param sectionName Name of the section for the marketplace card
         * @return Builder with the section set
         */
        public Builder withSection(String sectionName) {
            this.section = MarketplaceCard.sectionFromString(sectionName);
            return this;
        }

        /**
         * Sets the builder's title.
         *
         * @param title Title for the marketplace card
         * @return Builder with the title set
         */
        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the builder's description.
         *
         * @param description Description for the marketplace card
         * @return Builder with the description set
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the builder's close date and time.
         * If no closing date is provided then a two week interval after the creation moment is used.
         *
         * @param closes Creator for the marketplace card
         * @return Builder with the closing date and time set
         */
        public Builder withCloses(Instant closes) {
            this.closes = closes;
            return this;
        }

        /**
         * Adds a single keyword to this builder
         * @param keyword keyword to add
         * @return Builder with keyword added
         */
        public Builder addKeyword(Keyword keyword) {
            keywords.add(keyword);
            return this;
        }

        /**
         * Adds all the keywords in the keyword collection to this builder
         * @param keywords keywords to add
         * @return Builder with keywords added
         */
        public Builder addKeywords(Collection<Keyword> keywords) {
            this.keywords.addAll(keywords);
            return this;
        }

        /**
         * Builds the marketplace card
         * @return Newly created marketplace card
         */
        public MarketplaceCard build() {
            var card = new MarketplaceCard();

            if (creator == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card creator not provided");
            }
            card.creator = creator;
            card.setSection(section);
            card.setTitle(title);
            card.setDescription(description);
            card.created = Instant.now();
            if (closes == null) {
                card.setCloses(card.created.plus(14, ChronoUnit.DAYS));
            } else {
                card.setCloses(closes);
            }
            for (Keyword keyword : keywords) {
                card.addKeyword(keyword);
            }
            return card;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketplaceCard that = (MarketplaceCard) o;
        return id.equals(that.id) && section == that.section &&
                title.equals(that.title) &&
                Objects.equals(description, that.description) &&
                ChronoUnit.SECONDS.between(this.created, that.created) == 0 &&
                ChronoUnit.SECONDS.between(this.closes, that.closes) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
