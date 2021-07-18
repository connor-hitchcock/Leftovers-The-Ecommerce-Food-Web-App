<template>
  <div class="container">
    <div v-if="login">
      <Login @showRegister="togglePage" />
    </div>
    <div v-else>
      <Register @showLogin="togglePage" />
    </div>
  </div>
</template>


<script>
import Register from './Register';
import Login from './Login';

export default {
  name: 'Auth',
  components: {
    Login,
    Register
  },
  data() {
    return {
      login: true
    };
  },
  methods: {
    togglePage() {
      this.login = !this.login;
    }
  }
};
</script>


<style scoped>
.container {
  max-width: 600px;
  text-align: center;
}
</style>
