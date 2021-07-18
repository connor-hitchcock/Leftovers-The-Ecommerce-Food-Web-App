import Vue from 'vue';
import Vuetify from 'vuetify/lib/framework';

Vue.use(Vuetify);

export default new Vuetify({
  theme: {
    themes: {
      light: {
        primary: '#00bf63',
        secondary: '#8638d9',
        error: '#f54141',
        white: '#ffffff',
        lightGrey: '#e5e5e5',
        grey: '#bdbdbd',
        darkGrey: '#858585',
        black: '#000000'
      }
    },
    options: {
      customProperties: true
    }
  }
});
