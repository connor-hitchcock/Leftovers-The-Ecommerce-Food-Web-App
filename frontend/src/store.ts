import { User, Business, getUser, login, InventoryItem } from './api/internal';
import Vuex, { Store, StoreOptions } from 'vuex';
import { COOKIE, deleteCookie, getCookie, isTesting, setCookie } from './utils';

type UserRole = { type: "user" | "business", id: number };
type SaleItemInfo = { businessId: number, inventoryItem: InventoryItem };

export type StoreData = {
  /**
   * Object representing the current logged in user.
   * If null then no user is logged in
   */
  user: User | null,

  /**
   * The current role the user is acting as.
   * If acting as user then the user is shown content related to their personal account.
   * If acting as a business then the user is shown content related to their business.
   */
  activeRole: UserRole | null,

  /**
   * The global error message that is displayed at the top of the screen.
   *
   * This is the most intruding method for displaying errors and should only be used for errors that
   * must interrupt the application flow.
   * Otherwise error messages should be displayed closer to where they are generated from.
   */
  globalError: string | null,

  /**
   * Whether or not the dialog for registering a business is being shown.
   */
  createBusinessDialogShown: boolean,
  /**
   * The current business the create product dialog is being applied to.
   * If undefined then the create product dialog is hidden.
   */
  createProductDialogBusiness: number | undefined,
  /**
   * Whether or not the dialog for registering a business is being shown.
   */
  createInventoryDialog: number | undefined,
  /**
   * Whether or not the dialog for registering a business is being shown.
   */
  createSaleItemDialog: SaleItemInfo | undefined,
  /**
   * Whether or not the dialog for creating a marketplace card is being shown.
   */
  createMarketplaceCardDialog: User | undefined,
};

