<template>
  <div>
    <v-container fluid>
      <v-row align="center">
        <v-col
          class="d-flex"
          cols="auto"
        >
          <!---Select component for the order in which the cards should be displayed--->
          <v-select
            v-model="orderBy"
            flat
            solo-inverted
            hide-details
            :items="[
              { text: 'Date Added', value: 'created'},
              { text: 'Date Closing', value: 'closes'},
              { text: 'Title', value: 'title'},
              { text: 'Author First Name', value: 'creatorFirstName'},
              { text: 'Author Last Name', value: 'creatorLastName'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
        </v-col>
        <v-col cols="auto">
          <!---Reverse the order in which the cards should be displayed--->
          <v-btn-toggle class="toggle" v-model="reverse" mandatory>
            <v-btn depressed color="primary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col
          class="d-flex"
          cols="auto"
        >
          <!---Search for cards by their keywords--->
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Keywords"
            autofocus
          />
        </v-col>
        <v-spacer/>
        <v-col cols="auto" class="text-right" >
          <!---Link to modal for creating new card--->
          <v-btn type="button" color="primary" @click="showCreateCard" rounded>
            Create card
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
    >
      {{ error }}
    </v-alert>
    <v-tabs
      v-model="tab"
      grow
    >
      <!---Tabs for dividing marketplace into for sale, wanted and exchange sections--->
      <v-tab
        v-for="section in sectionNames"
        :key="section"
      >
        {{ section }}
      </v-tab>
    </v-tabs>

    <v-tabs-items v-model="tab">
      <v-tab-item
        v-for="section in sections"
        :key="section"
      >
        <!---Grid of cards for one section--->
        <v-container class="grey lighten-2">
          <v-row>
            <v-col v-for="card in cards[section]" :key="card.id" cols="12" sm="6" md="4" lg="3">
              <MarketplaceCard :content="card"/>
            </v-col>
          </v-row>
        </v-container>
        <v-pagination
          v-model="currentPage[section]"
          :length="totalPages(section)"
          circle
        />
        <!--Text to display range of results out of total number of results-->
        <v-row justify="center" no-gutters>
          {{ resultsMessage(section) }}
        </v-row>
      </v-tab-item>
    </v-tabs-items>

  </div>
</template>

<script>
import MarketplaceCard from "../cards/MarketplaceCard";
import {getMarketplaceCardsBySection, getMarketplaceCardCount} from "../../api/internal.ts";

export default {
  data() {
    return {
      tab: null,
      sectionNames: ["For Sale", "Wanted", "Exchange"],
      sections: ["ForSale", "Wanted", "Exchange"],
      cards: {
        ForSale: [],
        Wanted: [],
        Exchange: []
      },
      currentPage: {
        ForSale: 1,
        Wanted: 1,
        Exchange: 1,
      },
      /**
       * Number of results per a result page
       */
      resultsPerPage: 8,
      /**
       * Total number of results for all pages
       * For now, it is hard coded to suit the above aesthetic. once the api method to retrieve the count is created, it can
       * be replaced with dynamic values.
       */
      totalResults: {
        ForSale: 0,
        Wanted: 0,
        Exchange: 0,
      },
      error: "",
      orderBy: "created",
      /**
       * Note: change the default here to true because backlog states that
       * creation date should be descending by default.
       */
      reverse: true
    };
  },
  methods: {
    /**
     * Iterates through the 3 sections and gets all the cards and card count
     */
    async updateResults() {
      this.error = undefined;
      for (const index in this.sections) {
        const value = await getMarketplaceCardsBySection (
          this.sections[index],
          this.currentPage[this.sections[index]],
          this.resultsPerPage,
          this.orderBy,
          this.reverse
        );
        this.totalResults[this.sections[index]] = await getMarketplaceCardCount(this.sections[index]);
        if (typeof value === 'string') {
          this.cards[this.sections[index]] = [];
          this.error = value;
        } else {
          this.cards[this.sections[index]] = value;
        }
      }

    },
    showCreateCard() {
      this.$store.commit('showCreateMarketplaceCard', this.$store.state.user);
    },
    /**
     * The total number of pages required to show all the users
     * May be 0 if there are no results
     */
    totalPages (section) {
      return Math.ceil(this.totalResults[section] / this.resultsPerPage);
    },
    /**
     * The message displayed at the bottom of the page to show how many results there are
     */
    resultsMessage(section) {
      if (this.cards[section].length === 0) return 'There are no results to show';

      const pageStartIndex = (this.currentPage[section] - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.cards[section].length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults[section]} results`;
    },
  },
  components: {
    MarketplaceCard
  },
  watch: {
    orderBy() {
      this.updateResults();
    },
    reverse() {
      this.updateResults();
    },
    currentPage: {
      handler() {
        this.updateResults();
      },
      //this will enable the watcher to watch nested data
      deep: true
    },
    resultsPerPage() {
      this.updateResults();
    },
  },
  async created() {
    await this.updateResults();
  },
};
</script>
