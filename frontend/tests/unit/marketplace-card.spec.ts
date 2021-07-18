
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import MarketplaceCard from '@/components/cards/MarketplaceCard.vue';

import { User } from '@/api/internal';

Vue.use(Vuetify);

const testUser: User = {
  id: 2,
  firstName: 'test_firstname',
  lastName: 'test_lastname',
  email: 'test_email',
  homeAddress: { country: 'test_country', city: 'test_city', district: 'test_district'},
};

const testMarketplaceCard = {
  id: 1,
  creator: testUser,
  section: 'ForSale',
  created: '2021-03-10',
  title: 'test_card_title',
  description: 'test_card_description',
  keywords: [{id: 3, name: 'test_keyword_1'}, {id: 4, name: 'test_keyword_2'}],
};

describe('MarketplaceCard.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(() => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    wrapper = mount(MarketplaceCard as any, {
      localVue,
      vuetify,
      propsData: {
        content: testMarketplaceCard,
      }
    });
  });

  it('Must match snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });

  it("Must contain the creator name", () => {
    expect(wrapper.text()).toContain('By test_firstname test_lastname');
  });

  it("Must contain the creator address with suburb and city if provided", () => {
    expect(wrapper.text()).toContain('From test_district, test_city');
  });

  it("Must contain the creator address with city and country if provided", async () => {
    await wrapper.setData({
      content: {
        creator: {
          homeAddress: {
            district: undefined,
            city: 'test_city',
            country: 'test_country',
          }
        }
      },
    });
    await Vue.nextTick();
    expect(wrapper.text()).toContain('From test_city, test_country');
  });

  it("Must contain the creator country if only field provided", async () => {
    await wrapper.setData({
      content: {
        creator: {
          homeAddress: {
            district: undefined,
            city: undefined,
            country: 'test_country',
          }
        }
      },
    });
    await Vue.nextTick();
    expect(wrapper.text()).toContain('From test_country');
  });

  it("Must contain the title", () => {
    expect(wrapper.text()).toContain('test_card_title');
  });

  it("Must contain the description", () => {
    expect(wrapper.text()).toContain('test_card_description');
  });

  it("Must contain keyword names", () => {
    expect(wrapper.text()).toContain("test_keyword_1");
    expect(wrapper.text()).toContain("test_keyword_2");
  });

  it("Must contain posted date", () => {
    expect(wrapper.text()).toContain('Posted 10 Mar 2021');
  });
});
