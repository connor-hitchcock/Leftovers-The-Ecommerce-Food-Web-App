<template>
  <v-form @submit.prevent="register" v-model="valid">
    <h1>Register</h1>

    <v-container>
      <!-- INPUT: Email -->
      <v-text-field
        class="required"
        v-model="email"
        label="Email"
        :rules="mandatoryRules.concat(emailRules).concat(maxLongCharRules)"
        outlined
      />

      <!-- INPUT: Password -->
      <v-text-field
        ref="password"
        class="required"
        v-model="password"
        label="Password"
        @keyup="passwordChange"
        :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :type="showPassword ? 'text' : 'password'"
        @click:append="showPassword = !showPassword"
        :rules="mandatoryRules.concat(passwordRules).concat(maxMediumCharRules)"
        outlined
      />

      <!-- INPUT: Confirm Password -->
      <v-text-field
        ref="confirmPassword"
        class="required"
        v-model="confirmPassword"
        label="Confirm Password"
        :append-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :type="showConfirmPassword ? 'text' : 'password'"
        @click:append="showConfirmPassword = !showConfirmPassword"
        :rules="mandatoryRules.concat(passwordConfirmationRule).concat(maxMediumCharRules)"
        outlined
      />

      <!-- INPUT: First name -->
      <v-text-field
        class="required"
        v-model="firstName"
        label="First name"
        :rules="mandatoryRules.concat(nameRules).concat(maxMediumCharRules)"
        outlined
      />

      <!-- INPUT: Middle name(s) -->
      <v-text-field
        v-model="middleName"
        label="Middle name(s)"
        :rules="alphabetRules.concat(maxMediumCharRules)"
        outlined
      />

      <!-- INPUT: Last name -->
      <v-text-field
        class="required"
        v-model="lastName"
        label="Last name"
        :rules="mandatoryRules.concat(nameRules).concat(maxMediumCharRules)"
        outlined
      />

      <!-- INPUT: Nickname -->
      <v-text-field
        v-model="nickname"
        label="Nickname"
        :rules="alphabetRules.concat(maxMediumCharRules)"
        outlined
      />

      <!-- INPUT: Bio -->
      <v-textarea
        v-model="bio"
        label="Bio"
        rows="3"
        :rules="charBioRules"
        outlined
      />

      <!-- INPUT: Date of Birth -->
      <v-dialog
        ref="dialog"
        v-model="modal"
        :return-value.sync="dob"
        width="300px"
        persistent
      >
        <template v-slot:activator="{ on, attrs }">
          <v-text-field
            class="required"
            v-model="dob"
            label="Date of Birth"
            :rules="mandatoryRules"
            prepend-inner-icon="mdi-calendar"
            readonly
            v-bind="attrs"
            v-on="on"
            outlined
          />
        </template>
        <v-date-picker
          v-model="dob"
          :max="maxDate"
          scrollable
        >
          <v-spacer/>
          <v-btn
            text
            color="primary"
            @click="closeDatePicker"
          >
            Cancel
          </v-btn>
          <v-btn
            text
            color="primary"
            @click="$refs.dialog.save(dob)"
          >
            OK
          </v-btn>
        </v-date-picker>
      </v-dialog>

      <v-row>
        <v-col
          cols="12"
          sm="4"
        >
          <v-text-field
            ref="countryCode"
            v-model="countryCode"
            label="Country Code"
            :rules="countryCodeRules.concat(phoneRequiresCountryCodeRule)"
            outlined
          />
        </v-col>
        <v-col
          cols="12"
          sm="8">
          <!-- INPUT: Phone -->
          <v-text-field
            v-model="phone"
            label="Phone"
            @keyup="phoneNumberChange"
            :rules="phoneNumberRules"
            outlined
          />
        </v-col>

      </v-row>

      <!-- INPUT: Street -->
      <v-text-field
        class="required"
        v-model="streetAddress"
        label="Street Address"
        :rules="mandatoryRules.concat(streetNumRules)"
        outlined
      />

      <!-- INPUT: District/Region/Province -->
      <LocationAutocomplete
        type="district"
        v-model="district"
        :rules="maxLongCharRules.concat(alphabetRules).concat(maxLongCharRules)"
      />

      <!-- INPUT: City -->
      <LocationAutocomplete
        type="city"
        class="required"
        v-model="city"
        :rules="mandatoryRules.concat(alphabetRules).concat(maxLongCharRules)"
      />

      <!-- INPUT: Region -->
      <LocationAutocomplete
        type="region"
        class="required"
        v-model="region"
        :rules="mandatoryRules.concat(alphabetRules).concat(maxLongCharRules)"
      />

      <!-- INPUT: Country -->
      <LocationAutocomplete
        type="country"
        class="required"
        v-model="country"
        :rules="mandatoryRules.concat(alphabetRules).concat(maxLongCharRules)"
      />

      <!-- INPUT: Postcode -->
      <v-text-field
        class="required"
        v-model="postcode"
        label="Postcode"
        :rules="mandatoryRules.concat(numberRules).concat(maxShortCharRules)"
        outlined
      />

      <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>

      <!-- Register -->
      <v-btn
        type="submit"
        color="primary"
        :disabled="!valid">
        REGISTER
      </v-btn>
      <!-- Login Link if user already has an account. -->
      <p
        class="link pt-5"
        @click="showLogin"
      >
        Already have an account? Login.
      </p>
    </v-container>
  </v-form>
