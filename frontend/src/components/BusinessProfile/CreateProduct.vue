<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >
      <v-form
        v-model="valid"
      >
        <v-card>
          <v-card-title>
            <span class="headline create-product">Create new Product</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col>
                  <v-text-field
                    class="required"
                    v-model="productCode"
                    label="Short-hand Product Code"
                    outlined
                    :rules="mandatoryRules.concat(productCodeRules)"
                    ref="productCodeField"
                  />
                </v-col>
                <v-col>
                  <v-text-field
                    class="required"
                    v-model="product"
                    label="Name of product"
                    :rules="mandatoryRules.concat(maxCharRules).concat(validCharactersSingleLineRules)"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-textarea
                    v-model="description"
                    label="Description"
                    :rules="maxCharDescriptionRules.concat(validCharactersMultiLineRules)"
                    rows="3"
                    outlined
                  />
                </v-col>
                <v-col cols="6">
                  <v-text-field
                    v-model="manufacturer"
                    label="Manufacturer"
                    :rules="maxCharManufacturerRules.concat(validCharactersSingleLineRules)"
                    outlined
                  />
                </v-col>
                <v-col cols="6">
                  <v-text-field
                    v-model="recommendedRetailPrice"
                    label="Recommended Retail Price"
                    :prefix="currency.symbol"
                    :suffix="currency.code"
                    :rules="priceRules"
                    :hint="currency.errorMessage"
                    outlined
                  />
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer/>
            <div class="error-text" v-if="errorMessage !== undefined"> {{ errorMessage }} </div>
            <v-spacer/>
            <v-btn
              color="primary"
              text
              @click="closeDialog">
              Close
            </v-btn>
            <v-btn
              type="submit"
              color="primary"
              :disabled="!valid"
              :loading="isLoading"
              @click.prevent="createProduct">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>
../../internal
<script>
import {createProduct, getBusiness} from '@/api/internal';
import {currencyFromCountry} from "@/api/currency";
export default {
  name:'CreateProduct',
  data() {
    return {
      dialog: true,
      productCode: '',
      product: '',
      description: '',
      manufacturer: '',
      recommendedRetailPrice: '',
      errorMessage: undefined,
      isLoading: false,
      unavailableProductCodes: [],
      currency: {},
      valid: false,
      currencyErrorMessage: "",
      maxCharRules: [
        field => (field.length <= 50) || 'Reached max character limit: 50',
      ],
      maxCharManufacturerRules: [
        field => (field.length <= 100) || 'Reached max character limit: 100',
      ],
      maxCharDescriptionRules: [
        field => (field.length <= 200) || 'Reached max character limit: 200',
      ],
      validCharactersSingleLineRules: [
        field => /^[ \d\p{L}\p{P}]*$/u.test(field) || 'Must only contain letters, numbers, punctuation and spaces',
      ],
      validCharactersMultiLineRules: [
        field => /^[\s\d\p{L}\p{P}]*$/u.test(field) || 'Must only contain letters, numbers, punctuation and whitespace',
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ],
      priceRules: [
        //A price must be numbers and may contain a decimal followed by exactly two numbers
        field => /(^\d{1,5}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],
      productCodeRules: [
        field => field.length <= 15 || 'Reached max character limit: 15',
        field => !/ /.test(field) || 'Must not contain a space',
        field => /^[-A-Z0-9]+$/.test(field) || 'Must be all uppercase letters, numbers and dashes.',
        field => !this.unavailableProductCodes.includes(field) || 'Product code is unavailable',
      ]
    };
  },
  async created() {
    // When the create product dialogue, the currency will be set to the currency of the country the product is being
    // sold in. It will have blank fields if no currency can be found from the country.
    const business = await getBusiness(this.$store.state.createProductDialogBusiness);
    const countryOfSale = business.address.country;
    this.currency = await currencyFromCountry(countryOfSale);
  },
  methods: {
    /**
     * Creates the product by calling the API
     **/
    async createProduct() {
      const businessId = this.$store.state.createProductDialogBusiness;

      // Ensures that we have a reference to the original product code.
      const productCode = this.productCode;

      let recommendedRetailPrice = parseFloat(this.recommendedRetailPrice);
      if (isNaN(recommendedRetailPrice)) {
        recommendedRetailPrice = undefined;
      }

      this.errorMessage = undefined;
      this.isLoading = true;
      let response = await createProduct(businessId, {
        id: productCode,
        name: this.product,
        description: this.description,
        manufacturer: this.manufacturer,
        recommendedRetailPrice: recommendedRetailPrice,
      });
      this.isLoading = false;

      if (response === undefined) {
        // No error occurred
        this.closeDialog();
        return;
      }

      if (response === 'Product code unavailable') {
        this.unavailableProductCodes.push(productCode);
        this.$refs.productCodeField.validate();
        return;
      }
      this.errorMessage = response;
    },
    closeDialog() {
      this.$emit('closeDialog');
    },
    /**
     * Accepts only dates in the future
     * @param val
     * @returns {boolean}
     */
    allowedDates: val => new Date(val) > new Date()
  },
};
</script>

<style>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}

.create-product {
  color: var(--v-primary-base);
  font-weight: bolder;
}

.error-text {
  color: var(--v-error-base);
  text-align: right;
}
</style>