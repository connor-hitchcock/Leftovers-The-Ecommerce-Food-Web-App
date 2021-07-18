import Vue from 'vue';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';
import App from '@/App.vue';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

Vue.use(Vuetify);

const localVue = createLocalVue();

localVue.use(VueRouter);
const router = new VueRouter();

describe('GlobalError', () => {
  // Container for the App under test
  let wrapper: Wrapper<any>;
  let store: Store<StoreData>;

  /**
   * Sets up a test App instance.
   */
  beforeEach(() => {
    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();

    wrapper = mount(App, {
      localVue,
      store,
      router,
      vuetify: new Vuetify(),
    });
  });

  /**
   * Finds the current error message component if it exists.
   *
   * @returns Error message component wrapper or if doesn't exist an error wrapper
   */
  function findErrorBox() {
    // So long as we don't add a new v-alert component to App.vue, then this selector should be sufficient.
    return wrapper.findComponent({name: 'v-alert'});
  }

  /**
   * Tests that when the app is launched there is no error message
   */
  it('Initially no error message is displayed', () => {
    expect(findErrorBox().exists()).toBeFalsy();
  });

  /**
   * Tests that when an error is generated then it is shown
   */
  it('If there is an error then there should be an error component with the right message', async () => {
    store.commit('setError', 'test_error_message');
    await Vue.nextTick();
    let errorBox = findErrorBox();
    expect(errorBox.exists()).toBeTruthy();
    expect(errorBox.text()).toBe('test_error_message');
  });

  /**
   * Tests that when an error is cleared then there is no error shown
   */
  it('If the error is cleared then there should be no error component', async () => {
    store.commit('setError', 'test_error_message');
    await Vue.nextTick();
    store.commit('clearError');
    await Vue.nextTick();
    expect(findErrorBox().exists()).toBeFalsy();
  });

  /**
   * Tests that when the user changes pages then there the error disappears
   */
  it('If the router changes pages then the error component should disappear', async () => {
    store.commit('setError', 'test_error_message');
    await Vue.nextTick();
    expect(findErrorBox().exists()).toBeTruthy();
    router.push('/unimportant');
    await Vue.nextTick();
    expect(findErrorBox().exists()).toBeFalsy();
    expect(store.state.globalError).toBeNull();
  });

  /**
   * Tests that when the user dismisses an error then the error disappears
   */
  it('If there is an error then we should be able to dismiss it', async () => {
    store.commit('setError', 'test_error_message');
    await Vue.nextTick();
    let errorBox = findErrorBox();
    expect(errorBox.exists()).toBeTruthy();
    errorBox.findComponent({name: 'v-btn' }).trigger('click');
    await Vue.nextTick();
    errorBox = findErrorBox();
    expect(errorBox.exists()).toBeFalsy();
    expect(store.state.globalError).toBeNull();
  });
});