</template>


<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';
import {createUser} from '../../api/internal';

export default {
  name: 'Register',
  components: {
    LocationAutocomplete
  },
  data () {
    return {
      showPassword: false,
      showConfirmPassword: false,
      errorMessage: undefined,
      valid: false,
      email: '',
      password: '',
      confirmPassword: '',
      firstName: '',
      middleName: '',
      lastName: '',
      nickname: '',
      bio: '',
      dob: '',
      countryCode: '64',
      phone: '',
      streetAddress: '',
      district: '',
      region: '',
      city: '',
      country: '',
      postcode: '',
      modal: false,
      items: [],
      isLoading: false,
      maxDate: '',
      emailRules: [
        //regex rules for emails, example format is as such:
        //"blah@hotmail.co
        //if it does not follow the format, display error message
        email =>
          /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(email)
          || 'E-mail must be valid'
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ],
      passwordRules: [
        field => (field && field.length >= 7) || 'Password must have 7+ characters',
        field => /^(?=.*[0-9])(?=.*[a-zA-Z])([a-zA-Z0-9]+)$/.test(field) || 'Must have at least one number and one alphabet'
      ],
      numberRules: [
        field => /(^[a-zA-Z0-9]*$)/.test(field) || 'Must contain numbers and alphabet only'
      ],
      nameRules: [
        field =>  (field.length === 0 || (/^[a-z-//.// ]+$/i).test(field)) || 'Naming must be valid'
      ],
      maxShortCharRules: [
        field => (field.length <= 16) || 'Reached max character limit: 16'
      ],
      maxMediumCharRules: [
        field => (field.length <= 32) || 'Reached max character limit: 32'
      ],
      maxLongCharRules: [
        field => (field.length <= 100) || 'Reached max character limit: 100'
      ],
      charBioRules: [
        field => (field.length <= 200) || 'Reached max character limit: 200',
        field => /(^[ a-zA-Z0-9@//$%&!'//#,//.//(//)//:;_-]*$)/.test(field) || 'Bio must only contain letters, numbers, and valid special characters'
      ],
      phoneNumberRules: [
        field => /(^\(?\d{1,3}\)?[\s.-]?\d{3,4}[\s.-]?\d{4,5}$)|(^$)/.test(field) || 'Must be a valid phone number'
      ],
      countryCodeRules: [
        field => /(^(\d{1,2}-)?\d{2,3}$)|(^$)/.test(field) || 'Must be a valid country code.'
      ],
      alphabetRules: [
        field => ( field.length === 0 || /^[a-z-//.// ]+$/i.test(field)) || 'Naming must be valid'
      ],
      streetNumRules: [
        field => (field && field.length <= 109) || 'Reached max character limit 109 ',
        field => /^(([0-9]+|[0-9]+\/[0-9]+)[a-zA-Z]?)(?=.*[\s])(?=.*[a-zA-Z ])([a-zA-Z0-9 ]+)$/.test(field) || 'Must have at least one number and one alphabet'
      ],
    };
  },

  methods: {
    // Show login screen
    showLogin () {
      this.$emit('showLogin');
    },
    // Complete registration with API
    async register () {
      this.errorMessage = undefined;

      /**
       * Get the street number and name from the street address field.
       */
      const streetParts = this.streetAddress.split(" ");
      const streetNum = streetParts[0];
      const streetName = streetParts.slice(1, streetParts.length).join(" ");
      /**
       * Combine the country code and phone number to get the full phone number.
       */
      let fullPhoneNum;
      if (this.phone === '') {
        fullPhoneNum = '';
      } else {
        fullPhoneNum = '+' + this.countryCode + ' ' + this.phone;
      }

      let user = {
        firstName   : this.firstName,
        lastName    : this.lastName,
        middleName  : this.middleName,
        nickname    : this.nickname,
        bio         : this.bio,
        email       : this.email,
        dateOfBirth : this.dob,
        phoneNumber : fullPhoneNum,
        homeAddress : {
          streetNumber  : streetNum,
          streetName    : streetName,
          city          : this.city,
          region        : this.region,
          country       : this.country,
          postcode      : this.postcode,
          district      : this.district
        },
        password    : this.password,
      };

      let response = await createUser(user);
      console.log(response);
      if (response === undefined ) {
        this.$emit('showLogin');
        return;
      }
      this.errorMessage = response;
    },
    // Close the date picker modal
    closeDatePicker () {
      this.modal = false;
    },
    //Feature bug:
    //After the user has successfully typed in the same values in the password and confirmPassword, if the user decides
    //to change the password field value again first without editing the confirmPassword field after, because it has
    //been validated once before, the form would recognize both fields to be valid. That is not what is wanted from
    //this form validation.
    //Feature fix:
    //The bottom method solves that issue by observing the password field. That means the @keyup attribute in the
    //password field observes every finished keystroke in there and calls the ref with "confirmPassword" (in this case
    //it refers to the confirmPassword field)to revalidate itself upon any changes in the password field.
    passwordChange () {
      this.$refs.confirmPassword.validate();
    },
    phoneNumberChange () {
      this.$refs.countryCode.validate();
    },
    querySelections (v) {
      this.loading = true;
      // Simulated ajax query
      setTimeout(() => {
        this.items = this.states.filter(e => {
          return (e || '').toLowerCase().indexOf((v || '').toLowerCase()) > -1;
        });
        this.loading = false;
      }, 500);
    },
    minimumDateOfBirth () {
      //minimum age of a user must be 13
      let today = new Date();
      let year = today.getFullYear();
      let month = today.getMonth();
      let day = today.getDate();

      return new Date(year - 13, month, day);
    }
  },
  computed: {
    //The computed property below is dependent on two user input fields, password and password confirmation.
    //After the user has typed in the password field, the confirmPassword field would check this rule for each
    //change(in this case, each keystroke), and compare it with the password field. If they are not the same,
    //the error message "Passwords must match" will show up at the bottom of the confirmPassword field, until it
    //is the same.
    passwordConfirmationRule () {
      return () =>
        this.password === this.confirmPassword || 'Passwords must match';
    },
    phoneRequiresCountryCodeRule () {
      return () =>
        !(this.phone.length > 0 && this.countryCode.length < 1) || 'Country code must be present';
    }
  },
  //as any components are added to the dom, mounted() will be called
  mounted () {
    //sets maxDate and date of birth value
    this.maxDate = this.minimumDateOfBirth().toISOString().slice(0, 10);
    this.dob = this.minimumDateOfBirth().toISOString().slice(0, 10);
  }
};

</script>

<style>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}
.error-text {
  color: var(--v-error-base);
  font-size: 140%;
}
</style>
