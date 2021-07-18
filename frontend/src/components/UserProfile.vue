<template>
  <v-card class="body" v-if="user">
    <div class="top-section">
      <div class="profile-img">
        <UserAvatar :user="user" size="large" />
      </div>

      <div>
        <h1>
          {{ user.firstName }} {{ user.lastName }}
        </h1>
        <h2>
          <i>{{ user.nickname }}</i>
        </h2>
        <p><b>Member Since:</b> {{ createdMsg }}</p>
      </div>

      <!-- List of available actions -->
      <div class="action-menu">
        <v-dialog
          v-if="isActingAsBusiness && isViewingOwnProfile===false"
          v-model="removeAdminDialog"
          persistent
          max-width="300"
        >
          <template #activator="{ on: dialog, attrs}">
            <v-tooltip bottom >
              <template #activator="{ on: tooltip}">
                <v-btn
                  icon
                  color="primary"
                  v-bind="attrs"
                  v-on="{...tooltip, ...dialog}"
                  :disabled="isUserAdminOfActiveBusiness === false"
                  ref="removeAdminButton"
                >
                  <v-icon>mdi-account-minus</v-icon>
                </v-btn>
              </template>
              <span> Remove administrator </span>
            </v-tooltip>
          </template>
          <v-card>
            <v-card-title class="headline">
              Are you sure?
            </v-card-title>
            <v-card-text>This user will no longer be able to operate this business as an administrator for {{this.$store.state.user.businessesAdministered.find(x => x.id === this.activeRole.id).name}}.</v-card-text>
            <v-card-actions>
              <v-spacer/>
              <v-btn
                ref="cancelButton"
                color="green darken-1"
                text
                @click="removeAdminDialog = false"
              >
                No
              </v-btn>
              <v-btn
                ref="confirmButton"
                color="green darken-1"
                text
                @click="removeAdminDialog = false; removeUserAdmin()"
              >
                Yes, Demote
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog
          v-if="isActingAsBusiness && isViewingOwnProfile===false"
          v-model="addAdminDialog"
          persistent
          max-width="300"
        >
          <template #activator="{ on: dialog, attrs}">
            <v-tooltip bottom>
              <template #activator="{ on: tooltip }">
                <v-btn
                  icon
                  color="primary"
                  v-bind="attrs"
                  v-on="{...dialog, ...tooltip}"
                  :disabled="isUserAdminOfActiveBusiness === true"
                  ref="addAdminButton"
                >
                  <v-icon>mdi-account-plus</v-icon>
                </v-btn>
              </template>
              <span> Add administrator </span>
            </v-tooltip>
          </template>
          <v-card>
            <v-card-title class="headline">
              Are you sure?
            </v-card-title>
            <v-card-text>This user will be able to act as an administrator for {{this.$store.state.user.businessesAdministered.find(x => x.id === this.activeRole.id).name}}</v-card-text>
            <v-card-actions>
              <v-spacer/>
              <v-btn
                ref="cancelButton"
                color="green darken-1"
                text
                @click="addAdminDialog = false"
              >
                No
              </v-btn>
              <v-btn
                ref="confirmButton"
                color="green darken-1"
                text
                @click="addAdminDialog = false; addUserAsAdmin()"
              >
                Yes, promote
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <!--
          "currentUserRole==='user'" is so that normal users cannot see the GAA/DGAA statuses of system admins.
          "user.role==='globalApplicationAdmin' || user.role==='defaultGlobalApplicationAdmin'" is so that if the profile is
          a GAA/DGAA, it will be obvious to the current user viewing the profile that this user is a system admininstrator.
          Note: DGAAs wont be searchable by normal users in the first place so "user.role==='defaultGlobalApplicationAdmin'" is
          just there to remind DGAAs that they are in the DGAA account.
        -->
        <v-tooltip
          v-if="currentUserRole!=='user' && (user.role==='globalApplicationAdmin' || user.role==='defaultGlobalApplicationAdmin')"
          bottom>
          <template #activator="{ on: tooltip }">
            <v-menu
              bottom
              left
            >
              <template #activator="{on: menu, attrs}">
                <v-chip
                  ref="administratorStatus"
                  outlined
                  color="primary"
                  v-on="{...menu, ...tooltip}"
                  v-bind="attrs"
                >
                  <v-icon>mdi-account-tie</v-icon>
                </v-chip>
              </template>
              <!-- put as a v-list item because if in the future we want to be able to add more actions between DGAA/GAA, its easy to add-->
              <!--
                "currentUserRole==='defaultGlobalApplicationAdmin'" is so that only DGAA can see the revoke button
                "user.role!=='defaultGlobalApplicationAdmin'" is so that DGAA wont revoke themselves
              -->
              <v-list
                v-if="currentUserRole==='defaultGlobalApplicationAdmin' && user.role!=='defaultGlobalApplicationAdmin'">
                <v-list-item
                  ref="revokeAdminButton"
                  @click="revokeGAAFromUser(); loader='loadingRevoke'"
                  :loading="loadingRevoke"
                  :disabled="loadingRevoke"
                >
                  Revoke Admin
                </v-list-item>
              </v-list>
            </v-menu>
          </template>
          <span v-if="user.role==='defaultGlobalApplicationAdmin'">Default System Administrator</span>
          <span v-else-if="user.role==='globalApplicationAdmin'">System Administrator</span>
        </v-tooltip>
        <!--
          "user.role==='user'" is so that the DGAA can only make users as a GAA, not current GAAs/DGAAs
          "currentUserRole==='defaultGlobalApplicationAdmin'" is to ensure only DGAAs can make users admin
        -->
        <v-btn
          ref="makeAdminButton"
          v-else-if="user.role==='user' && currentUserRole==='defaultGlobalApplicationAdmin'"
          @click="makeUserAsGAA(); loader='loadingMake'"
          :loading="loadingMake"
          :disabled="loadingMake"
          small
          outlined
        >
          Make Admin
        </v-btn>
      </div>
    </div>

    <v-container fluid>
      <v-row>
        <v-col cols="12" sm="6">
          <h4>Email</h4>
          {{ user.email }}
        </v-col>
        <v-col cols="12" sm="6">
          <h4>Date of Birth</h4>
          {{ dateOfBirth }}
        </v-col>

        <v-col cols="12" sm="6">
          <h4>Phone Number</h4>
          {{ user.phoneNumber }}
        </v-col>
        <v-col class="address" cols="12" sm="6">
          <h4>Home Address</h4>
          {{ insertAddress(user.homeAddress) }}
        </v-col>

        <v-col cols="12">
          <h4>Bio</h4>
          {{ user.bio }}
        </v-col>
        <v-col cols="12">
          <h4>Businesses</h4>
          <span v-for="business in businesses" :key="business.id">
            <router-link :to="'/business/' + business.id">
              <v-chip color="primary" class="link-chip link"> {{ business.name }} </v-chip>
            </router-link>
          </span>
        </v-col>
      </v-row>

    </v-container>
  </v-card>
