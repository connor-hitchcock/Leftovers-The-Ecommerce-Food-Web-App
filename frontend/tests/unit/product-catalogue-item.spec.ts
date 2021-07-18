//!!!NOTICE!!!
//ProductImageCarousel will not be tested yet because its part of another task for future stories.
//Decided not to test it for now because whoever that is going to do that task in the future may want
//to redo some details if they desire. If you are doing that task, please refer to ProductCatalogueItem
//and ProductImageCarousel for more details.
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import ProductCatalogueItem from '@/components/cards/ProductCatalogueItem.vue';
// import ProductImageCarousel from "@/components/utils/ProductImageCarousel.vue";
import FullProductDescription from "@/components/utils/FullProductDescription.vue";

import * as api from "@/api/internal";
import { castMock, flushQueue } from './utils';
import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

Vue.use(Vuetify);

jest.mock('@/api/internal', () => ({
  deleteImage: jest.fn(),
}));

const deleteImage = castMock(api.deleteImage);

jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: 'Currency code',
      symbol: 'Currency symbol'
    };
  })
}));

describe('ProductCatalogueItem.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  // The global store to be used
  let store: Store<StoreData>;

  /**
   * Set up to test the routing and whether the Product Catalogue item component shows what is required
   */
  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();

    const app = document.createElement ("div");
    app.setAttribute ("data-app", "true");
    document.body.append (app);

    wrapper = mount(ProductCatalogueItem, {
      localVue,
      vuetify,
      store,
      components: {
        //ProductImageCarousel will not be tested yet because its part of another task.
        // ProductImageCarousel,
        FullProductDescription
      },
      stubs: {
        //stub ProductImageCarousel because its not related to this test for now.
        //remove stub if testing ProductCatalogueItem as a whole
        ProductImageCarousel: true
      },
      mocks: {
        $router: {
          go: () => undefined,
        }
      },
      propsData: {
        product: {
          name: "Some Product",
          description: "Some description",
          created: "2012-05-06",
          manufacturer: "Some Manufacturer",
          recommendedRetailPrice: 100,
          id: "Some Code",
          readMoreActivated: false,
          images: [
            {
              id: 1,
              filename: 'test_filename',
            }
          ],
          countryOfSale: "someCountry",
        },
        businessId: 77,
      }
      //Sets up each test case with some values to ensure the Product Catalogue item component works as intended
    });
    await wrapper.setData({
      currency: {
        code: "Currency code",
        symbol: "Currency symbol"
      },
    });
  });

  /**
  * Tests that the same product name exists as per the set data above
  */
  it("Must contain the product name", () => {
    expect(wrapper.text()).toContain('Some Product');
  });

  /**
  * Tests that the same product description exists as per the set data above
  */
  it("Must contain the product description", () => {
    expect(wrapper.text()).toContain('Some description');
  });

  /**
   * Tests the full user sequence when product description is above 50 characters
   */
  it("Must open dialog box with full product description upon clicking 'Read more...'", async () => {
    await wrapper.setProps({
      product: {
        name: "Some Product",
        description: "Some super long description Some super long description Some super long description Some super long description",
        created: "2012-05-06",
        manufacturer: "Some Manufacturer",
        recommendedRetailPrice: 100,
        id: "Some Code",
        readMoreActivated: false,
        images: [],
        countryOfSale: "someCountry"
      }
    });
    //the description will cut off at the 50th character
    expect(wrapper.text()).toContain(wrapper.vm.product.description.slice(0,50));
    //Full description should not exist
    expect(wrapper.text()).not.toContain(wrapper.vm.product.description);
    Vue.nextTick(() => {
      let productDescriptionComponent = wrapper.findComponent(FullProductDescription);
      //if the component found is not null, means the component exists to be able to read the full description
      expect(productDescriptionComponent).not.toBeNull();
      //value of dialog should be false initially
      expect(productDescriptionComponent.vm.$data.dialog).toBeFalsy();
      //at index 0, the link is the "Read more..." link
      productDescriptionComponent.findAll('a').at(0).trigger("click");
      //wait to let the dialog box load
      Vue.nextTick(() => {
        //now the dialog should be true
        expect(productDescriptionComponent.vm.$data.dialog).toBeTruthy();
        expect(productDescriptionComponent.text()).toContain(wrapper.vm.product.description);
        //at index 1, the link is the "return" link
        productDescriptionComponent.findAll('a').at(1).trigger("click");
        expect(productDescriptionComponent.vm.$data.dialog).toBeFalsy();
      });
    });
  });

  it("Must contain the product date added", () => {
    expect(wrapper.text()).toContain('6 May 2012');
  });

  it("Must contain the product manufacturer", () => {
    expect(wrapper.text()).toContain('Some Manufacturer');
  });

  it("Must contain the product RRP", () => {
    expect(wrapper.text()).toContain(100);
  });

  it("RRP must be formatted with symbol and code", () => {
    expect(wrapper.text()).toContain("Currency symbol100 Currency code");
  });

  it("Must contain the product code", () => {
    expect(wrapper.text()).toContain("Some Code");
  });

  it("When the product's description is not defined, the computed descriptiopn will be 'Not set'", async () => {
    await wrapper.setProps({
      product: {
        name: "Some Product",
        created: "Some Date Added",
        manufacturer: "Some Manufacturer",
        recommendedRetailPrice: 100,
        id: "Some Code",
        readMoreActivated: false,
        images: [],
        countryOfSale: "someCountry"
      }
    });
    expect(wrapper.vm.description).toEqual("Not set");
  });

  it("When the product's description is defined, the computed description will be the given product description", async () => {
    await wrapper.setProps({
      product: {
        name: "Some Product",
        created: "Some Date Added",
        description: "Some Description",
        manufacturer: "Some Manufacturer",
        recommendedRetailPrice: 100,
        id: "Some Code",
        readMoreActivated: false,
        images: [],
        countryOfSale: "someCountry"
      }
    });
    expect(wrapper.vm.description).toEqual("Some Description");
  });

  it("When the product's manufacturer is not defined, the computed manufacturer will be 'Not set'", async () => {
    await wrapper.setProps({
      product: {
        name: "Some Product",
        description: "Some Description",
        created: "Some Date Added",
        recommendedRetailPrice: 100,
        id: "Some Code",
        readMoreActivated: false,
        images: [],
        countryOfSale: "someCountry"
      }
    });
    expect(wrapper.vm.manufacturer).toEqual("Not set");
  });

  it("When the product's manufacturer is defined, the computed manufacturer will be the given product manufacturer", async () => {
    await wrapper.setProps({
      product: {
        name: "Some Product",
        created: "Some Date Added",
        description: "Some Description",
        manufacturer: "Some Manufacturer",
        recommendedRetailPrice: 100,
        id: "Some Code",
        readMoreActivated: false,
        images: [],
        countryOfSale: "someCountry"
      }
    });
    expect(wrapper.vm.manufacturer).toEqual("Some Manufacturer");
  });

  it("When the product's RRP is not defined, the computed RRP will be 'Not set'", async () => {
    await wrapper.setProps({
      product: {
        name: "Some Product",
        created: "Some Date Added",
        description: "Some Description",
        manufacturer: "Some Manufacturer",
        id: "Some Code",
        readMoreActivated: false,
        images: [],
        countryOfSale: "someCountry"
      }
    });
    expect(wrapper.vm.recommendedRetailPrice).toEqual("Not set");
  });

  it("When the product's RRP is defined, the computed RRP will be the given product RRP with the items currency symbol and code", async () => {
    await wrapper.setProps({
      product: {
        name: "Some Product",
        created: "Some Date Added",
        description: "Some Description",
        manufacturer: "Some Manufacturer",
        recommendedRetailPrice: 100,
        id: "Some Code",
        readMoreActivated: false,
        images: [],
        countryOfSale: "someCountry"
      }
    });
    expect(wrapper.vm.recommendedRetailPrice).toEqual("Currency symbol100 Currency code");
  });

  it('If "delete-image" is emitted from ProductImageCarousel then the corresponding image is deleted', () => {
    deleteImage.mockResolvedValue(undefined);
    wrapper.findComponent({name: 'ProductImageCarousel' }).vm.$emit('delete-image', 33);
    expect(deleteImage).toBeCalledWith(77, 'Some Code', 33);
  });

  it('If "delete-image" handler fails to delete the image then the global error should be set', async () => {
    deleteImage.mockResolvedValue('test_error_message');
    wrapper.findComponent({name: 'ProductImageCarousel' }).vm.$emit('delete-image', 33);
    await flushQueue();
    expect(store.state.globalError).toBe('test_error_message');
  });
});
