<template>
  <v-card max-width="1100px">
    <v-container fluid>
      <v-row>
        <!-- Image column -->
        <v-col align="center" justify="center" cols="auto" md="3" sm="12" style="width: 100%;" v-if="product.images.length === 0">
          <v-icon size="250">
            mdi-image
          </v-icon>
        </v-col>
        <v-col cols="auto" md="3" sm="12" v-else>
          <!-- feed the productImages into the carousel child component -->
          <ProductImageCarousel :productImages="product.images" :showControls="false"/>
        </v-col>

        <!-- Info column -->
        <v-col>
          <v-row>
            <v-col class="auto pa-0" md="10" sm="10">
              <v-card-title
                :class="{ 'pt-0': $vuetify.breakpoint.smAndDown }"
                class="pb-0"
              >
                <!-- shows product name -->
                {{ product.name }}
              </v-card-title>
            </v-col>
            <v-col cols="auto" md="2" sm="2">
              <v-card-actions
                :class="{ 'pt-0': $vuetify.breakpoint.smAndDown }"
                class="pb-0"
              />
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="auto" md="9" sm="12">
              <v-row>
                <!-- shows the product code -->
                <v-card-text
                  :class="{ 'pb-0': $vuetify.breakpoint.mdAndUp }"
                  class="product-fields"
                >
                  <strong>Product Code</strong>
                  <br>
                  {{ product.id }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- if the description length is more than or equal to 50 without slicing any words, the "Read more..." link will
                appear which will lead the user to the FullProductDescription component  -->
                <template v-if="product.description !== undefined">
                  <span v-if="product.description.length >= 50">
                    <v-card-text
                      id="description"
                      class="pb-0 product-fields"
                    >
                      <strong>Description</strong>
                      <br>
                      {{
                        product.description.replace(
                          /^([\s\S]{50}\S*)[\s\S]*/,
                          "$1"
                        )
                      }}...
                      <!-- feed the productDescription into the dialog box child component -->
                      <FullProductDescription
                        :productDescription="product.description"
                      />
                    </v-card-text>
                  </span>
                  <!-- else just show the product description -->
                  <span v-else>
                    <v-card-text class="pb-0 product-fields">
                      <strong>Description</strong>
                      <br>
                      {{ product.description }}
                    </v-card-text>
                  </span>
                </template>
              </v-row>
              <v-row>
                <!-- shows the product manufacturer -->
                <v-card-text
                  :class="{
                    'pb-0': $vuetify.breakpoint.smAndDown,
                  }"
                  class="product-fields"
                >
                  <strong>Manufacturer</strong>
                  <br>
                  {{ product.manufacturer }}
                </v-card-text>
              </v-row>
              <!-- On small screen merge all non-date info into a single column -->
              <template v-if="$vuetify.breakpoint.xsOnly">
                <v-row>
                  <!-- shows the product price -->
                  <v-card-text class="pb-0 product-fields">
                    <strong>Quantity</strong>
                    <br>
                    {{ inventoryItem.quantity }}
                  </v-card-text>
                </v-row>
                <v-row v-if="inventoryItem.pricePerItem !== undefined">
                  <!-- shows the product price -->
                  <v-card-text class="pb-0 product-fields">
                    <strong>Price per Item</strong>
                    <br>
                    {{ currency.symbol }}{{ inventoryItem.pricePerItem }} {{ currency.code }}
                  </v-card-text>
                </v-row>
                <v-row v-if="inventoryItem.totalPrice !== undefined">
                  <!-- shows the product price -->
                  <v-card-text class="pb-0 product-fields">
                    <strong>Total Price</strong>
                    <br>
                    {{ currency.symbol }}{{ inventoryItem.totalPrice }} {{ currency.code }}
                  </v-card-text>
                </v-row>
              </template>
            </v-col>
            <v-col cols="auto" md="3" sm="12" v-if="$vuetify.breakpoint.smAndUp">
              <v-row>
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Quantity</strong>
                  <br>
                  {{inventoryItem.remainingQuantity}}/{{ inventoryItem.quantity }}
                </v-card-text>
              </v-row>
              <v-row v-if="inventoryItem.pricePerItem !== undefined">
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Price per Item</strong>
                  <br>
                  {{ currency.symbol }}{{ inventoryItem.pricePerItem }} {{ currency.code }}
                </v-card-text>
              </v-row>
              <v-row v-if="inventoryItem.totalPrice !== undefined">
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Total Price</strong>
                  <br>
                  {{ currency.symbol }}{{ inventoryItem.totalPrice }} {{ currency.code }}
                </v-card-text>
              </v-row>
            </v-col>
            <!-- Narrow date view -->
            <v-col v-if="$vuetify.breakpoint.xsOnly" cols="auto" sm="12">
              <v-row>
                <v-card-text class="pb-0 product-fields">
                  <strong>Manufactured</strong>
                  <br>
                  <template v-if="inventoryItem.manufactured === undefined">
                    -
                  </template>
                  <template v-else>
                    {{ formatDate(inventoryItem.manufactured) }}
                  </template>
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text class="pb-0 product-fields">
                  <strong>Created</strong>
                  <br>
                  <template v-if="inventoryItem.created === undefined">
                    -
                  </template>
                  <template v-else>
                    {{ formatDate(inventoryItem.created) }}
                  </template>
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text class="pb-0 product-fields">
                  <strong>Sell By</strong>
                  <br>
                  <template v-if="inventoryItem.sellBy === undefined">
                    -
                  </template>
                  <template v-else>
                    {{ formatDate(inventoryItem.sellBy) }}
                  </template>
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text class="pb-0 product-fields">
                  <strong>Best Before</strong>
                  <br>
                  <template v-if="inventoryItem.bestBefore === undefined">
                    -
                  </template>
                  <template v-else>
                    {{ formatDate(inventoryItem.bestBefore) }}
                  </template>
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text class="pb-0 product-fields">
                  <strong>Expiry</strong>
                  <br>
                  <template v-if="inventoryItem.expires === undefined">
                    -
                  </template>
                  <template v-else>
                    {{ formatDate(inventoryItem.expires) }}
                  </template>
                </v-card-text>
              </v-row>
            </v-col>
          </v-row>
        </v-col>
        <!-- Timeline column -->
        <v-col cols="auto" >
          <v-timeline
            clipped
            class="timeline"
            v-if="$vuetify.breakpoint.smAndUp"
          >
            <v-timeline-item v-if="inventoryItem.manufactured" small color="green" right>
              <template v-slot:opposite>
                <div class="timeline-label">Manufactured</div>
              </template>
              <div class="timeline-content">
                {{ formatDate(inventoryItem.manufactured) }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="product.created" small color="grey" right>
              <template v-slot:opposite>
                <div class="timeline-label">Created</div>
              </template>
              <div class="timeline-content">
                {{ formatDate(product.created) }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="inventoryItem.sellBy" small color="yellow" right>
              <template v-slot:opposite>
                <div class="timeline-label">Sell By</div>
              </template>
              <div class="timeline-content">
                {{ formatDate(inventoryItem.sellBy) }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="inventoryItem.bestBefore" small color="orange" right>
              <template v-slot:opposite>
                <div class="timeline-label">Best Before</div>
              </template>
              <div class="timeline-content">
                {{ formatDate(inventoryItem.bestBefore) }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="inventoryItem.expires" small color="red" f right>
              <template v-slot:opposite>
                <div class="timeline-label">Expiry</div>
              </template>
              <div class="timeline-content">
                {{ formatDate(inventoryItem.expires) }}
              </div>
            </v-timeline-item>
          </v-timeline>
        </v-col>
      </v-row>
      <v-row justify="end">
        <v-tooltip top>
          <template #activator="{on: tooltip}">
            <v-btn
              ref="createSaleItemButton"
              rounded
              outlined
              v-on="tooltip"
              color="primary"
              class="sale-item-but"
              @click="viewCreateSaleItem"
            >
              Create Sale Item
            </v-btn>
          </template>
          <span>Create a Sale from this inventory item</span>
        </v-tooltip>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
//This component requires two other custom components, one to display the product image, one to view more of the product's description
import FullProductDescription from "../utils/FullProductDescription.vue";
import ProductImageCarousel from "../utils/ProductImageCarousel.vue";
import { currencyFromCountry } from "@/api/currency";
import { formatDate } from '@/utils';

export default {
  name: "InventoryItem",
  props: {
    inventoryItem: Object,
    //retrieved from Inventory page
    businessId: Number
  },
  components: {
    FullProductDescription,
    ProductImageCarousel,
  },
  data() {
    return {
      currency: {},
      showImageUploaderForm: false,
      //If readMoreActivated is false, the product description is less than 50 words, so it wont have to use the FullProductDescription
      //component. Else it will use it and the "Read more..." link will also be shown to lead to the FullProductDescription component
      readMoreActivated: false
    };
  },
  async created() {
    // When the catalogue item is created, the currency will be set to the currency of the country the product is being
    // sold in. It will have blank fields if no currency can be found from the country.
    this.currency = await currencyFromCountry(this.product.countryOfSale);
  },
  computed: {
    /**
     * Helper property for getting the product out of the inventoryItem
     */
    product() {
      return this.inventoryItem.product;
    },
  },
  methods: {
    //if the "Read more..." link if clicked, readMoreActivated becomes true and the FullProductDescription dialog box will open
    activateReadMore() {
      this.readMoreActivated = true;
    },

    placeholderIfEmpty(text) {
      if (text === undefined) {
        return '-';
      }
      return text;
    },

    /**
     * Shows the create Sale Item dialog
     */
    viewCreateSaleItem() {
      this.$store.commit('showCreateSaleItem', {businessId: this.businessId, inventoryItem: this.inventoryItem});
    },
    formatDate,
  },
};
</script>

<style>
.product-fields {
  padding-top: 0;
}

.timeline {
  height: 100%;
}

.timeline-content {
  margin-left: -20px;
}

.timeline-label {
  width: 90px;
  font-weight: bold;
}

.sale-item-but {
  margin: 10px;
  font-weight: bold;
}

</style>
