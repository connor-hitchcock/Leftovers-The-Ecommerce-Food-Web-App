import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';

import Index from '@/components/Auth/index.vue';
import Login from '@/components/Auth/Login.vue';
import Register from '@/components/Auth/Register.vue';
import LocationAutocomplete from "@/components/utils/LocationAutocomplete.vue";

Vue.use(Vuetify);

/**
 * Index is used here as the mount because the tests in here focuses on the switching between Register and Login,
 * of which, Index is the parent component of both.
 */
describe('index.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;
  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Index, {
      localVue,
      vuetify,
      components: {
        Login,
        Register
      },
      data() {
        return {
          //override value for login to false, meaning the current page is at Register page
          login: false
        };
      }
    } as any);
  });

  it("Testing out the register page link, should redirect to Login Page from Register Page", async () => {
    //if login is false, the Register component should exist and the Login component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeFalsy();
    expect(wrapper.findComponent(Register).exists()).toBeTruthy();

    //find the link which toggles between pages
    const link = wrapper.findComponent(Register).find('.link');
    //click on the link to change the login value
    await link.trigger('click');

    //if login is true, the Login component should exist and the Register component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeTruthy();
    expect(wrapper.findComponent(Register).exists()).toBeFalsy();
  });
});

describe('Register.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;

  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Register, {
      localVue,
      vuetify,
      components: {
        LocationAutocomplete
      },
      data() {
        //automatically fill in all details before each test except the autocomplete ones as the autocomplete fields
        //need to be filled in manually
        return {
          showPassword: false,
          showConfirmPassword: false,
          valid: false,
          email: 'someemail@gmail.com',
          password: 'somepassword1',
          confirmPassword: 'somepassword1',
          firstName: 'Winnie',
          middleName: 'The',
          lastName: 'Pooh',
          nickname: 'some nickname',
          bio: 'some bio',
          dob: '2008-04-06',
          countryCode: '64',
          phone: '1234567890',
          streetAddress: '15 some street',
          district: "some district",
          city: "some city",
          region: "some state",
          country: "some country",
          postcode: '1234'
        };
      }
    } as any);
    //The jsdom environment doesn't declare the fetch function, hence we need to implement it
    //ourselves to make LocationAutocomplete not crash.
    globalThis.fetch = async () => {
      return {
        json() {
          return {
            features: [],
          };
        }
      } as any;
    };
  });

  it("Testing out all inputs, such that the user can only press the register button " +
    "after inputting valid formats for all fields", () => {
    //find the register button by the component
    const registerButton = wrapper.find(".v-btn");
    //since the fields are all inputted with valid formats and all mandatory fields are filled, the button should not be
    //disabled.
    expect(registerButton.props().disabled).toBeFalsy();
  });

  it("Testing for invalid email format, with less than two characters after each '.'", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "someemail@gmail.c"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid email format,with no '@'", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "someemail.com"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid email format, with no characters before '@'", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "@gmail.com"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid email format, with no '.'", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: "fsefsgr@gmailcom"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid email format, empty email field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid email format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      email: 'a'.repeat(101)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid password format, with no numbers", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      password: "hello"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid password format, with no alphabets", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      password: "123455678"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid password format, with less than 7 characters", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      password: "abcd1"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid password format, empty password field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      password: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid password format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      password: 'a'.repeat(101)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid confirm password format, different input value from password field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      confirmPassword: "somepassword2"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid confirm password format, empty confirm password field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      confirmPassword: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid first name format, empty first name field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      firstName: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid first name format, first name with numbers", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      firstName: "somename1"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid first name format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      firstName: 'a'.repeat(101)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing that empty middle name field is not invalid because middle name is not mandatory", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      middleName: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeFalsy();
  });

  it("Testing for invalid middle name format, middle name with numbers", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      middleName: "somename1"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid middle name format, middle name over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      middleName: 'a'.repeat(101)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid last name format, empty last name field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      lastName: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid last name format, last name with numbers", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      lastName: "somename1"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid last name format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      lastName: 'a'.repeat(101)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid nickname format, name with numbers", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      nickname: "somename1"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid nickname format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      nickname: 'a'.repeat(101)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid bio format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      bio: 'a'.repeat(201)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid date format, empty date field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      dob: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid country code format, no country code", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      countryCode: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid country code format, alphabets in country code", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      countryCode: "a1"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid country code format, more than three numbers in country code", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      countryCode: "1234"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid country code format, less than two numbers in country code", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      countryCode: "1"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid phone format, alphabets in field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      phone: "123456789a"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid phone format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      phone: '1'.repeat(101)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid phone format, spaces at the start of the phone number", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      phone: " 1234567890"
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid phone format, spaces at the end of the phone number", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      phone: "1234567890 "
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid street format, empty street field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      streetAddress: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid district format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      district: 'a'.repeat(256)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid city format, empty city field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      city: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid city format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      city: 'a'.repeat(256)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid region format, empty region field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      region: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid region format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      region: 'a'.repeat(256)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid country format, empty country field", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      country: ""
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing for invalid country format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");
    await wrapper.setData({
      country: 'a'.repeat(256)
    });
    await Vue.nextTick();
    expect(registerButton.props().disabled).toBeTruthy();
  });

  it("Testing the password field's mdi-eye icon to show user input", () => {
    //originally it should be false
    expect(wrapper.vm.showPassword).toBeFalsy();
    let showPasswordInput = wrapper.findComponent({ ref: "password" });
    let eyeButton = showPasswordInput.findComponent({ name: "v-icon" });
    //clicking on the icon would allow the user to see the password, thus making showPassword true
    eyeButton.trigger("click");
    Vue.nextTick(() => {
      expect(wrapper.vm.showPassword).toBeTruthy();
    });
  });

  it("Testing the confirm password field's mdi-eye icon to show user input", () => {
    //originally it should be false
    expect(wrapper.vm.showConfirmPassword).toBeFalsy();
    let showConfirmPasswordInput = wrapper.findComponent({ ref: "confirmPassword" });
    let eyeButton = showConfirmPasswordInput.findComponent({ name: "v-icon" });
    //clicking on the icon would allow the user to see the password, thus making showConfirmPassword true
    eyeButton.trigger("click");
    Vue.nextTick(() => {
      expect(wrapper.vm.showConfirmPassword).toBeTruthy();
    });
  });
});