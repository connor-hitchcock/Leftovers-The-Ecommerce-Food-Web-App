<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <v-col align="center" justify="center" cols="auto" md="3" sm="12" v-if="product.images.length === 0">
          <v-icon size="250">
            mdi-image
          </v-icon>
        </v-col>
        <v-col cols="auto" md="3" sm="12" v-else>
          <!-- feed the productImages into the carousel child component -->
          <ProductImageCarousel
            :productImages="product.images"
            :showControls="true"
            v-on:change-primary-image="setPrimaryImage"
            @delete-image="deleteImage"
          />
        </v-col>
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
              >
                <!-- shows the edit button for editing product details, which supposedly links to a form -->
                <a @click="showImageUploaderForm=true">Upload Image</a>
                <!-- cant mutate parent props directly, so no point to send a prop to the child -->
                <ProductImageUploader
                  :businessId="businessId"
                  :productCode="product.id"
                  v-model="showImageUploaderForm"
                  @image-added="imageAdded"
                />
              </v-card-actions>
            </v-col>
          </v-row>
          <v-row />
          <v-row>
            <v-col cols="auto" md="9" sm="12">
              <v-row>
                <!-- if the description length is more than or equal to 50 without slicing any words, the "Read more..." link will
                appear which will lead the user to the FullProductDescription component  -->
                <span v-if="description.length >= 50">
                  <v-card-text
                    id="description"
                    class="pb-0 product-fields"
                  >
                    <strong>Description: </strong>
                    <br >
                    {{
                      description.replace(
                        /^([\s\S]{50}\S*)[\s\S]*/,
                        "$1"
                      )
                    }}...
                    <!-- feed the productDescription into the dialog box child component -->
                    <FullProductDescription
                      :productDescription="description"
                    />
                  </v-card-text>
                </span>
                <!-- else just show the product description -->
                <span v-else>
                  <v-card-text class="pb-0 product-fields">
                    <strong>Description: </strong>
                    <br >
                    {{ description }}
                  </v-card-text>
                </span>
              </v-row>
              <v-row>
                <!-- shows the date added for the product -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Date Added: </strong>
                  <br >
                  {{ formatDate(product.created) }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product manufacturer -->
                <v-card-text
                  :class="{
                    'pb-0': $vuetify.breakpoint.smAndDown,
                  }"
                  class="product-fields"
                >
                  <strong>Manufacturer: </strong>
                  <br >
                  {{ manufacturer }}
                </v-card-text>
              </v-row>
            </v-col>
            <v-col cols="auto" md="3" sm="12">
              <v-row>
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>RRP: </strong>
                  <br >
                  {{ recommendedRetailPrice }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product code -->
                <v-card-text
                  :class="{ 'pb-0': $vuetify.breakpoint.mdAndUp }"
                  class="product-fields"
                >
                  <strong>Product Code: </strong>
                  <br >
                  {{ product.id }}
                </v-card-text>
              </v-row>
            </v-col>
          </v-row>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
//This component requires two other custom components, one to display the product image, one to view more of the product's description
import FullProductDescription from "../utils/FullProductDescription.vue";
import ProductImageCarousel from "../utils/ProductImageCarousel.vue";
import { currencyFromCountry } from "@/api/currency";
import ProductImageUploader from "../utils/ProductImageUploader";
import { makeImagePrimary, deleteImage } from "@/api/internal";
import { formatDate } from '@/utils';

export default {
  name: "ProductCatalogueItem",
  props: {
    product: Object,
    //retrieved from ProductCatalogue
    businessId: Number
  },
  components: {
    FullProductDescription,
    ProductImageCarousel,
    ProductImageUploader
  },
  data() {
    return {
      currency: {
        code: "",
        symbol: ""
      },
      showImageUploaderForm: false,
      //If readMoreActivated is false, the product description is less than 50 words, so it wont have to use the FullProductDescription
      //component. Else it will use it and the "Read more..." link will also be shown to lead to the FullProductDescription component
      readMoreActivated: false
    };
  },
  computed: {
    description() {
      return this.product.description || "Not set";
    },
    recommendedRetailPrice() {
      if (!this.product.recommendedRetailPrice) {
        return "Not set";
      }
      return this.currency.symbol + this.product.recommendedRetailPrice + " " + this.currency.code;
    },
    manufacturer() {
      return this.product.manufacturer || "Not set";
    },
  },
  async created() {
    // When the catalogue item is created, the currency will be set to the currency of the country the product is being
    // sold in. It will have blank fields if no currency can be found from the country.
    this.currency = await currencyFromCountry(this.product.countryOfSale);
  },
  methods: {
    //if the "Read more..." link if clicked, readMoreActivated becomes true and the FullProductDescription dialog box will open
    activateReadMore() {
      this.readMoreActivated = true;
    },
    closeDialog() {
      this.showImageUploaderForm = false;
    },
    /**
     * Sets the currently selected image as the primary image.
     * @param imageId Id of the currently selected image
     */
    async setPrimaryImage(imageId) {
      let response = await makeImagePrimary(this.businessId, this.product.id, imageId);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }
      this.$emit('content-changed');
    },

    /**
     * Deletes the provided image
     * @param imageId Image to delete
     */
    async deleteImage(imageId) {
      let response = await deleteImage(this.businessId, this.product.id, imageId);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }
      this.$emit('content-changed');
    },
    /**
     * Handler for when a new image is added
     */
    async imageAdded() {
      this.$emit('content-changed');
    },
    formatDate,
  },
};

</script>

<style scoped>
.product-fields {
    padding-top: 0;
}
</style>
