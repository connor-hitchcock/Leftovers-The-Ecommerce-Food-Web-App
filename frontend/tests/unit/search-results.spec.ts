import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';
import SearchResults from '@/components/SearchResults.vue';
import SearchResultItem from '@/components/cards/SearchResultItem.vue';
import { User } from '@/api/internal';
import * as api from '@/api/internal';
import { castMock, flushQueue } from './utils';

jest.mock('@/api/internal', () => ({
  search: jest.fn(),
  getSearchCount: jest.fn(),
}));

// Debounce adds a delay on updates to search query that we need to get rid of
jest.mock('@/utils', () => ({
  debounce: (func: (() => void)) => func,
}));

const search = castMock(api.search);
const getSearchCount = castMock(api.getSearchCount);

Vue.use(Vuetify);

const localVue = createLocalVue();
localVue.use(Vuex);

const RESULTS_PER_PAGE = 10;

/**
 * Creates a list of unique test users
 *
 * @param count Number of users to create
 * @returns List of test users
 */
function createTestUsers(count: number) {
  let result: User[] = [];

  for (let i = 0; i<count; i++) {
    result.push({
      id: i,
      firstName: 'test_firstname' + i,
      lastName: 'test_lastname' + i,
      email: 'test_email' + i,
      dateOfBirth: '1/1/1900',
      homeAddress: { country: 'test_country' + i },
    });
  }
  return result;
}

describe('SearchResults.vue', () => {
  // Container for the SearchResults under test
  let wrapper: Wrapper<any>;

  /**
   * Creates the wrapper for the SearchResults component.
   * This must be called before using the SearchResults wrapper.
   */
  function createWrapper() {
    wrapper = mount(SearchResults, {
      stubs: ['router-link', 'router-view'],
      mocks: {
        $route: {
          query: {
            query: 'test_query',
          },
        },
      },
      localVue,
      vuetify: new Vuetify(),
    });
  }

  /**
   * Sets the mock api results.
   *
   * @param users Users on the current page to use for the mock results
   * @param testCount The mock number of total users for this search
   */
  function setResults(users: User[], totalCount?: number) {
    search.mockResolvedValue(users);
    getSearchCount.mockResolvedValue(totalCount !== undefined ? totalCount : users.length);
  }

  /**
   * Finds the error message component if it exists
   *
   * @returns Wrapper for the error component if it exists
   */
  function findErrorBox() {
    return wrapper.findComponent({name: 'v-alert'});
  }

  it('The search query passed in from the url is searched', () => {
    setResults(createTestUsers(5));
    createWrapper();
    expect(search).toBeCalledWith('test_query', 1, RESULTS_PER_PAGE, 'relevance', false);
  });

  it('The search results should be displayed somewhere', async () => {
    let users = createTestUsers(5);
    setResults(users);
    createWrapper();
    // Flush queue is used instead of Vue.nextTick() since this will wait for everything to finish
    // instead of a single event.
    await flushQueue();
    const shownUsers = wrapper
      .findAllComponents(SearchResultItem)
      .wrappers
      .map(searchResult => searchResult.props().user);
    expect(shownUsers).toStrictEqual(users);
  });

  it('If there is an error then the error should be displayed', async () => {
    search.mockResolvedValue('test_error');
    createWrapper();
    await flushQueue();
    expect(findErrorBox().text()).toEqual('test_error');
  });

  it('If the error is dismissed then the error should disappear', async () => {
    search.mockResolvedValue('test_error');
    createWrapper();
    await flushQueue();
    // Finds dismiss button and clicks it
    findErrorBox().findComponent({name: 'v-btn' }).trigger('click');
    await Vue.nextTick();
    expect(findErrorBox().exists()).toBeFalsy();
  });

  it('If there are no results then there should be a message informing the user of that', async () => {
    setResults([]);
    createWrapper();
    await flushQueue();
    expect(wrapper.text()).toContain('There are no results to show');
  });

  it('If there are results then there should be a message informing the user how many', async () => {
    setResults(createTestUsers(RESULTS_PER_PAGE), 100);
    createWrapper();
    await flushQueue();
    expect(wrapper.text()).toContain(`Displaying 1 - ${RESULTS_PER_PAGE} of 100 results`);
  });

  it('The search query should update as the search box is modified', async () => {
    setResults(createTestUsers(5));
    createWrapper();
    await flushQueue();
    // Update the search box
    const searchBox = wrapper.findComponent({ name: 'v-text-field' });
    await searchBox.findAll('input').at(0).setValue('new_test_query');
    expect(search).lastCalledWith('new_test_query', 1, RESULTS_PER_PAGE, 'relevance', false);
  });

  it('If there are many pages then there should be a pagination component with many pages', async () => {
    setResults(createTestUsers(RESULTS_PER_PAGE), 100);
    createWrapper();
    await flushQueue();
    let pagination = wrapper.findComponent({ name: 'v-pagination' });
    expect(pagination.props().length).toBe(Math.ceil(100 / RESULTS_PER_PAGE));
    expect(pagination.props().disabled).toBe(false);
    expect(pagination.props().value).toBe(1);
  });
});
