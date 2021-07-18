<template>
  <div>
    <!-- User actions - Wide version -->
    <v-card
      rounded="lg"
      class="action-pane small-no-display "
    >
      <v-card-text>
        <v-list>
          <v-list-item-group>
            <v-list-item @click="viewProfile">
              <v-list-item-icon>
                <v-icon>mdi-account-circle</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Profile</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewCreateBusiness">
              <v-list-item-icon>
                <v-icon>mdi-briefcase-plus</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Add Business</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewMarketplace">
              <v-list-item-icon>
                <v-icon>mdi-store</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Marketplace</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card-text>
    </v-card>

    <!-- User actions - Narrow version -->
    <v-card class="action-pane large-no-display">
      <v-btn icon @click="viewProfile" class="action-button">
        <v-icon large>mdi-account-circle</v-icon>
      </v-btn>
      <v-btn icon @click="viewCreateBusiness" class="action-button">
        <v-icon large>mdi-briefcase-plus</v-icon>
      </v-btn>
      <v-btn icon @click="viewMarketplace" class="action-button">
        <v-icon large>mdi-store</v-icon>
      </v-btn>
    </v-card>
  </div>
</template>

<script>
export default {
  name: "UserActionPanel",
  methods: {
    /**
     * Shows the user profile page
     */
    viewProfile() {
      this.$router.push("/profile");
    },
    /**
     * Shows the create business dialog
     */
    viewCreateBusiness() {
      this.$store.commit('showCreateBusiness');
    },
    /**
     * Redirect to marketplace page
     */
    viewMarketplace() {
      this.$router.push("/marketplace");
    }
  }
};
</script>


<style scoped>

@media all and (min-width: 992px) {
  .large-no-display {
    display: none !important;
  }
}

@media not all and (min-width: 992px) {
  .small-no-display {
    display: none !important;
  }
}

.action-pane {
  margin-right: 10px;
  max-height: 350px;
}


@media not all and (min-width: 992px) {
  .action-pane {
    display: block;
  }
}

.action-button {
  display: block;
  margin: 10px;
}

</style>