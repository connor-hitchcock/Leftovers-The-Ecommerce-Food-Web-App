import * as api from '@/api/internal';
import axios, { AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    post: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'post'>> = axios.instance;

describe("Test POST /cards endpoint", () => {

  const marketplaceCard : api.CreateMarketplaceCard = {
    creatorId: 1,
    section: "Exchange",
    title: "Title",
    keywordIds: [1],
  };

  it('When API request is successfully resolved and contains cardId field which has a number, response is the integer value of cardId', async ()=>{
    instance.post.mockResolvedValueOnce({
      data: {
        cardId: 15
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual(15);
  });

  it('When API request is successfully resolved does not contain cardId field, message is returned saying response format was invalid', async ()=>{
    instance.post.mockResolvedValueOnce({
      data: {
        notCardId: 15
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual('Invalid response format');
  });

  it('When API request is successfully resolved and has cardId field which is not a number, message is returned saying response format was invalid', async ()=>{
    instance.post.mockResolvedValueOnce({
      data: {
        cardId: "Not a number"
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual('Invalid response format');
  });

  it('When API response has a 400 status code, an error message saying the request was incorrectly formatted will be returned', async () =>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 400,
        data: {
          message: "Title too long"
        }
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("Incorrect marketplace card format: Title too long");
  });

  it('When API response has a 401 status code, an error message saying the token was invalid will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("Missing/Invalid access token");
  });

  it('When API response has a 403 status code, an error message saying a card cannot be created for another user will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: 403,
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("A user cannot create a marketplace card for another user");
  });

  it('When response is undefined status, an error message containing the message from the response will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: 738,
        data: {
          message: "The server is having a bad day"
        }
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("Request failed: The server is having a bad day");
  });

  it('When response is undefined status, an error message stating unable to reach backend will be returned', async () => {
    instance.post.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const response = await api.createMarketplaceCard(marketplaceCard);
    expect(response).toEqual("Failed to reach backend");
  });

  it('When a response without a status is received, an error message stating unable to reach backend will be returned', async () => {
    instance.post.mockRejectedValueOnce(undefined);
    const message = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(message).toEqual('Failed to reach backend');
  });
});