function createOptions(): StoreOptions<StoreData> {
  return {
    state: {
      user: null,
      activeRole: null,
      globalError: null,
      createBusinessDialogShown: false,
      createProductDialogBusiness: undefined,
      createInventoryDialog: undefined,
      createSaleItemDialog: undefined,
      createMarketplaceCardDialog: undefined,
    },
    mutations: {
      setUser(state, payload: User) {
        state.user = payload;

        // Ensures that when we log in we always have a role.
        state.activeRole = { type: "user", id: payload.id };

        // If the payload contains a user ID, user is now logged in. Set their session cookie.
        if (payload.id) {
          deleteCookie(COOKIE.USER.toUpperCase());
          deleteCookie(COOKIE.USER.toLowerCase());
          setCookie(COOKIE.USER, payload.id);
        }
      },
      /**
       * Displays an error message at the top of the screen.
       *
       * This is the least perferred method for displaying errors.
       * Since error messages should be displayed closer to where they are generated from.
       *
       * @param state Current state
       * @param error Error message to display
       */
      setError(state, error: string) {
        state.globalError = error;
      },
      /**
       * Dismisses the current error message that is displayed.
       * This function is only expected to be called from the global error message component.
       *
       * @param state Current state
       */
      clearError(state) {
        state.globalError = null;
      },

      logoutUser(state) {
        state.user = null;
        deleteCookie(COOKIE.USER);
      },

      /**
       * Creates a modal create business dialog
       *
       * @param state Current store state
       */
      showCreateBusiness(state) {
        state.createBusinessDialogShown = true;
      },

      /**
       * Hides the create business dialog
       *
       * @param state Current store state
       */
      hideCreateBusiness(state) {
        state.createBusinessDialogShown = false;
      },

      /**
       * Creates a modal create product dialog for adding a product to the provided business
       *
       * @param state Current store state
       * @param businessId Business to create the product for
       */
      showCreateProduct(state, businessId: number) {
        state.createProductDialogBusiness = businessId;
      },

      /**
       * Hides the create product dialog
       *
       * @param state Current store state
       */
      hideCreateProduct(state) {
        state.createProductDialogBusiness = undefined;
      },
      /**
       * Creates a modal create inventory dialog for adding a inventory item to the provided business
       *
       * @param state Current store state
       * @param businessId Business to create the product for
       */
      showCreateInventory(state, businessId: number) {
        state.createInventoryDialog = businessId;
      },
      /**
       * Hides the create inventory dialog
       *
       * @param state Current store state
       */
      hideCreateInventory(state) {
        state.createInventoryDialog = undefined;
      },
      /**
       * Creates a modal create inventory dialog for adding a sale item to the provided business
       *
       * @param state Current store state
       * @param businessId Business to create the sale item for
       */
      showCreateSaleItem(state, saleItemInfo: SaleItemInfo) {
        state.createSaleItemDialog = saleItemInfo;
      },
      /**
       * Hides the create inventory dialog
       *
       * @param state Current store state
       */
      hideCreateSaleItem(state) {
        state.createSaleItemDialog = undefined;
      },

      /**
       * Creates a modal create card dialog for addin a card to the marketplace
       *
       * @param state Current store state
       */
      showCreateMarketplaceCard(state, user : User) {
        state.createMarketplaceCardDialog = user;
      },
      /**
       * Hides the create marketplace card dialog
       *
       * @param state Current store state
       */
      hideCreateMarketplaceCard(state) {
        state.createMarketplaceCardDialog = undefined;
      },

      /**
       * Sets the current user role
       *
       * @param state Current store state
       * @param role Role to act as
       */
      setRole(state, role: UserRole) {
        setCookie('role', JSON.stringify(role));
        state.activeRole = role;
      }
    },
    getters: {
      isLoggedIn(state) {
        return state.user !== null;
      },
      role(state) {
        return state.user?.role;
      },
    },
    actions: {
      /**
       * Attempts to automatically log in the provided user id with the current authentication cookies.
       * Will also set the current role to the previously selected role.
       *
       * @param context The store context
       * @param userId The userId to try to login as
       */
      async autoLogin(context, userId: number) {
        const response = await getUser(userId);
        if (typeof response === 'string') {
          //context.commit('setError', response);
          return;
        }
        context.commit('setUser', response);

        let rawRole = getCookie('role');
        if (rawRole !== null) {
          rawRole = rawRole.split('=')[1];

          const role: UserRole = JSON.parse(rawRole);
          // We should already be logged in at this point, so this should be valid
          const user = context.state.user!;

          if (role.type === 'user') {
            if (user.id === role.id) {
              context.state.activeRole = role;
            } else {
              console.warn('Previous role id does not match current user id');
            }
          } else if (role.type === 'business') {
            let success = false;
            for (const business of (user.businessesAdministered || [])) {
              if (business.id !== role.id) continue;
              context.state.activeRole = role;
              success = true;
              break;
            }
            if (!success) {
              console.warn(`Previous role id does not match a administered business id=${role.id}`);
            }
          } else {
            console.error(`Unknown role type: "${role.type}"`);
          }
        }
      },
      /**
       * Attempts to log in the given user.
       * This will set the cookies and with authenticate future requests.
       *
       * @param context The current context
       * @param Object containing the login credentials
       * @returns Undefined if successful or a string error message
       */
      async login(context, { email, password }) {
        let userId = await login(email, password);
        if (typeof userId === 'string') {
          return userId;
        }
        let user = await getUser(userId);
        if (typeof user === 'string') {
          return user;
        }
        context.commit('setUser', user);

        return undefined;
      }
    }
  };
}


let store: Store<StoreData>;
if (!isTesting()) {
  // If we're in a test enviroment then Vue.use(Vuex) won't have been called yet.
  store = new Vuex.Store(createOptions());
}

/**
 * Resets the global store to the initial state.
 * This function is only to be used in the test enviroment
 */
export function resetStoreForTesting() {
  if (!isTesting()) throw new Error('This function should only be called when testing');
  store = new Vuex.Store(createOptions());
}

/**
 * Gets the global Vuex store.
 *
 * @returns The global Vuex store
 */
export function getStore() {
  return store;
}
