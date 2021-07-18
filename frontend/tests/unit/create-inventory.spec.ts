import Vue from "vue";
import Vuex from "vuex";
import Vuetify from "vuetify";
import { createLocalVue, Wrapper, mount } from "@vue/test-utils";
import CreateInventory from "@/components/BusinessProfile/CreateInventory.vue";
import { castMock, flushQueue } from "./utils";
import { getStore, resetStoreForTesting } from "@/store";
import * as api from '@/api/internal';

Vue.use(Vuetify);

// Makes sure that fetching the currency doesn't crash
jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn().mockResolvedValue({
    code: 'NZD',
    name: 'New Zealand dollar',
    symbol: '$',
  }),
}));

jest.mock('@/api/internal', () => ({
  createInventoryItem: jest.fn(),
  getProducts: jest.fn(),
  getBusiness: jest.fn().mockReturnValue({address: {}}), // Makes sure that fetching the currency doesn't crash
}));
const createInventoryItem = castMock(api.createInventoryItem);
const getProducts = castMock(api.getProducts);

getProducts.mockResolvedValue([]);
// Characters that are in the set of letters, numbers, spaces and punctuation.
const validQuantityCharacters = [
  "11",
  "2123",
  "1231423",
  "987654321",
  "123",
  "21233",
  "412345",
  "98765432",
];
// Characters that are in the set of number, decimal, number with decimal
const validPriceCharacters = [
  "0",
  "0.00",
  "1",
  "1.11",
  "99.99",
  "100",
  "9999",
  "001",
];
// Characters that are in the set of number, decimal, number with decimal
const validHugePriceCharacters = [
  "10000",
  "999999",
  "123765",
  "23546.00",
  "888888.12",
];
// Characters that are not a letter, number, space or punctuation.
const invalidCharacters = [
  "\uD83D\uDE02",
  "♔",
  " ",
  ":",
  ",",
  "é",
  "树",
  "A",
  "-1",
  "-99",
];
const testProducts = [
  {
    "id": "WATT-420-BEANS",
    "name": "Watties Baked Beans - 420g can",
    "description": "Baked Beans as they should be.",
    "manufacturer": "Heinz Wattie's Limited",
    "recommendedRetailPrice": 2.2,
    "created": "2021-05-22T02:06:27.767Z",
    "images": [
      {
        "id": 1234,
        "filename": "/media/images/23987192387509-123908794328.png",
        "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
      }
    ]
  }
];
// Characters that are whitespace not including the space character.
const whitespaceCharacters = ["\n", "\t"];

const localVue = createLocalVue();