</template>

<script>
import { getUser, makeBusinessAdmin, removeBusinessAdmin, makeAdmin, revokeAdmin } from '../api/internal';
import UserAvatar from './utils/UserAvatar';
import convertAddressToReadableText from './utils/Methods/convertJsonAddressToReadableText';

export default {
  name: 'ProfilePage',
  data() {
    return {
      /**
       * The user that this profile is for.
       * If null then no profile is displayed
       */
      user: null,
      removeAdminDialog: false,
      addAdminDialog: false,
      //get this session's user's role
      currentUserRole: this.$store.state.user.role,
      //for loader, loadingMake and loadingRevoke is for the loading animation in the makeAdmin/revokeAdmin buttons
      loader: null,
      loadingMake: false,
      loadingRevoke: false
    };
  },

  methods: {
    async addUserAsAdmin() {
      const role = this.activeRole;
      if (!this.user || role?.type !== 'business') return;
      let response = await makeBusinessAdmin(role.id, this.user.id);

      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }
      // Temporarily adds the business to the list of administered businesses.
      this.user.businessesAdministered.push({ id: role.id });

      response = await getUser(this.user.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }

      // Updates the user properly
      this.user = response;
      if (this.user.id === this.$store.state.user?.id) {
        this.$store.commit('setUser', this.user);
      }
    },
    async removeUserAdmin() {
      const role = this.activeRole;
      if (!this.user || role?.type !== 'business') return;
      let response = await removeBusinessAdmin(role.id, this.user.id);

      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }

      this.user.businessesAdministered.filter(business => business.id !== role.id);

      response = await getUser(this.user.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }

      // Updates the user properly
      this.user = response;
      if (this.user.id === this.$store.state.user?.id) {
        this.$store.commit('setUser', this.user);
      }
    },
    //have to use a method here to access the method
    insertAddress(address) {
      if (this.currentUserRole === "defaultGlobalApplicationAdmin" ||
          this.currentUserRole ===  "globalApplicationAdmin" ||
          this.user === this.$store.state.user) {
        return convertAddressToReadableText(address, "full");
      } else {
        return convertAddressToReadableText(address, "partial");
      }
    },
    async makeUserAsGAA() {
      //let the loading for the make admin start
      this.loadingMake = true;
      let response = await makeAdmin(this.user.id);

      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        //let the loading for the make admin stop
        this.loadingMake = false;
        return;
      }

      response = await getUser(this.user.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        //let the loading for the make admin stop
        this.loadingMake = false;
        return;
      }

      // Updates the user properly
      this.user = response;
      if (this.user.id === this.$store.state.user?.id) {
        this.$store.commit('setUser', this.user);
      }
      //let the loading for the make admin stop
      this.loadingMake = false;
    },
    async revokeGAAFromUser() {
      //let the loading for the revoke admin start
      this.loadingRevoke = true;
      let response = await revokeAdmin(this.user.id);

      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        //let the loading for the revoke admin stop
        this.loadingRevoke = false;
        return;
      }

      response = await getUser(this.user.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        //let the loading for the revoke admin stop
        this.loadingRevoke = false;
        return;
      }

      // Updates the user properly
      this.user = response;
      if (this.user.id === this.$store.state.user?.id) {
        this.$store.commit('setUser', this.user);
      }
      //let the loading for the revoke stop
      this.loadingRevoke = false;
    }
  },
  watch: {
    $route: {
      handler() {
        if (this.$route.params.id === undefined) {
          this.user = this.$store.state.user;
          return;
        }

        const id = parseInt(this.$route.params.id);
        if (isNaN(id)) return;

        if (id === this.$store.state.user?.id) {
          this.user = this.$store.state.user;
        } else {
          getUser(id).then((value) => {
            if (typeof value === 'string') {
              this.$store.commit('setError', value);
            } else {
              this.user = value;
            }
          });
        }
      },
      immediate: true,
    },
  },
  computed: {
    activeRole() {
      return this.$store.state.activeRole;
    },
    isActingAsBusiness() {
      return this.activeRole?.type === 'business';
    },
    isUserAdminOfActiveBusiness() {
      if (!this.isActingAsBusiness) return undefined;
      if (this.user === undefined) return undefined;

      return this.user.businessesAdministered.map(business => business.id).includes(this.activeRole.id);
    },
    isViewingOwnProfile() {
      return (this.user?.id === this.$store.state.user?.id);
    },
    createdMsg() {
      if (this.user.created === undefined) return '';

      const now = new Date();
      const createdAt = new Date(this.user.created);
      const parts = createdAt.toDateString().split(' ');

      const diffTime = now - createdAt;
      const diffMonths = Math.floor(diffTime / (1000 * 60 * 60 * 24 * 30));

      return `${parts[2]} ${parts[1]} ${parts[3]} (${diffMonths} months ago)`;
    },
    businesses() {
      return this.user?.businessesAdministered;
    },
    /**
     * Construct a representation of the user's date of birth to display on the profile
     */
    dateOfBirth() {
      if (this.user.dateOfBirth === undefined) return '';

      const dateOfBirth = new Date(this.user.dateOfBirth);
      const parts = dateOfBirth.toDateString().split(' ');
      return `${parts[2]} ${parts[1]} ${parts[3]}`;
    },
    userRole() {
      return this.user.role;
    }
  },
  components: {
    UserAvatar,
  }
};
</script>

<style scoped>
.profile-img {
  margin-top: -116px;
  margin-right: 16px;
}

.action-menu {
  display: flex;
  flex: 1;
  justify-content: flex-end;
}

.body {
  padding: 16px;
  width: 100%;
  margin-top: 140px;
  /* text-align: center; */
}

.top-section {
  display: flex;
  flex-wrap: wrap;
  /* justify-content: center; */
}

.link-chip {
  margin-right: 4px;
}

.address{
  white-space: pre-line;
}
</style>
