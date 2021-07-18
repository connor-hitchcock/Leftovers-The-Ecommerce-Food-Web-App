import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';
import ProductCatalogue from '@/components/ProductCatalogue.vue';
import ProductCatalogueItem from '@/components/cards/ProductCatalogueItem.vue';
import { Product } from '@/api/internal';
import * as api from '@/api/internal';
import { castMock, flushQueue } from './utils';

jest.mock('@/api/internal', () => ({
  getProducts: jest.fn(),
  getProductCount: jest.fn()
}));

const getProducts = castMock(api.getProducts);
const getProductCount = castMock(api.getProductCount);

Vue.use(Vuetify);

const localVue = createLocalVue();
localVue.use(Vuex);

const RESULTS_PER_PAGE = 10;

/**
 * Creates a list of unique test products
 *
 * @param count Number of products to create
 * @returns List of test products
 */
function createTestProducts(count: number) {
  let result: Product[] = [];

  for (let i = 0; i<count; i++) {
    result.push({
      id: 'product_code' + i,
      name: 'product_name' + i,
      description: 'product_description' + i,
      manufacturer: 'product_manufacturer' + i,
      recommendedRetailPrice: i,
      images: [],
    });
  }
  return result;
}

describe('ProductCatalogue.vue', () => {
  // Container for the ProductCatalogue under test
  let wrapper: Wrapper<any>;
  let numberOfTestProducts: number;

  getProductCount.mockImplementation(async businessId => {
    return numberOfTestProducts;
  });

  /**
   * Creates the wrapper for the ProductCatalogue component.
   * This must be called before using the ProductCatalogue wrapper.
   */
  function createWrapper() {
    wrapper = mount(ProductCatalogue, {
      stubs: ['router-link', 'router-view', 'ProductCatalogueItem'],
      mocks: {
        $route: {
          params: {
            id: '100',
          }
        },
      },
      localVue,
      vuetify: new Vuetify(),
    });
  }

  /**
   * Sets the mock api results.
   *
   * @param products Products on the current page to use for the mock results
   */
  function setResults(products: Product[], totalCount?: number) {
    getProducts.mockResolvedValue(products);
    getProductCount.mockResolvedValue(totalCount !== undefined ? totalCount : products.length);
  }

  /**
   * Finds the error message component if it exists
   *
   * @returns Wrapper for the error component if it exists
   */
  function findErrorBox() {
    return wrapper.findComponent({name: 'v-alert'});
  }

  beforeEach(() => {
    numberOfTestProducts = 5;
  });
  /**
   * Tests that when initially opened that the products are queried
   */
  it('The products from the business id are queried', () => {
    setResults(createTestProducts(numberOfTestProducts));
    createWrapper();
    expect(getProducts).toBeCalledWith(100, 1, RESULTS_PER_PAGE, 'productCode', false);
  });

  it('The search results should be displayed somewhere', async () => {
    let products = createTestProducts(numberOfTestProducts);
    setResults(products);
    createWrapper();
    // Flush queue is used instead of Vue.nextTick() since this will wait for everything to finish
    // instead of a single event.
    await flushQueue();
    const shownProducts = wrapper
      .findAllComponents(ProductCatalogueItem)
      .wrappers
      .map(searchResult => searchResult.props().product);
    expect(shownProducts).toStrictEqual(products);
  });

  it('If there is an error then the error should be displayed', async () => {
    getProducts.mockResolvedValue('test_error');
    createWrapper();
    await flushQueue();
    expect(findErrorBox().text()).toEqual('test_error');
  });

  it('If the error is dismissed then the error should disappear', async () => {
    getProducts.mockResolvedValue('test_error');
    createWrapper();
    await flushQueue();
    // Finds dismiss button and clicks it
    findErrorBox().findComponent({name: 'v-btn' }).trigger('click');
    await Vue.nextTick();
    expect(findErrorBox().exists()).toBeFalsy();
  });

  it('If there are many pages then there should be a pagination component with many pages', async () => {
    setResults(createTestProducts(RESULTS_PER_PAGE), 100);
    createWrapper();
    await flushQueue();
    let pagination = wrapper.findComponent({ name: 'v-pagination' });
    expect(pagination.props().length).toBe(Math.ceil(100 / RESULTS_PER_PAGE));
    expect(pagination.props().disabled).toBe(false);
    expect(pagination.props().value).toBe(1);
  });

  it('If there are results then there should be a message informing the buisness admin how many', async () => {
    setResults(createTestProducts(RESULTS_PER_PAGE), 100);
    createWrapper();
    await flushQueue();
    expect(wrapper.text()).toContain(`Displaying 1 - ${RESULTS_PER_PAGE} of 100 results`);
  });

  it('If there are no results then there should be a message informing the buisness admin of that', async () => {
    setResults([]);
    createWrapper();
    await flushQueue();
    expect(wrapper.text()).toContain('There are no results to show');
  });
});