describe("CreateInventory.vue", () => {
  // Container for the wrapper around CreateInventory
  let appWrapper: Wrapper<any>;

  // Container for the CreateInventory under test
  let wrapper: Wrapper<any>;

  /**
     * Sets up the test CreateInventory instance
     *
     * Because the element we're testing has a v-dialog we need to take some extra sets to make it
     * work.
     */
  beforeEach(() => {
    localVue.use(Vuex);
    const vuetify = new Vuetify();

    // Creating wrapper around CreateInventory with data-app to appease vuetify
    const App = localVue.component("App", {
      components: { CreateInventory },
      template: "<div data-app><CreateInventory/></div>",
    });

    // Put the CreateInventory component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement("div");
    document.body.appendChild(elem);

    resetStoreForTesting();
    let store = getStore();
    store.state.createInventoryDialog = 90;

    appWrapper = mount(App, {
      localVue,
      vuetify,
      store: store,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(CreateInventory);
  });

  /**
     * Executes after every test case.
     *
     * This function makes sure that the CreateInventory component is removed from the global document
     */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
     * Adds all the fields that are required for the create Inventory form to be valid
     *
     * These are:
     * - Inventory shortcode
     * - Item quantity
     * - Item expires
     */
  async function populateRequiredFields() {
    await wrapper.setData({
      productCode: "ABC-XYZ-012-789",
      quantity: 2,
      expires: "2030-05-17",
    });
  }

  /**
     * Populates all fields of the CreateInventory form
     *
     * Which include the inventory's:
     * - Inventory shortcode
     * - Item quantity
     * - Item expires
     * - PricePerItem
     * - TotalPrice
     * - Manufactured
     * - Sell By
     * - Best Before
     */
  async function populateAllFields() {
    await populateRequiredFields();
    await wrapper.setData({
      pricePerItem: "12",
      totalPrice: "20",
      manufactured: "2020-05-17",
      sellBy: "2030-05-17",
      bestBefore: "2030-05-17",
    });
  }
  // `findClose and findCreate` function will only be used when api is implemented
  /**
     * Finds the close button in the CreateProduct form
     *
     * @returns A Wrapper around the close button
     */
  function findCloseButton() {
    const buttons = wrapper.findAllComponents({ name: "v-btn" });
    const filtered = buttons.filter((button) =>
      button.text().includes("Close")
    );
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
     * Finds the create button in the CreateProduct form
     *
     * @returns A Wrapper around the create button
     */
  function findCreateButton() {
    const buttons = wrapper.findAllComponents({ name: "v-btn" });
    const filtered = buttons.filter((button) =>
      button.text().includes("Create")
    );
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the product select dropdown
   *
   * @returns A Wrapper around the product select dropdown
   */
  function findProductSelect() {
    const select = wrapper.findAllComponents({name:"v-select"});
    expect(select.length).toBe(1);
    return select.at(0);
  }

  it("Valid if all required fields are provided", async () => {
    await populateRequiredFields();
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it("Valid if all fields are provided", async () => {
    await populateAllFields();
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  //TRUTHY section follow by ORDER : QUANTITY, PRICE PER ITEM , TOTAL PRICE

  it.each(validQuantityCharacters)(
    'Valid when quantity contain numbers from 1 to 9 digit, QUANTITY = "%s"',
    async (quantity) => {
      await populateAllFields();
      await wrapper.setData({
        quantity,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeTruthy();
    }
  );

  it('Invalid when quantity is 0', async () => {
    await populateAllFields();
    await wrapper.setData({
      quantity: 0
    });
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('Invalid when quantity is -1', async () => {
    await populateAllFields();
    await wrapper.setData({
      quantity: -1
    });
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('Invalid when quantity is -10000', async () => {
    await populateAllFields();
    await wrapper.setData({
      quantity: -10000
    });
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it.each(validPriceCharacters)(
    'Valid when PRICE PER ITEM contain valid price [e.g 999 or 999.99] & <10000, Price per Item =  "%s"',
    async (pricePerItem) => {
      await populateAllFields();
      await wrapper.setData({
        pricePerItem,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeTruthy();
    }
  );

  it.each(validPriceCharacters.concat(validHugePriceCharacters))(
    'Valid when TOTAL PRICE contain valid price [e.g 999 or 999.99] & <10000, TOTAL PRICE =  "%s"',
    async (totalPrice) => {
      await populateAllFields();
      await wrapper.setData({
        totalPrice,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeTruthy();
    }
  );

  //FALSY section follow by ORDER : QUANTITY, PRICE PER ITEM, TOTAL PRICE

  it.each(invalidCharacters.concat(whitespaceCharacters))(
    'invalid if QUANTITY contain space, tab, symbol, other language QUANTITY = "%s"',
    async (quantity) => {
      await populateAllFields();
      await wrapper.setData({
        quantity,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeFalsy();
    }
  );
  it.each(
    invalidCharacters
      .concat(whitespaceCharacters)
      .concat(validHugePriceCharacters)
  )('Invalid if PRICE PER ITEM contain space, tab, symbol, other language, number  >= 10000, PRICE PER ITEM = "%s"', async (pricePerItem) => {
    await populateAllFields();
    await wrapper.setData({
      pricePerItem,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it.each(invalidCharacters.concat(whitespaceCharacters))(
    'Invalid if TOTAL PRICE contain space, tab, symbol, other language, TOTAL PRICE = "%s"',
    async (totalPrice) => {
      await populateAllFields();
      await wrapper.setData({
        totalPrice,
      });
      await Vue.nextTick();
      expect(wrapper.vm.valid).toBeFalsy();
    }
  );

  describe("Date Validation", () => {
    /**
         * Gets todays date and adds on a certain number of years
         *
         * @param years the number of years to add onto today
         * @returns Todays date with x more years
         */
    async function todayPlusYears(years: number) {
      let today = new Date();
      let currentYears = today.getFullYear() + years;
      return currentYears + "-" + today.getMonth() + "-" + today.getDay();
    }

    it("Valid when all date fields are today", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(0);
      let sellByDate = await todayPlusYears(0);
      let bestBeforeDate = await todayPlusYears(0);
      let expiresDate = await todayPlusYears(0);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate,
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Valid when manufactured date before today", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(-1);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkManufacturedDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when manufactured date after today", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(1);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkManufacturedDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when manufactured date before sell by date", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(-1);
      let sellByDate = await todayPlusYears(1);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkManufacturedDateValid();
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when manufactured date after sell by date", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(-1);
      let sellByDate = await todayPlusYears(-2);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkManufacturedDateValid();
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when manufactured date before 1000 AD", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(-1500);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkManufacturedDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when manufactured date after 10000 AD", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(10000);
      await wrapper.setData({
        manufactured: manufacturedDate
      });
      wrapper.vm.checkManufacturedDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when sell by date after today", async () => {
      await populateRequiredFields();
      let sellByDate = await todayPlusYears(1);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when sell by date before today", async () => {
      await populateRequiredFields();
      let sellByDate = await todayPlusYears(-1);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when sell by date after manufactured date", async () => {
      await populateRequiredFields();
      let manufacturedDate = await todayPlusYears(-1);
      let sellByDate = await todayPlusYears(1);
      await wrapper.setData({
        manufactured: manufacturedDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkManufacturedDateValid();
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Valid when sell by date before best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(2);
      let sellByDate = await todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when sell by date after best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(1);
      let sellByDate = await todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when sell by date before 1000 AD", async () => {
      await populateRequiredFields();
      let sellByDate = await todayPlusYears(-1500);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when sell by date after 10000 AD", async () => {
      await populateRequiredFields();
      let sellByDate = await todayPlusYears(10000);
      await wrapper.setData({
        sellBy: sellByDate
      });
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when best before date after today", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when best before date before today", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(-1);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when best before date after sell by date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(2);
      let sellByDate = await todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when best before date before sell by date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(1);
      let sellByDate = await todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        sellBy: sellByDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkSellByDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when best before date before expires date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(1);
      let expiresDate = await todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when best before date after expires date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(2);
      let expiresDate = await todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when best before date before 1000 AD", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(-1500);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when best before date after 10000 AD", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(10000);
      await wrapper.setData({
        bestBefore: bestBeforeDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when expires date after today", async () => {
      await populateRequiredFields();
      let expiresDate = await todayPlusYears(1);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when expires date before today", async () => {
      await populateRequiredFields();
      let expiresDate = await todayPlusYears(-1);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Valid when expires date after best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(1);
      let expiresDate = await todayPlusYears(2);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeFalsy();
    });

    it("Invalid when expires date before best before date", async () => {
      await populateRequiredFields();
      let bestBeforeDate = await todayPlusYears(2);
      let expiresDate = await todayPlusYears(1);
      await wrapper.setData({
        bestBefore: bestBeforeDate,
        expires: expiresDate
      });
      wrapper.vm.checkBestBeforeDateValid();
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when expires date before 1000 AD", async () => {
      await populateRequiredFields();
      let expiresDate = await todayPlusYears(-1500);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });

    it("Invalid when expires date after 10000 AD", async () => {
      await populateRequiredFields();
      let expiresDate = await todayPlusYears(10000);
      await wrapper.setData({
        expires: expiresDate
      });
      wrapper.vm.checkExpiresDateVaild();
      await Vue.nextTick();
      expect(findCreateButton().props().disabled).toBeTruthy();
    });
  });

  //Needs addressed with bug associated with t170
  it("Product dropdown contains a list of products", async ()=>{
    await wrapper.setData({productList:testProducts});
    await Vue.nextTick();
    const selectbox = findProductSelect();
    (selectbox.vm as any).activateMenu();
    await flushQueue();
    expect(appWrapper.text()).toContain("Watties");
  });

  it("Product dropdown contains a search box", async ()=>{
    const selectbox = findProductSelect();
    (selectbox.vm as any).activateMenu();
    await Vue.nextTick();
    const input = selectbox.findComponent({name:"v-text-field"});
    expect(input.exists()).toBeTruthy();
  });

  //Associated with bug on t170
  it('Product search limits results', async ()=>{
    await wrapper.setData({productList:testProducts});
    const selectbox = findProductSelect();
    (selectbox.vm as any).activateMenu();
    await flushQueue();
    expect(appWrapper.text()).toContain("Watties");
    await wrapper.setData({productFilter: "Not watties"});
    await Vue.nextTick();
    expect(appWrapper.text()).not.toContain("Watties");
  });
});
