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
            <h4 class="primary--text">Create Business</h4>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col>
                  <v-text-field
                    class="required"
                    v-model="business"
                    label="Name of business"
                    :rules="mandatoryRules.concat(maxCharRules)"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-textarea
                    v-model="description"
                    label="Description"
                    :rules="maxCharDescriptionRules"
                    rows="3"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-select
                    class="required"
                    v-model="businessType"
                    :items="businessTypes"
                    label="Business Type"
                    :rules="mandatoryRules.concat(maxCharRules)"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="street1"
                    label="Company Street Address"
                    :rules="mandatoryRules.concat(streetRules)"
                    outlined/>
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="district"
                    v-model="district"
                    :rules="maxCharRules"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="city"
                    class="required"
                    v-model="city"
                    :rules="mandatoryRules.concat(maxCharRules)"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="region"
                    class="required"
                    v-model="region"
                    :rules="mandatoryRules.concat(maxCharRules)"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="country"
                    class="required"
                    v-model="country"
                    :rules="mandatoryRules.concat(maxCharRules)"
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="postcode"
                    label="Postcode"
                    :rules="mandatoryRules.concat(maxCharRules)"
                    outlined
                  />
                </v-col>
              </v-row>
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
              color="primary"
              :disabled="!valid"
              @click.prevent="createBusiness">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>

<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';
import {createBusiness} from '@/api/internal';

export default {
  name: 'CreateBusiness',
  components: {
    LocationAutocomplete,
  },
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      business: '',
      description: '',
      businessType: [],
      street1: '',
      district: '',
      city: '',
      region: '',
      country: '',
      postcode: '',
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organisation',
        'Non-profit organisation',
        'Retail Trade',
      ],
      valid: false,
      maxCharRules: [
        field => (field.length <= 100) || 'Reached max character limit: 100'
      ],
      maxCharDescriptionRules: [
        field => (field.length <= 200) || 'Reached max character limit: 200'
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ],
      streetRules: [
        field => /^(([0-9]+|[0-9]+\/[0-9]+)[a-zA-Z]?)(?=.*[\s])(?=.*[a-zA-Z ])([a-zA-Z0-9 ]+)$/.test(field) || 'Must have at least one number and one alphabet'
      ]
    };
  },
  methods: {
    async createBusiness() {
      this.errorMessage = undefined;
      /**
       * Get the street number and name from the street address field.
       */
      const streetParts = this.street1.split(" ");
      const streetNum = streetParts[0];
      const streetName = streetParts.slice(1, streetParts.length).join(" ");
      let business = {
        primaryAdministratorId: this.$store.state.user.id,
        name: this.business,
        description: this.description,
        address: {
          streetNumber: streetNum,
          streetName: streetName,
          district: this.district,
          city: this.city,
          region: this.region,
          country: this.country,
          postcode: this.postcode,
        },
        businessType: this.businessType,
      };
      let response = await createBusiness(business);
      if (response === undefined) {
        this.closeDialog();
        this.$router.go();
      } else {
        this.errorMessage = response;
      }
    },
    closeDialog() {
      this.$emit('closeDialog');
    }
  }
};
</script>

<style scoped>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}
</style>