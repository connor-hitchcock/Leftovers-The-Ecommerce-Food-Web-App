import * as api from '@/api/internal';
import axios, { AxiosError, AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get'>> = axios.instance;

describe("Test GET /keywords/search endpoint", () => {
  it("Get keywords", async () => {
    const defaultWords = [{
      id: 1,
      name: "Gluten Free",
      created: "2002-02-02"
    }];
    instance.get.mockResolvedValueOnce({
      data: defaultWords
    });
    const keywords = await api.getKeywords();
    expect(keywords).toEqual(defaultWords);
  });

  it("Backend couldn't be reached", async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const keywords = await api.getKeywords();
    expect(keywords).toEqual("Failed to reach backend");
  });

  it("Unauthorised call to keywords", async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const keywords = await api.getKeywords();
    expect(keywords).toEqual('Missing/Invalid access token');
  });
});