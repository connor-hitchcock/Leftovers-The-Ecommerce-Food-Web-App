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
            flat
            v-model="orderBy"
            solo-inverted
            hide-details
            :items="[
              { text: 'Date Added', value: 'created'},
              { text: 'Closing Date', value: 'closing'},
              { text: 'Product Name', value: 'productName'},
              { text: 'Sale Quantity', value: 'quantity'},
              { text: 'Sale Price', value: 'price'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
        </v-col>
        <v-col cols="auto">
          <!---Reverse the order in which the cards should be displayed--->
          <v-btn-toggle class="toggle" mandatory v-model="reversed">
            <v-btn depressed color="primary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
      </v-row>
    </v-container>
    <v-container class="grey lighten-2">
      <v-row justify="space-between">
        <v-col v-for="sale in salesList" v-bind:key="sale.id" cols="auto">
          <SaleItem :business-id="businessId" :sale-item="sale" />
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-pagination
          v-model="page"
          :length="totalPages"
        />
      </v-row>
    </v-container>
  </div>
</template>

<script>
import {getBusinessSales, getBusinessSalesCount} from "@/api/internal";
import SaleItem from "@/components/cards/SaleItem";

export default {
  name: "SalePage",
  components: {SaleItem},
  data() {
    return {
      salesList: [],
      page: 1,
      totalCount: 1,
      resultsPerPage: 6,
      reversed: false,
      orderBy: 'created'
    };
  },
  methods: {
    /**
     * Populates the page with sales
     * Called on creation and whenever sorting or pagination updated
     * @returns {Promise<void>}
     */
    async populateSales() {
      const result = await getBusinessSales(this.businessId, this.page, this.resultsPerPage, this.orderBy, this.reversed);
      if (typeof result === 'string') {
        this.$store.commit('setError', result);
      } else {
        this.salesList = result;
      }
    },
    /**
     * Gets the total count of Sales for the given business
     * Used for pagination
     * @returns {Promise<void>}
     */
    async getTotalCount() {
      const result = await getBusinessSalesCount(this.businessId);
      if (typeof result === 'string') {
        this.$store.commit('setError', result);
      } else {
        this.totalCount = result;
      }
    }
  },
  computed: {
    /**
     * The ID of the current business
     * @returns {number}
     */
    businessId() {
      return parseInt(this.$route.params.id);
    },
    /**
     * The total number of pages
     * @returns {number}
     */
    totalPages() {
      return Math.ceil(this.totalCount / this.resultsPerPage);
    }
  },
  async created() {
    this.$store.commit('clearError');
    await this.getTotalCount();
    await this.populateSales();
  },
  watch: {
    reversed() {
      this.populateSales();
    },
    orderBy() {
      this.populateSales();
    },
    page() {
      this.populateSales();
    }
  }
};
</script>
<style scoped>

</style>