import { getProducts, Product, Image } from "@/api/internal";
import axios, { AxiosError, AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function() {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends {[k: string]: (...args: any[]) => any}> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>>}

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' >> = axios.instance;

describe('Test GET /businesses/:id/products endpoint', () => {

  const image : Image = {
    id: 1,
    filename: "",
    thumbnailFilename: ""
  };

  it('When response is a product array where product has no null attributes, getProducts returns the product array', async () => {
    const responseData = [
      {
        id: "",
        name: "",
        description: "",
        manufacturer: "",
        recommendedRetailPrice: 10,
        created: "",
        images: [image],
        countryOfSale: "",
      }
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const products = await getProducts(1, 1, 1, "created", false);
    expect(products).toEqual(responseData);
  });

  it('When response is a product array where product\'s optional attributes aren\'t present, getProducts returns the product array', async () => {
    const responseData = [
      {
        id: "",
        name: "",
        images: [],
      }
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const products = await getProducts(1, 1, 1, "created", false);
    expect(products).toEqual(responseData);
  });

  it('When response does not contain the field id, getProducts returns an error message indicating that the response is not a product array', async () => {
    const responseData = [
      {
        name: "",
        images: [],
      }
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const products = await getProducts(1, 1, 1, "created", false);
    expect(products).toEqual('Response is not product array');
  });

  it('When response has a 401 status, getProducts returns an error message indicating that the user is not logged in', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Missing/Invalid access token');
  });

  it('When response has a 403 status, getProducts returns an error message indicating that the user is not an admin of the business', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 403,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Not an admin of the business');
  });

  it('When response has a 406 status, getProducts returns an error message indicating that buesiness does not exists', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 406,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Business not found');
  });

  it('When a response without a status is received, getProducts returns an error message indicating that the server could not be reached', async () => {
    instance.get.mockRejectedValueOnce("Server is down");
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Failed to reach backend');
  });

  it('When response has an error status that is not 401, 403 or 406, getProducts will return an error message with that status', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 732,
      }});
    const message = await getProducts(1, 1, 1, "created", false);
    expect(message).toEqual('Request failed: 732');
  });
});
