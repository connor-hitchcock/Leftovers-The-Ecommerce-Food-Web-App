<template>
  <v-container>
    <!-- Red block to show this is the admin dashbaord/ panel -->
    <v-card height="400px" color="grey lighten-3">
      <v-footer v-bind="localAttrs" :padless="padless">
        <v-card height="70px" rounded width="100%" color="primary" class=" text-center font-weight-bold ">
          <v-divider/>

          <v-card-text class="white--text">
            {{ new Date().getFullYear() }} —
            <strong>Admin Dashboard</strong>
          </v-card-text>
        </v-card>
      </v-footer>

      <!-- Admin action list -->
      <v-card
        height="310"
        width="256"
        color="white"
      >
        <!--     class="mx-auto"      -->
        <v-list-item flat tile color="background">
          <v-list-item-content>

            <v-list-item-title class="title">
              Admin Action
            </v-list-item-title>
            <v-list-item-subtitle>
              DGAA Privilege
            </v-list-item-subtitle>

          </v-list-item-content>
        </v-list-item>
        <v-list dense nav>
          <v-card color="grey lighten-5">
            <v-list-item v-for="item in items" :key="item.title" link>
              <v-list-item-icon>
                <v-icon>{{ item.icon }}</v-icon>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title>{{ item.title }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-card>
        </v-list>
      </v-card>

      <v-divider/>

      <!-- Action list that admin can execute -->

    </v-card>
  </v-container>
</template>

<script>
export default {
  data () {
    return {
      items: [
        { title: "Dashboard", icon: "mdi-view-dashboard" },
        { title: "Gallery", icon: "mdi-image" },
        { title: "Admin Guide", icon: "mdi-help-box" },
      ],
      right: null,
    };
  },
};
</script>

<style scoped></style>
