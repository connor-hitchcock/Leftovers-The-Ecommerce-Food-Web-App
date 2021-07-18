import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import ProductImageCarousel from '@/components/utils/ProductImageCarousel.vue';

Vue.use(Vuetify);

const localVue = createLocalVue();

describe('ProductImageCarousel.vue', () => {
  // Container for the wrapper around ProductImageCarousel
  let appWrapper: Wrapper<any>;

  // Container for the ProductImageCarousel under test
  let wrapper: Wrapper<any>;

  /**
   * Sets up the test ProductImageCarousel instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(async () => {
    localVue.use(Vuex);
    const vuetify = new Vuetify();

    // Creating wrapper around ProductImageCarousel with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { ProductImageCarousel },
      template: `<div data-app><ProductImageCarousel :productImages="[{id: 7, filename:'test_filename'}, {id: 11, filename: 'test_filename2'}]" :showControls="showControls"/></div>`,
    });

    // Put the ProductImageCarousel component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
      props: ['showControls'],
      propsData: {
        showControls: true,
      }
    });

    wrapper = appWrapper.getComponent(ProductImageCarousel);

    await wrapper.setData({
      dialog: true,
    });
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the ProductImageCarousel component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  it('Expect product image carousel emits "delete-image" when make delete button is pressed', async () => {
    const button = wrapper.getComponent({ ref: 'deleteImageButton' });
    expect(button.exists()).toBeTruthy();

    button.trigger('click');

    expect(wrapper.emitted()['delete-image']).toEqual([[7]]);
  });

  it('Expect product image carousel does not have a makePrimary button when looking at primary image', async () => {
    const button = wrapper.findAllComponents({ref: 'makePrimaryImageButton'});
    expect(button.length).toBe(0);
  });

  it('Expect product image carousel has a make primary button when looking at second item and clicking it results in a change-primary-image event', async () => {
    await wrapper.setData({
      carouselItem: 1,
    });
    await Vue.nextTick();
    const button = wrapper.getComponent({ ref: 'makePrimaryImageButton' });
    expect(button.exists()).toBeTruthy();
    button.trigger('click');
    expect(wrapper.emitted()['change-primary-image']).toEqual([[11]]);
  });

  it('Expect delete image control not to exist if "showControls" is false', async () => {
    await appWrapper.setProps({
      showControls: false,
    });
    const button = wrapper.findAllComponents({ref: 'deleteImageButton'});
    expect(button.length).toBe(0);
  });

  it('Expect make primary image control not to exist if "showControls" is false', async () => {
    await wrapper.setData({
      carouselItem: 1,
    });
    await Vue.nextTick();
    await appWrapper.setProps({
      showControls: false,
    });
    const button = wrapper.findAllComponents({ref: 'makePrimaryImageButton'});
    expect(button.length).toBe(0);
  });
});