import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import Index from '@/components/Auth/index.vue';
import Login from '@/components/Auth/Login.vue';
import Register from '@/components/Auth/Register.vue';

Vue.use(Vuetify);

/**
 * Index is used here as the mount because the tests in here focuses on the switching between Register and Login,
 * and directing to other pages, of which, Index is the parent component of both.
 */
describe('index.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;
  const methodInvoked = jest.spyOn((Login as any).methods, 'login').mockImplementation(() => undefined);
  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Index, {
      localVue,
      vuetify,
      methodInvoked,
      components: {
        Login,
        Register
      },
      data() {
        return {
          //override value for login to true, meaning the current page is at Login page
          login: true
        };
      }
    } as any);
  });
  it("Testing out the log in page link, should redirect to Register Page from Login Page", async () => {
    //if login is true, the Login component should exist and the Register component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeTruthy();
    expect(wrapper.findComponent(Register).exists()).toBeFalsy();

    //find the link which toggles between pages
    const link = wrapper.findComponent(Login).find('.link');
    //click on the link to change the login value
    await link.trigger('click');

    //if login is false, the Register component should exist and the Login component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeFalsy();
    expect(wrapper.findComponent(Register).exists()).toBeTruthy();
  });

  it("Testing out the log in page button, should call function login which redirects to Home Page from " +
    "Login Page", async () => {
    //initially wrapper should not be able to find HomePage as its in the Login page
    //checking Login page existence
    expect(wrapper.findComponent(Login).exists()).toBeTruthy();

    const loginPage = wrapper.findComponent(Login);
    //enter proper, valid inputs for both email and password fields
    const emailInput = loginPage.find('input[type="email"]');
    await emailInput.setValue('someemail@gmail.com');
    const passwordInput = loginPage.find('input[type="password"]');
    await passwordInput.setValue('hello123');

    const loginButton = wrapper.findComponent(Login).find(".v-btn");

    await Vue.nextTick();

    expect(loginButton.props().disabled).toBeFalsy();

    await loginButton.trigger('click');

    await Vue.nextTick();
    expect(methodInvoked).toBeCalled();
  });
});

/**
 * Tests in here focuses on the input of the fields, of which, Login would be used as the mount as it is the parent of
 * these fields.
 */
describe('Login.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;

  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Login, {
      localVue,
      vuetify,
      data() {
        return {
          email: "someemail@gmail.com",
          password: "somepassword1"
        };
      }
    });
  });

  /**
   * Forces the form to be validated
   */
  function validateForm() {
    const form = wrapper.findComponent({ name: 'v-form'});
    (form.vm as any).validate();
  }

  it("Testing out the inputs for the email and password, such that the user can only press the login button " +
    "after inputting valid formats for both fields", async () => {
    //find the login button by the component
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeFalsy();
  });

  it("Testing for invalid email format, with less than two characters after each '.'", async () => {
    const loginButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "someemail@gmail.c"
    });
    //Docs from the vue api:
    //nextTick() Defers the callback to be executed after the next DOM update cycle.
    //Use it immediately after you’ve changed some data to wait for the DOM update.
    //In this case, we just changed some data on the email and password field, so we need to call nextTick for a DOM
    //update.
    await Vue.nextTick();
    validateForm();
    await Vue.nextTick();
    expect(loginButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid email format,with no '@'", async () => {
    const loginButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "someemail.com"
    });
    validateForm();
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, with no characters before '@'", async () => {
    const loginButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "@gmail.com"
    });
    validateForm();
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, with no '.'", async () => {
    const loginButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "fsefsgr@gmailcom"
    });
    validateForm();
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, empty email field", async () => {
    const loginButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: ""
    });
    validateForm();
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, over character limit", async () => {
    const loginButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: 'a'.repeat(101)
    });
    validateForm();
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it('Tests that errorMessage is displayed if not undefined', async () => {
    await wrapper.setData({
      errorMessage: 'test_error_message',
    });
    expect(wrapper.text()).toContain('test_error_message');
  });
});