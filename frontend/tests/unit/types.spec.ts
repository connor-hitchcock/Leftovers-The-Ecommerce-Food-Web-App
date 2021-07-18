import { is } from 'typescript-is';

import { Business, User } from '@/api/internal';

it('Testing that 7 is a number', () => {
  expect(is<number>(7)).toBeTruthy();
});

it('Testing valid user is a user', () => {
  expect(is<User>({
    id: 7,
    firstName: 'dave',
    lastName: 'rogers',
    email: 'example@gmail.com',
    dateOfBirth: '1/1/1900',
    homeAddress: {
      country: 'Africa'
    },
    nickname: 'davy',
    role: 'user',
  })).toBeTruthy();
});

it('Testing valid user with businesses', () => {
  expect(is<User>({
    firstName: "Andy",
    lastName: "Elliot",
    role: "user",
    created: "2021-05-13 23:17:08.591",
    nickname: "Ando",
    businessesAdministered: [
      {
        primaryAdministratorId: 2,
        address: {
          country: "New Zealand",
          streetName: "Albert Road",
          streetNumber: "108",
          city: "Christchurch",
          district: "Ashburton",
          postcode: "8041",
          region: "Canterbury"
        },
        created: "2021-05-13 23:17:08.618",
        name: "BUSINESS_NAME",
        description: "DESCRIPTION",
        id: 10,
        businessType: "Accommodation and Food Services"
      }
    ],
    middleName: "Percy",
    dateOfBirth: "1987-04-12 00:00:00.0",
    id: 2,
    email: "123andyelliot@gmail.com",
    homeAddress: {
      country: "New Zealand",
      streetName: "Albert Road",
      streetNumber: "108",
      city: "Christchurch",
      district: "Ashburton",
      postcode: "8041",
      region: "Canterbury"
    }
  })).toBeTruthy();
});

const test: Business = {
  primaryAdministratorId:2,
  address: {
    country:"New Zealand",
    streetName:"Albert Road",
    streetNumber:"108",
    city:"Christchurch",
    district:"Ashburton",
    postcode:"8041",
    region:"Canterbury"
  },
  created:"2021-05-13 23:17:08.618",
  name:"BUSINESS_NAME",
  description:"DESCRIPTION",
  id:10,
  businessType:"Accommodation and Food Services",
  administrators:[
    {
      firstName:"Andy",
      lastName:"Elliot",
      created:"2021-05-13 23:17:08.591",
      nickname:"Ando",
      middleName:"Percy",
      id:2,
      email:"123andyelliot@gmail.com",
      homeAddress:{ country:"New Zealand", region:"Canterbury",city:"Christchurch" }
    }
  ]
};

it('Testing valid business', () => {
  expect(is<Business>({
    primaryAdministratorId:2,
    address: {
      country:"New Zealand",
      streetName:"Albert Road",
      streetNumber:"108",
      city:"Christchurch",
      district:"Ashburton",
      postcode:"8041",
      region:"Canterbury"
    },
    created:"2021-05-13 23:17:08.618",
    name:"BUSINESS_NAME",
    description:"DESCRIPTION",
    id:10,
    businessType:"Accommodation and Food Services",
    administrators:[
      {
        firstName:"Andy",
        lastName:"Elliot",
        created:"2021-05-13 23:17:08.591",
        nickname:"Ando",
        middleName:"Percy",
        id:2,
        email:"123andyelliot@gmail.com",
        homeAddress:{ country:"New Zealand", region:"Canterbury",city:"Christchurch" }
      }
    ]
  })).toBeTruthy();
});

it('Testing user with invalid role is not a user', () => {
  expect(is<User>({
    id: 7,
    firstName: 'dave',
    lastName: 'rogers',
    email: 'example@gmail.com',
    dateOfBirth: '1/1/1900',
    homeAddress: {
      country: 'Africa'
    },
    nickname: 'davy',
    role: 'pirate',
  })).toBeFalsy();
});

it('Testing user without id is not a user', () => {
  expect(is<User>({
    firstName: 'dave',
    lastName: 'rogers',
    email: 'example@gmail.com',
    dateOfBirth: '1/1/1900',
    homeAddress: {
      country: 'Africa'
    },
    nickname: 'davy',
  })).toBeFalsy();
});
