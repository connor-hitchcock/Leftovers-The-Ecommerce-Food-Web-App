<template>
  <v-card width="600px" style="margin: 1em">
    <v-row>
      <v-col cols="8">
        <v-expand-transition>
          <div v-show="!moreInfo">
            <ProductImageCarousel :productImages="product.images" :productId="product.id" />
            <v-card-title>{{ saleItem.quantity + " × " + product.name }}</v-card-title>
            <v-card-subtitle>{{ retailPrice }}</v-card-subtitle>

          </div>
        </v-expand-transition>
        <v-expand-transition>
          <div v-show="moreInfo">
            <v-divider/>
            <v-card-subtitle>
              Product Description
            </v-card-subtitle>
            <v-card-text v-if="productDescription.length >= 50">
              {{productDescription.slice(0, 50) + "..."}}
              <FullProductDescription ref="fullProductDescription" :product-description="productDescription"/>
            </v-card-text>
            <v-card-text v-else>
              {{productDescription}}
            </v-card-text>
            <div ref="sellerInfo" v-if="saleItem.moreInfo !== undefined && saleItem.moreInfo.length > 0">
              <v-card-subtitle>
                Additional Sale Info
              </v-card-subtitle>
              <v-card-text v-if="saleItem.moreInfo.length >= 50">
                {{saleItem.moreInfo.slice(0,50)}}
                <FullProductDescription ref="fullMoreInfo" :product-description="saleItem.moreInfo"/>
              </v-card-text>
              <v-card-text v-else>
                {{saleItem.moreInfo}}
              </v-card-text>
            </div>
          </div>
        </v-expand-transition>
      </v-col>
      <v-col cols="4">
        <v-timeline dense style="height: 100%; margin-left: -40%; margin-bottom: 10px">
          <v-timeline-item color="grey" small>
            <div style="margin-left: -25px">
              <strong>Created</strong>
              {{createdFormatted}}
            </div>
          </v-timeline-item>
          <v-timeline-item color="orange" small>
            <div style="margin-left: -25px">
              <strong>Expires</strong>
              {{expiresFormatted}}
            </div>
          </v-timeline-item>
          <v-timeline-item color="red" small>
            <div style="margin-left: -25px">
              <strong>Closes</strong>
              {{closesFormatted}}
            </div>
          </v-timeline-item>
        </v-timeline>
        <v-card-actions>
          <v-btn ref="viewMoreButton" style="position: absolute; bottom: 10px; right: 10px" color="secondary" @click="moreInfo=!moreInfo">View {{moreInfo? 'Less' : 'More'}}</v-btn>
        </v-card-actions>
      </v-col>
    </v-row>
  </v-card>
</template>

<script>
import ProductImageCarousel from "@/components/utils/ProductImageCarousel";
import FullProductDescription from "@/components/utils/FullProductDescription";
import { currencyFromCountry } from "@/api/currency";
import { formatDate } from '@/utils';

export default {
  name: "SaleItem",
  components: {FullProductDescription, ProductImageCarousel},
  data() {
    return {
      moreInfo: false,
      currency: {
        code: "",
        symbol: ""
      },
    };
  },
  props: {
    saleItem: Object,
    businessId: Number,
  },
  computed: {
    /**
     * Easier access to the product for this sale
     * @returns the product
     */
    product() {
      return this.saleItem.inventoryItem.product;
    },
    /**
     * Easier access to the inventory item for this sale
     * @returns Inventory item
     */
    inventoryItem() {
      return this.saleItem.inventoryItem;
    },
    /**
     * Creates a nicely formatted readable string for the sales creation date
     * @returns {string} CreatedDate
     */
    createdFormatted() {
      let date = new Date(this.saleItem.created);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted readable string for the sales expiry date
     * @returns {string} ExpiryDate
     */
    expiresFormatted() {
      let date = new Date(this.saleItem.inventoryItem.expires);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted readable string for the sales close date
     * @returns {string} CloseDate
     */
    closesFormatted() {
      let date = new Date(this.saleItem.closes);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted retail price, including the currency
     * @returns {string} RetailPrice
     */
    retailPrice() {
      if (!this.saleItem.price) {
        return "Not set";
      }
      return this.currency.symbol + this.saleItem.price + " " + this.currency.code;
    },
    productDescription() {
      return this.product.description || "Not set";
    }
  },
  async created() {
    // When the Sale item is created, the currency will be set to the currency of the country the product is being
    // sold in. It will have blank fields if no currency can be found from the country.
    this.currency = await currencyFromCountry(this.product.countryOfSale);
  }
};
</script>

<style scoped>

</style>