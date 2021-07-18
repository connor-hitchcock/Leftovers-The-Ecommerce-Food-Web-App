<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >
      <v-form v-model="valid">
        <v-card>
          <v-card-title>
            <h4 class="primary--text">Create Inventory Item</h4>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <!-- INPUT: Product code Currently used v-selector to reduce typo probability from user -->
                <v-col cols="6">
                  <v-select
                    no-data-text="No products found"
                    class="required"
                    solo
                    value = "product Code"
                    v-model="productCode"
                    :items="filteredProductList"
                    label="Product Code"
                    item-text="name"
                    item-value="id"
                    :rules="mandatoryRules"
                    :hint="productCode"
                    @click="productCode=undefined"
                    persistent-hint
                    outlined
                  >
                    <template v-slot:prepend-item>
                      <v-list-item>
                        <v-list-item-content>
                          <v-text-field
                            label="Search for a product"
                            v-model="productFilter"
                            clearable
                            :autofocus="true"
                            @click:clear="resetSearch"
                            hint="Id, Name, Description, Manufacturer"
                          />
                        </v-list-item-content>
                      </v-list-item>
                    </template>
                  </v-select>
                </v-col>
                <!-- INPUT: Quantity. Only allows number.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    solo
                    v-model="quantity"
                    label="Quantity"
                    :rules="mandatoryRules.concat(quantityRules)"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Price per item. Only allows number or '.'but come with 2 digit -->
                <v-col cols="6">
                  <v-text-field
                    v-model="pricePerItem"
                    label="Price Per Item"
                    :prefix="currency.symbol"
                    :suffix="currency.code"
                    :hint="currency.errorMessage"
                    :rules="maxCharRules.concat(smallPriceRules)"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Total Price. Only allows number or '.'but come with 2 digit -->
                <v-col cols="6">
                  <v-text-field
                    v-model="totalPrice"
                    label="Total Price"
                    :prefix="currency.symbol"
                    :suffix="currency.code"
                    :hint="currency.errorMessage"
                    :rules="maxCharRules.concat(hugePriceRules)"
                    outlined/>
                </v-col>
                <!-- INPUT: Manufactured. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="manufactured"
                    label="Manufactured"
                    type="date"
                    @input=checkManufacturedDateValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Sell By. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="sellBy"
                    label="Sell By"
                    type="date"
                    @input=checkSellByDateValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Best Before. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="bestBefore"
                    label="Best Before"
                    type="date"
                    @input=checkBestBeforeDateValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Expires. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    v-model="expires"
                    label="Expires"
                    type="date"
                    @input=checkExpiresDateVaild()
                    outlined/>
                </v-col>
              </v-row>
              <!-- Error Message if textfield.value !valid -->
              <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer/>
            <v-btn
              color="primary"
              text
              @click="closeDialog">
              Close
            </v-btn>
            <v-btn
              type="submit"
              label="submit"
              color="primary"
              :disabled="!valid || !datesValid"
              @click.prevent="CreateInventory">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>

<script>
import { createInventoryItem, getBusiness } from '@/api/internal';
import { getProducts } from "@/api/internal";
import { currencyFromCountry } from "@/api/currency";

