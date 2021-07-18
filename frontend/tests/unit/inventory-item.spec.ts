
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import InventoryItem from '@/components/cards/InventoryItem.vue';
// import ProductImageCarousel from "@/components/utils/ProductImageCarousel.vue";
import FullProductDescription from "@/components/utils/FullProductDescription.vue";

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

Vue.use(Vuetify);

jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: "test_currency_code",
      symbol: "test_currency_symbol"
    };
  })
}));


describe('InventoryItem.vue', () => {
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

    wrapper = mount(InventoryItem, {
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
        //remove stub if testing InventoryItem as a whole
        ProductImageCarousel: true
      },
      mocks: {
        $router: {
          go: () => undefined,
        }
      },
      propsData: {
        inventoryItem: {
          product: {
            name: "test_product_name",
            description: "test_product_description",
            created: "2021-05-12",
            manufacturer: "test_product_manufacturer",
            recommendedRetailPrice: 100,
            id: "test_product_code",
            images: [],
            countryOfSale: "test_country",
          },
          quantity: 4,
          pricePerItem: 7,
          totalPrice: 99,
          manufactured: "2021-05-11",
          sellBy: "2021-05-13",
          bestBefore: "2021-05-14",
          expires: "2021-05-15"
        },
        businessId: 77,
      }
      //Sets up each test case with some values to ensure the Product Catalogue item component works as intended
    });
    await wrapper.setData({
      currency: {
        code: "test_currency_code",
        symbol: "test_currency_symbol"
      },
    });
  });

  it("Must contain the product name", () => {
    expect(wrapper.text()).toContain('test_product_name');
  });

  it("Must contain the product description", () => {
    expect(wrapper.text()).toContain('test_product_description');
  });

  it("Must open dialog box with full product description upon clicking 'Read more...'", async () => {
    await wrapper.setProps({
      inventoryItem: {
        product: {
          name: "test_product_name",
          description: "Some super long description Some super long description Some super long description Some super long description",
          created: "2021-05-12",
          manufacturer: "test_product_manufacturer",
          recommendedRetailPrice: 100,
          id: "test_product_code",
          images: [],
          countryOfSale: "test_country",
        }
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

  it("Must contain the product manufacturer", () => {
    expect(wrapper.text()).toContain('test_product_manufacturer');
  });

  it("Must contain the product code", () => {
    expect(wrapper.text()).toContain("test_product_code");
  });

  it('Must contain the quantity', () => {
    expect(wrapper.text()).toContain("4");
  });

  it('Must contain the price per item correctly formatted', () => {
    expect(wrapper.text()).toContain("test_currency_symbol7 test_currency_code");
  });

  it('Must contain the total price correctly formatted', () => {
    expect(wrapper.text()).toContain("test_currency_symbol99 test_currency_code");
  });

  it('Must contain manufactured date', () => {
    expect(wrapper.text()).toContain("11 May 2021");
  });

  it('Must contain created date', () => {
    expect(wrapper.text()).toContain("12 May 2021");
  });

  it('Must contain sell by date', () => {
    expect(wrapper.text()).toContain("13 May 2021");
  });

  it('Must contain best before date', () => {
    expect(wrapper.text()).toContain("14 May 2021");
  });

  it('Must contain expiry date', () => {
    expect(wrapper.text()).toContain("15 May 2021");
  });

  it('Must match snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });

  it('Must have a button to create a sale', () => {
    const button = wrapper.findComponent({ref:'createSaleItemButton'});
    expect(button.exists()).toBeTruthy();
  });

  it('Clicking the create sale button opens a dialog to create a sale', async () => {
    const button = wrapper.findComponent({ref:'createSaleItemButton'});
    expect(store.state.createSaleItemDialog).toBeUndefined();
    button.trigger('click');
    await Vue.nextTick();
    expect(store.state.createSaleItemDialog).not.toBeUndefined();
  });
});
