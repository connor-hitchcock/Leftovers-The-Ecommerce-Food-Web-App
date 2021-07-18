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

describe("Test GET /businesses/:businessId/inventory endpoint", () => {
  it('When response is a inventory array with all fields, the result will be an inventory array', async () => {
    const responseData = [
      {
        id: 1,
        product: {
          id: "test_product",
          name: "test_product_name",
          description: "test_description",
          manufacturer: "test_manufacturer",
          recommendedRetailPrice: 1,
          created: "test_date",
          images: [],
          countryOfSale: "test_country",
        },
        quantity: 1,
        remainingQuantity: 1,
        pricePerItem: 1,
        totalPrice: 1,
        manufactured: "test_manufacturer",
        sellBy: "test_date",
        bestBefore: "test_date",
        expires: "test_date"
      }
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual(responseData);
  });

  it('When response is a inventory array with the required fields, the result will be an inventory array', async () => {
    const responseData = [
      {
        id: 1,
        product: {
          id: 'ID-VALUE',
          name: 'test_name',
          images: []
        },
        quantity: 9,
        remainingQuantity: 5,
        expires: "test_date"
      }
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual(responseData);
  });

  it('When response is a inventory array with a missing required field, the result will be an error message stating invalid format', async () => {
    const responseData = [
      {
        id: 1,
        product: {
          id: 'ID-VALUE',
          name: 'test_name',
          images: []
        },
        quantity: 9
      }
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual("Response is not inventory array");
  });

  it('When response is undefined status, the result will be an error message stating unable to reach backend', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual("Failed to reach backend");
  });

  it('When response is 401 status, the result will be an error message stating the access token is invalid/missing', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual("Missing/Invalid access token");
  });

  it('When response is 403 status, the result will be an error message stating the user is not an admin of the business', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 403,
      }
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual("Not an admin of the business");
  });

  it('When response is 406 status, the result will be an error message stating there is no such business', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 406,
      }
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual("Business not found");
  });

  it('When response is any other error status number, the result will be an error message stating the request failed', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 999,
      }
    });
    const inventories = await api.getInventory(7, 1, 10, "name", false);
    expect(inventories).toEqual("Request failed: 999");
  });

  it('When a response without a status is received, the result returns an error message indicating that the server could not be reached', async () => {
    instance.get.mockRejectedValueOnce("Server is down");
    const message = await api.getInventory(7, 1, 10, "name", false);
    expect(message).toEqual('Failed to reach backend');
  });
});

describe("Test GET /businesses/:businessId/inventory/count endpoint", () => {
  it('When response is a number containing the total number of results, the result will be a number', async () => {
    const responseData = {
      count: 9
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const inventories = await api.getInventoryCount(7);
    expect(inventories).toEqual(responseData.count);
  });

  it('When response is not a number, the result will be an error message stating that the response is not a number', async () => {
    const responseData = {
      count: "some_number"
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const inventories = await api.getInventoryCount(7);
    expect(inventories).toEqual("Response is not number");
  });

  it('When response is not a number, the result will be an error message stating that the response is not a number', async () => {
    const responseData = {
      count: "some_number"
    };
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const inventories = await api.getInventoryCount(7);
    expect(inventories).toEqual("Response is not number");
  });

  it('When response is undefined status, the result will be an error message stating unable to reach backend', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const inventories = await api.getInventoryCount(7);
    expect(inventories).toEqual("Failed to reach backend");
  });

  it('When response is any other error status number, the result will be an error message stating the request failed', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 999,
      }
    });
    const inventories = await api.getInventoryCount(7);
    expect(inventories).toEqual("Request failed: 999");
  });
});