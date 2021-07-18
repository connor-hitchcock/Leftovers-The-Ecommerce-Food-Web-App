<template>
  <v-app-bar max-height="64px">
    <div class="container-outer flex-center">
      <h1 class="link" @click="showHome">LEFT_OVERS</h1>

      <!-- Space between the app name and the controls -->
      <div class="spacer"/>

      <!-- Search Bar component to perform search and show result, if not on search page -->
      <SearchBar v-if="$route.path !== '/search'" />

      <!-- Profile Menu -->
      <div class="flex-center">
        <!-- Dropdown menu for selecting role which user is currently acting as -->
        <div class="role-menu">
          <v-menu offset-y>
            <template v-slot:activator="{ on, attrs }">
              <!-- Username and icon -->
              <v-chip
                v-if="user"
                v-bind="attrs"
                v-on="on"
              >
                <UserAvatar :user="user" size="small" />
                <div class="name">
                  {{ roles[selectedRole].displayText }}
                </div>
              </v-chip>
            </template>

            <!-- Menu Items -->
            <v-list>
              <!-- User/Business Selector -->
              <div class="section-title">
                Profiles
              </div>
              <v-list-item-group
                v-model="selectedRole"
                color="primary"
              >
                <template v-for="(role, index) in roles">
                  <v-list-item :key="index">
                    <v-list-item-title>{{ role.displayText }}</v-list-item-title>
                  </v-list-item>
                </template>
              </v-list-item-group>

              <hr>

              <!-- DGAA/GAA Indicator -->
              <v-list-item v-if="isDGAA" class="admin link" @click="viewAdmin">
                <v-list-item-title class="admin"> ADMIN </v-list-item-title>
              </v-list-item>
              <v-list-item v-else-if="isAdmin" class="admin">
                <v-list-item-title class="admin"> ADMIN </v-list-item-title>
              </v-list-item>

              <!-- View User Profile -->
              <v-list-item>
                <v-list-item-title class="link" @click="viewProfile">
                  Profile
                </v-list-item-title>
              </v-list-item>

              <!-- Home -->
              <v-list-item>
                <v-list-item-title class="link" @click="showHome">
                  Home
                </v-list-item-title>
              </v-list-item>

              <!-- View marketplace -->
              <v-list-item v-if="isUser">
                <v-list-item-title class="link" @click="viewMarketplace">
                  Marketplace
                </v-list-item-title>
              </v-list-item>

              <!-- Logout -->
              <v-list-item>
                <v-list-item-title class="link" @click="logout">
                  Logout
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </div>
      </div>
    </div>
  </v-app-bar>
</template>

<script>
import SearchBar from "./utils/SearchBar";
import UserAvatar from "./utils/UserAvatar";
import { USER_ROLES } from "../utils";

export default {
  name: "AppBar",
  components: {
    SearchBar,
    UserAvatar,
  },
  data() {
    return {
      selectedRole : 0,
    };
  },
  methods: {
    viewProfile() {
      // Navigate to the profile page of the current active role
      switch (this.$store.state.activeRole.type) {
      case "user":
        this.$router.push("/profile");
        break;
      case "business":
        this.$router.push("/business/" + this.$store.state.activeRole.id);
        break;
      default:
        this.$router.push("/profile");
      }
    },
    showHome() {
      this.$router.push("/home");
    },
    viewMarketplace() {
      this.$router.push("/marketplace");
    },
    logout() {
      this.$store.commit("logoutUser");
      this.$router.push("/login");
    },
    viewAdmin() {
      this.$router.push("/admin");
    }
  },
  computed: {
    isAdmin() {
      return [USER_ROLES.DGAA, USER_ROLES.GAA].includes(
        this.$store.getters.role
      );
    },
    isDGAA() {
      return this.$store.getters.role === USER_ROLES.DGAA;
    },
    isUser() {
      return this.$store.state.activeRole.type === "user";
    },
    user() {
      return this.$store.state.user;
    },
    roles() {
      let result = [
        { displayText: this.user.firstName, type: "user", id: this.user.id }
      ];

      for (const business of this.user.businessesAdministered) {
        result.push({ displayText: business.name, type: "business", id: business.id });
      }

      return result;
    }
  },
  watch : {
    selectedRole() {
      // Set the role that the user is acting as to the role that has been selected from the list
      const role = this.roles[this.selectedRole];
      this.$store.commit('setRole', { type: role.type, id: role.id });
    },

    /**
     * Handler for when the store changes the active role.
     * Updates the selected item if it differs from the current selection.
     */
    '$store.state.activeRole': {
      handler() {
        // This is a bit dubious I'd like to refactor it into something cleaner.
        const currentRole = this.roles[this.selectedRole];
        const actualRole = this.$store.state.activeRole;

        if (actualRole === null) return;
        if (actualRole.type === currentRole.type && actualRole.id === currentRole.id) return;

        let newSelection = 0;
        for (const role of this.roles) {
          if (role.type === actualRole.type && role.id === actualRole.id) {
            this.selectedRole = newSelection;
          }
          newSelection++;
        }
      },
      immediate: true,
    },
  }
};
</script>

<style scoped>
.spacer {
  flex: 1;
  max-width: 900px;
}

.name {
  align-self: center;
  margin-left: 5px;
}

.admin {
  color: rgb(255, 16, 16);
  background-color: rgb(212, 212, 212);
  font-weight: 500;
}

.list {
  padding-top: 0;
  padding-bottom: 0;
}

.flex-center {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  text-align: center;
  font-weight: 500;
}
</style>