export default {
  name: 'CreateInventory',
  components: {
  },
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      valid: false,
      today: new Date(),
      productCode : "",
      productList: [],
      quantity : "",
      pricePerItem: "",
      totalPrice: "",
      manufactured: "",
      manufacturedValid: true,
      sellBy: "",
      sellByValid: true,
      bestBefore: "",
      bestBeforeValid: true,
      expires: new Date().toISOString().slice(0,10),
      expiresValid: true,
      datesValid: true,
      productFilter: '',
      minDate: new Date("1500-01-01"),
      maxDate: new Date("5000-01-01"),
      currency: {},
      maxCharRules: [
        field => (field.length <= 100) || 'Reached max character limit: 100'
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ],
      numberRules: [
        field => /(^[0-9]*$)/.test(field) || 'Must contain numbers only'
      ],
      quantityRules: [
        field => /(^[1-9][0-9]*$)/.test(field) || 'Must contain numbers only above zero'
      ],
      smallPriceRules: [
        //A price must be numbers and may contain a decimal followed by exactly two numbers (4digit)
        field => /(^\d{1,4}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],
      hugePriceRules: [
        //A price must be numbers and may contain a decimal followed by exactly two numbers (6digit)
        field => /(^\d{1,6}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],
    };
  },
  methods: {
    /**
     * Closes the dialog
     */
    closeDialog() {
      this.$emit('closeDialog');
    },

    /**
     * Populates the products array for the dropdown select for selecting a product
     * @returns {Promise<void>}
     */
    async fetchProducts() {
      // get the list of products for this business
      const result = await getProducts(this.businessId, null, 10000, 'name', false);
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.productList = result;
      }
    },
    resetSearch: function () {
      this.productFilter = '';
    },
    /**
     * Defines a predicate used for filtering the available products
     * Predicate matches Id, Name, Manufacturer and Description
     * @param product The product to compare
     * @returns {boolean|undefined}
     */
    filterPredicate(product) {
      const filterText = this.productFilter ?? '';
      return product.id.toLowerCase().includes(filterText.toLowerCase()) ||
          product.name.toLowerCase().includes(filterText.toLowerCase()) ||
          product.manufacturer?.toLowerCase().includes(filterText.toLowerCase()) ||
          product.description?.toLowerCase().includes(filterText.toLowerCase());
    },
    /**
     * Called when the form is submitted
     * Requests backend to create an inventory item
     * Empty attributes are set to undefined
     */
    async CreateInventory() { //to see the attribute in console for debugging or testing, remove after this page is done
      const businessId = this.$store.state.createInventoryDialog;
      this.errorMessage = undefined;
      let quantity;
      try {
        quantity = parseInt(this.quantity);
      } catch (error) {
        this.errorMessage = 'Could not parse field \'Quantity\'';
        return;
      }
      const inventoryItem = {
        productId: this.productCode,
        quantity: quantity,
        pricePerItem: this.pricePerItem.length ? this.pricePerItem : undefined,
        totalPrice: this.totalPrice ? this.totalPrice : undefined,
        manufactured: this.manufactured ? this.manufactured : undefined,
        sellBy: this.sellBy ? this.sellBy : undefined,
        bestBefore: this.bestBefore ? this.bestBefore : undefined,
        expires: this.expires
      };
      const result = await createInventoryItem(businessId, inventoryItem);
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.closeDialog();
      }
    },

    async checkAllDatesValid() {
      //checks the booleans for all the dates are valid
      if (this.manufacturedValid && this.sellByValid && this.bestBeforeValid && this.expiresValid) {
        this.datesValid = true;
      } else {
        this.datesValid = false;
      }
    },
    async checkManufacturedDateValid() {
      //checks manufactured cannot be after today and is before sell by
      let sellByDate = new Date(this.manufactured);
      let manufacturedDate = new Date(this.manufactured);
      this.manufacturedValid = false;
      if (manufacturedDate < this.minDate || manufacturedDate > this.maxDate) {
        this.errorMessage = "The manufactured date cannot be before 1500 AD or after 5000 AD";
      } else if (manufacturedDate > this.today) {
        this.errorMessage = "The manufactured date is after today!";
      } else if (manufacturedDate > sellByDate) {
        this.errorMessage = "The manufactured date cannot be after the sell by date!";
      } else {
        this.errorMessage = undefined;
        this.manufacturedValid = true;
      }
      await this.checkAllDatesValid();
    },
    async checkSellByDateValid() {
      //checks sell by date cannot be before today and is after manufactured and before best before
      let bestBeforeDate = new Date(this.bestBefore);
      let sellByDate = new Date(this.sellBy);
      let manufacturedDate = new Date(this.manufactured);
      this.sellByValid = false;
      if (sellByDate < this.minDate || sellByDate > this.maxDate) {
        this.errorMessage = "The sell by date cannot be before 1500 AD or after 5000 AD";
      } else if (sellByDate < this.today) {
        this.errorMessage = "The sell by date is before today!";
      } else if (sellByDate < manufacturedDate) {
        this.errorMessage = "The sell by date cannot be before the manufactured date!";
      } else if (sellByDate > bestBeforeDate) {
        this.errorMessage = "The sell by date cannot be after the best before date!";
      } else {
        this.errorMessage = undefined;
        this.sellByValid = true;
      }
      await this.checkAllDatesValid();
    },
    async checkBestBeforeDateValid() {
      //checks best before date cannot be before today and is after sell by date
      let expiresDate = new Date(this.expires);
      let bestBeforeDate = new Date(this.bestBefore);
      let sellByDate = new Date(this.sellBy);
      this.bestBeforeValid = false;
      if (bestBeforeDate < this.minDate || bestBeforeDate > this.maxDate) {
        this.errorMessage = "The best before date cannot be before 1500 AD or after 5000 AD";
      } else if (bestBeforeDate < this.today) {
        this.errorMessage = "The best before date is before today!";
      } else if (bestBeforeDate < sellByDate) {
        this.errorMessage = "The best before date cannot be before the sell by date!";
      } else if (bestBeforeDate > expiresDate) {
        this.errorMessage = "The best before date cannot be after the expires date!";
      } else {
        this.errorMessage = undefined;
        this.bestBeforeValid = true;
      }
      await this.checkAllDatesValid();
    },
    async checkExpiresDateVaild() {
      //checks expires date cannot be before today and is after best before date
      let expiresDate = new Date(this.expires);
      let bestBeforeDate = new Date(this.bestBefore);
      this.expiresValid = false;
      if (expiresDate < this.minDate || expiresDate > this.maxDate) {
        this.errorMessage = "The expires date cannot be before 1500 AD or after 5000 AD";
      } else if (expiresDate < this.today) {
        this.errorMessage = "The expires date is before today!";
      } else if (expiresDate < bestBeforeDate) {
        this.errorMessage = "The expires date cannot be before the best before date!";
      } else {
        this.errorMessage = undefined;
        this.expiresValid = true;
      }
      await this.checkAllDatesValid();
    },

    async fetchCurrency() {
      const business = await getBusiness(this.businessId);
      this.currency = await currencyFromCountry(business.address.country);
    }
  },
  computed: {
    /**
     * Filters the list of products based on the value of the search term
     * value must be passed to ensure products are refreshed when input is cleared
     */
    filteredProductList() {
      return this.productList.filter(x => this.filterPredicate(x));
    },

    /**
     * Gets the business ID from the store
     */
    businessId() {
      return this.$store.state.createInventoryDialog;
    }
  },
  created() {
    this.fetchCurrency();
    this.fetchProducts();
  },
};
</script>

<style scoped>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}
</style>