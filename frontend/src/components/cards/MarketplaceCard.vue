<template>
  <v-card min-height="250px" class="d-flex flex-column">
    <v-card-title class="my-n1 title">
      {{ content.title }}
    </v-card-title>
    <v-card-text class="my-n2 flex-grow-1 d-flex flex-column justify-space-between">
      <div>
        <strong v-if="location">
          {{ locationString }}
          <br>
        </strong>
        <div class="d-flex justify-space-between flex-wrap">
          <span class="mr-1">
            <em v-if="creator">By {{ creator.firstName }} {{ creator.lastName }}</em>
          </span>
          <span v-if="content.created !== undefined">
            Posted {{ formatDate(content.created) }}
          </span>
        </div>
        {{ content.description }}
      </div>
      <div class="v-flex mx-n1">
        <v-chip
          v-for="keyword in content.keywords"
          :key="keyword.id"
          small
          color="primary"
          class="mx-1 my-1"
        >
          {{ keyword.name }}
        </v-chip>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
import { formatDate } from '@/utils';

export default {
  name: "MarketplaceCard",
  props: {
    content: {
      id: Number,
      title: String,
      description: String,
      creator: Object,
      created: String,
      keywords: Array,
    },
  },

  computed: {
    creator() {
      return this.content.creator;
    },
    location() {
      return this.creator?.homeAddress;
    },
    locationString() {
      if (this.location.district !== undefined && this.location.city !== undefined) {
        return `From ${this.location.district}, ${this.location.city}`;
      } else if (this.location.city !== undefined) {
        return `From ${this.location.city}, ${this.location.country}`;
      } else {
        return `From ${this.location.country}`;
      }
    },
  },

  methods: {
    formatDate,
  }
};
</script>


<style scoped>
.title {
  line-height: 1.25;
  word-break: break-word;
}
</style>