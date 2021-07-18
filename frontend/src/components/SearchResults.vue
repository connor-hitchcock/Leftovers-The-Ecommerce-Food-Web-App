<template>
  <div>
    <v-toolbar dark color="primary" class="mb-1">
      <!-- Temporary stand in for search bar component -->
      <v-text-field
        clearable
        flat
        solo-inverted
        hide-details
        v-model="searchQuery"
        prepend-inner-icon="mdi-magnify"
        label="Search"
        autofocus
      />
      <v-spacer/>
      <v-select
        v-model="orderBy"
        flat
        solo-inverted
        hide-details
        :items="[
          { text: 'Relevance',   value: 'relevance'  },
          { text: 'User ID',     value: 'userId'     },
          { text: 'First Name',  value: 'firstName'  },
          { text: 'Middle Name', value: 'middleName' },
          { text: 'Last Name',   value: 'lastName'   },
          { text: 'Nickname',    value: 'nickname'   },
          { text: 'Email',       value: 'email'      },
        ]"
        prepend-inner-icon="mdi-sort-variant"
        label="Sort by"
      />
      <v-btn-toggle class="toggle" v-model="reverse" mandatory>
        <v-btn depressed color="primary" :value="false">
          <v-icon>mdi-arrow-up</v-icon>
        </v-btn>
        <v-btn depressed color="primary" :value="true">
          <v-icon>mdi-arrow-down</v-icon>
        </v-btn>
      </v-btn-toggle>
    </v-toolbar>

    <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
    >
      {{ error }}
    </v-alert>
    <v-list three-line>
      <!--users would produce the results for each page, and then it will show each result with
      SearchResultItem-->
      <template v-for="(user, index) in users">
        <v-divider v-if="user === undefined" :key="'divider-'+index"/>
        <SearchResultItem v-else :key="user.id" :user="user"/>
      </template>
    </v-list>
    <!--paginate results-->
    <v-pagination
      v-model="currentPage"
      :length="totalPages"
      circle
    />
    <!--Text to display range of results out of total number of results-->
    <v-row justify="center" no-gutters>
      {{ resultsMessage }}
    </v-row>
  </div>
</template>

<script>
import SearchResultItem from './cards/SearchResultItem';
import { getSearchCount, search } from '../api/internal';
import { debounce } from '../utils';

export default {
  name: 'SearchResults',
  data() {
    return {
      /**
       * The contents of the search box.
       */
      searchQuery: this.$route.query.query || '',
      /**
       * Current query that is being searched.
       */
      searchedQuery: undefined,
      /**
       * The search response data for this page.
       */
      users: [],
      /**
       * Current error message string.
       * If undefined then there is no error.
       */
      error: undefined,
      /**
       * Whether to reverse the search order
       */
      reverse: false,
      /**
       * The current search result order
       */
      orderBy: 'relevance',
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 10,
      /**
       * Total number of returned results
       */
      totalResults: 0,
      /**
       * Function that is called whenever the "searchQuery" variable is updated.
       * This function is rate limited to avoid too many queries to the backend.
       */
      debouncedUpdateQuery: debounce(this.updateQuery, 500),
    };
  },

  computed: {
    /**
     * The total number of pages required to show all the users
     * May be 0 if there are no results
     */
    totalPages () {
      return Math.ceil(this.totalResults / this.resultsPerPage);
    },
    /**
     * The message displayed at the bottom of the page to show how many results there are
     */
    resultsMessage() {
      if (this.users.length === 0) return 'There are no results to show';

      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.users.length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
  },
  methods: {
    /**
     * This function gets called when the search query is changed.
     */
    async updateQuery() {
      if (!this.searchQuery) return; // If the current search query is empty, do not search

      this.searchedQuery = this.searchQuery;
      this.currentPage = 1; // Makes sure we start on the first page
      await this.updateNotQuery();

      // Sets an initial estimate for the total number of results
      this.totalResults = this.users.length;

      if (this.users.length >= this.resultsPerPage) {
        // If we have at least a page worth of results, check if we have any more results.
        let count = await getSearchCount(this.searchQuery);
        if (typeof count === 'string') {
          this.error = count;
        } else {
          this.totalResults = count;
        }
      }
    },

    /**
     * This function gets called when the search results need to change, but the search query has not changed.
     * The page index, results per page, order by and reverse variables notify this function.
     */
    async updateNotQuery() {
      if (!this.searchedQuery) return; // If the current search query is empty, do not search
      const value = await search (
        this.searchedQuery,
        this.currentPage,
        this.resultsPerPage,
        this.orderBy,
        this.reverse
      );
      if (typeof value === 'string') {
        this.users = [];
        this.error = value;
      } else {
        this.users = value;
        this.error = undefined;
      }
    },
  },

  watch: {
    searchQuery: {
      handler() {
        this.debouncedUpdateQuery();
      },
      immediate: true,
    } ,
    orderBy() {
      this.updateNotQuery();
    },
    reverse() {
      this.updateNotQuery();
    },
    currentPage() {
      this.updateNotQuery();
    },
    resultsPerPage() {
      this.updateNotQuery();
    },
    totalPages() {
      // Ensures that the current page is at least 1 and less than or equal to the total number of pages.
      this.currentPage = Math.max(Math.min(this.currentPage, this.totalPages), 1);
    }
  },

  components: {
    SearchResultItem,
  },
};
</script>

<style scoped>
.toggle {
  margin-left: 10px;
}
</style>