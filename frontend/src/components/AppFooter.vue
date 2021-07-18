<template>
  <div class="foot">
    <v-divider/>
    <v-row justify="center" align="center" no-gutters>
      <div class="item">© 2021 - Team 'Internal Error'</div>
      <router-link class="item link" to="/about">About</router-link>
      <router-link class="item link" to="/contact">Contact</router-link>
    </v-row>
  </div>
</template>

<style scoped>

.item {
  padding: 8px;
  user-select: none;
}

.link {
  color: black;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

</style>