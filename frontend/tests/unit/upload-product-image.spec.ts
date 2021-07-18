import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import ProductImageUploader from '@/components/utils/ProductImageUploader.vue';
import { castMock, flushQueue } from './utils';
import * as api from '@/api/internal';

jest.mock('@/api/internal', () => ({
  uploadProductImage: jest.fn(),
}));

const uploadProductImage = castMock(api.uploadProductImage);

Vue.use(Vuetify);

const localVue = createLocalVue();

const openFileDialogMock = jest.spyOn((ProductImageUploader as any).methods, 'openFileDialog');

describe('ProductImageUploader.vue', () => {
  // Container for the wrapper around ProductImageUploader
  let appWrapper: Wrapper<any>;

  // Container for the ProductImageUploader under test
  let wrapper: Wrapper<any>;


  /**
   * Executes before all the tests.
   *
   * The jsdom environment doesn't declare URL.createObjectURL function, hence we need to implement it
   * ourselves to make the uploader not crash.
   */
  beforeAll(() => {
    globalThis.URL = {
      createObjectURL(object: any) {
        return 'obj_url:' + object;
      }
    } as any;
  });

  /**
   * Sets up the test ProductImageUploader instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(() => {
    const vuetify = new Vuetify();

    // Creating wrapper around ProductImageUploader with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { ProductImageUploader },
      template: `
      <div data-app>
        <ProductImageUploader v-model="showImageUploaderForm" :businessId="100" productCode="PRODUCT-ID"/>
      </div>`,
    });

    // Put the ProductImageUploader component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
      data() {
        return {
          showImageUploaderForm: true
        };
      }
    });

    wrapper = appWrapper.getComponent(ProductImageUploader);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the ProductImageUploader component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Finds the close button in the ProductImageUploader form
   *
   * @returns A Wrapper around the close button
   */
  function findCloseButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Close'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the create button in the ProductImageUploader form
   *
   * @returns A Wrapper around the create button
   */
  function findCreateButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Upload'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }
  /**
   * Finds the select file button in the ProductImageUploader form.
   *
   * @returns A Wrapper around the select button
   */
  function findSelectButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Select'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the discard selected image button in the ProductImageUploader form if it exists
   *
   * @returns A wrapper around the discard image button if it exists, undefined otherwise
   */
  function findDiscardImageButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.findComponent({name: 'v-icon' }).exists());
    expect(filtered.length).toBeLessThanOrEqual(1);
    return filtered.length === 1 ? filtered.at(0) : undefined;
  }

  it('Clicking the select button should trigger the "openFileDialog" method', async () => {
    findSelectButton().trigger('click');
    expect(openFileDialogMock).toHaveBeenCalled();
  });

  it('Dropping a file should set the file attribute', async () => {
    wrapper.vm.isDragging = true;
    const testFile = new File([], 'test_file');
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        files: [
          testFile
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDrop(event);
    expect(wrapper.vm.file).toBe(testFile);
    expect(wrapper.vm.isDragging).toBeFalsy();
    expect(event.preventDefault).toBeCalled();
  });

  it('Dragging state should be enabled when the "onDragOver" event is triggered with a valid file', async () => {
    expect(wrapper.vm.isDragging).toBeFalsy();
    const matchFn = jest.fn().mockReturnValue(true);
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        items: [
          {
            kind: 'file',
            type: {
              match: matchFn,
            }
          }
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDragOver(event);
    expect(matchFn).toBeCalledWith('^image/');
    expect(wrapper.vm.isDragging).toBeTruthy();
    expect(event.preventDefault).toBeCalled();
  });

  it('Dragging state should not be enabled when the "onDragOver" event is triggered with a invalid file', async () => {
    wrapper.vm.isDragging = false;
    const matchFn = jest.fn().mockReturnValue(false); // Non-matching file
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        items: [
          {
            kind: 'file',
            type: {
              match: matchFn,
            }
          }
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDragOver(event);
    expect(matchFn).toBeCalledWith('^image/');
    expect(wrapper.vm.isDragging).toBeFalsy();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('Dragging state should not be enabled when the "onDragOver" event is triggered with a invalid drag type', async () => {
    wrapper.vm.isDragging = false;
    const matchFn = jest.fn().mockReturnValue(true); // Non-matching file
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        items: [
          {
            kind: 'not-file',
            type: {
              match: matchFn,
            }
          }
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDragOver(event);
    expect(wrapper.vm.isDragging).toBeFalsy();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('When no image is selected then the discard image button should not exist', () => {
    expect(findDiscardImageButton()).toBeUndefined();
  });

  it('When an image is selected the discard image button should exist and clicking it should discard the image', async () => {
    const testFile = new File([], 'test_file');
    wrapper.vm.file = testFile;
    await Vue.nextTick();
    const discardImage = findDiscardImageButton();
    expect(discardImage).not.toBeUndefined();
    discardImage!.trigger('click');
    await Vue.nextTick();
    expect(wrapper.vm.file).toBeUndefined();
    expect(findDiscardImageButton()).toBeUndefined();
  });

  it('When the close button is pressed then the "showImageUploaderForm" boolean should be false', async () => {
    await findCloseButton().trigger('click'); // Click close button
    expect(appWrapper.vm.showImageUploaderForm).toBeFalsy();
  });

  it('When there is no image selected then the create button should be disabled', async () => {
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeTruthy();
  });

  it('When there is an image selected then the create button should be enabled', async () => {
    const testFile = new File([], 'test_file');
    wrapper.vm.file = testFile;
    await Vue.nextTick();
    expect(findCreateButton().props().disabled).toBeFalsy();
  });

  it('When the create button is pressed then an api call should be made and is successful', async () => {
    const testFile = new File([], 'test_file');
    wrapper.vm.file = testFile;
    uploadProductImage.mockResolvedValue(undefined); // Ensure that the operation is successful
    await Vue.nextTick();
    await findCreateButton().trigger('click'); // Click create button
    await Vue.nextTick();
    expect(uploadProductImage).toBeCalledWith(
      100,
      'PRODUCT-ID',
      testFile
    );
    expect(appWrapper.vm.showImageUploaderForm).toBeFalsy();
  });

  it('When the create button is pressed and the api returns an error then the error should be shown', async () => {
    const testFile = new File([], 'test_file');
    wrapper.vm.file = testFile;
    uploadProductImage.mockResolvedValue('test_error_message'); // Ensure that the operation fails
    await Vue.nextTick();
    await findCreateButton().trigger('click'); // Click create button
    await flushQueue();
    // The appWrapper is tested for the text, because the dialog content is not in the dialog
    // element.
    expect(appWrapper.text()).toContain('test_error_message');
  });
});
