<template>
  <div class="flex-row">
    <v-form @submit.prevent="submit">
      <v-text-field
        hide-details
        single-line
        outlined
        placeholder="Search"
        class="searchbox"
        append-icon="mdi-magnify"
        @click:append="submit"
        v-model="query"
      />
    </v-form>
  </div>
</template>

<script>
import { USER_ROLES } from '@/utils';
export default {
  name: "SearchBar",
  data() {
    return {
      query: ''
    };
  },
  methods: {
    submit() {
      this.$router.push({ path: "/search", query: {query: this.query} });
    },
  },
  computed: {
    isAdmin() {
      return this.$store.getters.role !== USER_ROLES.USER;
    }
  }
};
//hide-details - before icon
</script>

<style scoped>
.searchbox {
  width: 300px;
}
</style>
