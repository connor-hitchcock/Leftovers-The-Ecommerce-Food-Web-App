<template>
  <v-combobox
    :label="label"
    :items="autocompleteItems"
    :loading="isLoading"
    :search-input.sync="search"
    :rules="rules"
    :value="value"
    no-filter
    clearable
    outlined
  />
</template>

<script>
import {insertResultsFromAPI} from './Methods/autocomplete.ts';

const AUTOCOMPLETE_TYPES = {
  country: {
    label: 'Country',
    tags: 'osm_tag=place:country',
  },
  region: {
    label: 'State/Province/Region',
    tags: 'osm_tag=place',
  },
  city: {
    label: 'City',
    tags: 'osm_tag=place:city&osm_tag=place:town',
  },
  district: {
    label: 'District/City Area',
    tags: 'osm_tag=place:suburb'
  }
};


export default {
  name: 'LocationAutocomplete',
  props: ['type', 'rules', 'value'],
  data () {
    return {
      label: AUTOCOMPLETE_TYPES[this.type].label,
      autocompleteItems: [],
      isLoading: false,
      search: '',
    };
  },

  watch: {
    search (val) {
      if (val === null) val = '';
      this.$emit('input', val);
      this.autocompleteItems = [];
      //if input length exists, and has a length more than 2
      if (val.length > 2) {
        //show loading animation
        this.isLoading = true;
        //append to the api url the input the user has entered
        //added option for api that only produces english results
        let url = `https://photon.komoot.io/api/?lang=en&q=${encodeURIComponent(val)}&${AUTOCOMPLETE_TYPES[this.type].tags}`;
        insertResultsFromAPI(url, this.autocompleteItems).then(() => {
          //after everything is shown, the loading animation will stop
          this.isLoading = false;
        });
      }
    }
  }
};
</script>
