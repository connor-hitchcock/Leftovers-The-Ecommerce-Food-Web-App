<template>
  <!-- use the v-dialog to show a pop up for the whole product description -->
  <v-dialog v-model="dialog">
    <template v-slot:activator="{ on, attrs }">
      <!-- using the "Read more..." link to activate the pop up dialog -->
      <!--  v-bind="attrs" v-on="on" allows the v-dialog to use this link as the activator for the dialog box -->
      <a
        v-bind="attrs"
        v-on="on"
      >
        Read more
      </a>
    </template>
    <v-card>
      <v-card-title>
        <h2>Description</h2>
      </v-card-title>
      <!-- shows the whole product description -->
      <v-card-text>
        {{ productDescription }}
        <br>
        <v-card-actions class="pa-0">
          <!-- if the user presses return, will close the dialog -->
          <a @click="dialog = false">return</a>
        </v-card-actions>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "FullProductDescription",
  //pass in productDescription from parent compoenent
  props: ["productDescription"],
  data() {
    return {
      // if dialog is false, the popup does not appear.
      dialog: false,
    };
  },
};
</script>
