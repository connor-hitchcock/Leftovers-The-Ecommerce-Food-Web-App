import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import CreateCard from '@/components/marketplace/CreateCard.vue';
import {castMock} from "./utils";
import * as api from '@/api/internal';
import { getStore, resetStoreForTesting } from '@/store';
import {User} from "@/api/internal";

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @returns The generated user
 */
function makeTestUser(userId: number) {
  let user: User = {
    id:  userId,
    firstName: 'test_firstname' + userId,
    lastName: 'test_lastname' + userId,
    nickname: 'test_nickname' + userId,
    email: 'test_email' + userId,
    bio: 'test_biography' + userId,
    phoneNumber: 'test_phone_number' + userId,
    dateOfBirth: '1/1/1900',
    created: '1/5/2005',
    homeAddress: {
      streetNumber: 'test_street_number',
      streetName: 'test_street1',
      city: 'test_city',
      region: 'test_region',
      postcode: 'test_postcode',
      district: 'test_district',
      country: 'test_country' + userId
    },
    businessesAdministered: [],
  };


  return user;
}

jest.mock('@/api/internal', () => ({
  getKeywords: jest.fn(),
  createMarketplaceCard: jest.fn(),
}));

const getKeywords = castMock(api.getKeywords);
const createMarketplaceCard = castMock(api.createMarketplaceCard);
Vue.use(Vuetify);
const localVue = createLocalVue();

//Characters that are in the set of letters, numbers, spaces and punctuation
const validCharacters = [
  "A",
  "7",
  " ",
  "树",
  ":",
  ",",
  "é",
];

// Characters that are not a letter, number, space or punctuation.
const invalidCharacters = [
  "\uD83D\uDE02",
  "♔",
];

/**
   * Sets up the test CreateCard instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
describe('CreateCard.vue', () => {
  // Container for the wrapper around CreateCard
  let appWrapper: Wrapper<any>;

  // Container for the CreateCard under test
  let wrapper: Wrapper<any>;

  beforeEach(() => {
    const vuetify = new Vuetify();
    localVue.use(Vuex);
    resetStoreForTesting();
    let store = getStore();
    store.state.user = makeTestUser(1); // log in as user 1
    store.state.createMarketplaceCardDialog = store.state.user;
    // Creating wrapper around CreateCard with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { CreateCard },
      template: '<div data-app><CreateCard/></div>',
    });

    // Put the CreateCard component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    getKeywords.mockResolvedValue([]);

    // We have to mock the $router.go method to prevent errors.
    appWrapper = mount(App, {
      stubs: ['router-link', 'router-view'],
      mocks: {
        $router: {
          go: () => {return;},
        }
      },
      localVue,
      vuetify,
      attachTo: elem,
      store: store,
    });

    wrapper = appWrapper.getComponent(CreateCard);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the CreateCard component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Finds the cancel button in the CreateCard form
   *
   * @returns A Wrapper around the cancel button
   */
  function findCancelButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Cancel'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the create button in the CreateCard form
   *
   * @returns A Wrapper around the create button
   */
  function findCreateButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Create Card'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  it('Valid if all required fields are provided', async () => {
    await wrapper.setData({
      title: "Title",
      selectedSection: "ForSale",
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
    expect(findCreateButton().props().disabled).toBeFalsy();
  });

  it('Valid if all fields are provided', async () => {
    await wrapper.setData({
      title: "Title",
      selectedSection: "ForSale",
      description: "Description"
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
    expect(findCreateButton().props().disabled).toBeFalsy();
  });

  it('Invalid if title not provided', async () => {
    await wrapper.setData({
      title: "",
      selectedSection: "ForSale",
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it.each(validCharacters)('Valid if title contains valid characters %s', async (character) => {
    await wrapper.setData({
      title: character,
      selectedSection: "ForSale",
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
    expect(findCreateButton().props().disabled).toBeFalsy();
  });

  it.each(invalidCharacters)('Invalid if title contains invalid characters %s', async (character) => {
    await wrapper.setData({
      title: character,
      selectedSection: "ForSale",
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('Invalid if title has over 50 characters', async () => {
    await wrapper.setData({
      title: "a".repeat(51),
      selectedSection: "ForSale",
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it.each(validCharacters)('Valid if description contains valid characters %s', async (character) => {
    await wrapper.setData({
      title: "Title",
      selectedSection: "ForSale",
      description: character
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
    expect(findCreateButton().props().disabled).toBeFalsy();
  });

  it.each(invalidCharacters)('Invalid if description contains invalid characters %s', async (character) => {
    await wrapper.setData({
      title: "Title",
      selectedSection: "ForSale",
      description: character
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('Invalid if description has over 200 characters', async () => {
    await wrapper.setData({
      title: "Title",
      selectedSection: "ForSale",
      description: "a".repeat(201),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('Invalid if section not provided', async () => {
    await wrapper.setData({
      title: "Title",
      selectedSection: undefined,
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });
});