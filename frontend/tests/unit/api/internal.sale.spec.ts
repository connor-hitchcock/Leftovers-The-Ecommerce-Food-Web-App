import * as api from '@/api/internal';
import axios, { AxiosError, AxiosInstance } from 'axios';
jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
    post: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get', 'post'>> = axios.instance;

describe("Test GET /businesses/:businessId/listings endpoint", () => {
  it('When response is a sale array with all fields, the response will be an sale array', async ()=>{
    const responseData = [
      {
        "id": 57,
        "inventoryItem": {
          "id": 101,
          "product": {
            "id": "WATT-420-BEANS",
            "name": "Watties Baked Beans - 420g can",
            "description": "Baked Beans as they should be.",
            "manufacturer": "Heinz Wattie's Limited",
            "recommendedRetailPrice": 2.2,
            "created": "2021-05-19T08:22:27.875Z",
            "images": [
              {
                "id": 1234,
                "filename": "/media/images/23987192387509-123908794328.png",
                "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
              }
            ]
          },
          "quantity": 4,
          "remainingQuantity": 1,
          "pricePerItem": 6.5,
          "totalPrice": 21.99,
          "manufactured": "2021-05-19",
          "sellBy": "2021-05-19",
          "bestBefore": "2021-05-19",
          "expires": "2021-05-19"
        },
        "quantity": 3,
        "price": 17.99,
        "moreInfo": "Seller may be willing to consider near offers",
        "created": "2021-07-14T11:44:00Z",
        "closes": "2021-07-21T23:59:00Z"
      }
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const sales = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(sales).toEqual(responseData);
  });

  it('When response is a sale array with required fields, the result will be a sale array', async () =>{
    const responseData = [
      {
        "id": 57,
        "inventoryItem": {
          "id": 101,
          "product": {
            "id": "WATT-420-BEANS",
            "name": "Watties Baked Beans - 420g can",
            "description": "Baked Beans as they should be.",
            "manufacturer": "Heinz Wattie's Limited",
            "recommendedRetailPrice": 2.2,
            "created": "2021-05-19T08:22:27.875Z",
            "images": [
              {
                "id": 1234,
                "filename": "/media/images/23987192387509-123908794328.png",
                "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
              }
            ]
          },
          "quantity": 4,
          "remainingQuantity": 1,
          "pricePerItem": 6.5,
          "totalPrice": 21.99,
          "manufactured": "2021-05-19",
          "sellBy": "2021-05-19",
          "bestBefore": "2021-05-19",
          "expires": "2021-05-19"
        },
        "quantity": 3,
        "price": 17.99,
        "created": "2021-07-14T11:44:00Z",
      }];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const sales = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(sales).toEqual(responseData);
  });

  it('When response is a inventory array with a missing required field, the result will be an error message stating invalid format', async () => {
    const responseData = [
      {
        "id": 57,
        "inventoryItem": {
          "id": 101,
          "product": {
            "id": "WATT-420-BEANS",
            "name": "Watties Baked Beans - 420g can",
            "description": "Baked Beans as they should be.",
            "manufacturer": "Heinz Wattie's Limited",
            "recommendedRetailPrice": 2.2,
            "created": "2021-05-19T08:22:27.875Z",
            "images": [
              {
                "id": 1234,
                "filename": "/media/images/23987192387509-123908794328.png",
                "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
              }
            ]
          },
          "quantity": 4,
          "pricePerItem": 6.5,
          "totalPrice": 21.99,
          "manufactured": "2021-05-19",
          "sellBy": "2021-05-19",
          "bestBefore": "2021-05-19",
          "expires": "2021-05-19"
        },
        "quantity": 3,
        "created": "2021-07-14T11:44:00Z",
      }];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const inventories = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(inventories).toEqual("Response is not Sale array");
  });

  it('When response is undefined status, the result will be an error message stating unable to reach backend', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: undefined,
      }
    });
    const inventories = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(inventories).toEqual("Failed to reach backend");
  });

  it('When response is 401 status, the result will be an error message stating the access token is invalid/missing', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }
    });
    const inventories = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(inventories).toEqual("Missing/Invalid access token");
  });

  it('When response is 406 status, the result will be an error message stating there is no such business', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 406,
      }
    });
    const inventories = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(inventories).toEqual("The given business does not exist");
  });

  it('When response is any other error status number, the result will be an error message stating the request failed', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 999,
      }
    });
    const inventories = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(inventories).toEqual("Request failed: 999");
  });

  it('When a response without a status is received, the result returns an error message indicating that the server could not be reached', async () => {
    instance.get.mockRejectedValueOnce("Server is down");
    const message = await api.getBusinessSales(7, 1, 1, 'created', false);
    expect(message).toEqual('Failed to reach backend');
  });

  it("When the user tries to create a sales item with all the fields, the result returns 'undefined'", async() => {
    const salesItem = {
      inventoryItemId: 1,
      quantity: 1,
      price: 1,
      moreInfo: 'Some Info',
      closes: 'Some Date',
    };
    instance.post.mockResolvedValueOnce({
      data: {
        "listingId": 1
      }
    });
    const salesItemResponse = await api.createSaleItem(1, salesItem);
    expect(salesItemResponse).toEqual({
      "listingId": 1
    });
  });

  it("When the user tries to create a sales item with only the required fields, the result returns 'undefined'", async() => {
    const salesItem = {
      inventoryItemId: 1,
      quantity: 1,
      price: 1
    };
    instance.post.mockResolvedValueOnce({
      data: {
        "listingId": 1
      }
    });
    const salesItemResponse = await api.createSaleItem(1, salesItem);
    expect(salesItemResponse).toEqual({
      "listingId": 1
    });
  });

  it("When the unsucessful response returns an incorrect request error, an 400 error message is returned", async() => {
    //the sales item below is technically still in the correct format, because we still need to feed the correct
    //format for the salesItem in order for the mocking of createSaleItem to work.
    const salesItem = {
      inventoryItemId: 1,
      quantity: 1,
      price: 1
    };
    instance.post.mockRejectedValueOnce({
      response: {
        status: 400
      }
    });
    const errorResponse = await api.createSaleItem(1, salesItem);
    expect(errorResponse).toEqual('Invalid data with the Sale Item');
  });

  it("When the unsucessful response returns a forbidden error, an 403 error message is returned", async() => {
    const salesItem = {
      inventoryItemId: 1,
      quantity: 1,
      price: 1
    };
    instance.post.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const errorResponse = await api.createSaleItem(1, salesItem);
    expect(errorResponse).toEqual('Operation not permitted');
  });

  it("When the unsucessful response returns something other than an error message, an error message is returned", async() => {
    const salesItem = {
      inventoryItemId: 1,
      quantity: 1,
      price: 1
    };
    instance.post.mockRejectedValueOnce({
      response: {}
    });
    const errorResponse = await api.createSaleItem(1, salesItem);
    expect(errorResponse).toEqual('Failed to reach backend');
  });

  it("When the unsucessful response returns an error other than 400 and 403, an error message is returned", async() => {
    const salesItem = {
      inventoryItemId: 1,
      quantity: 1,
      price: 1
    };
    instance.post.mockRejectedValueOnce({
      response: {
        status: 500
      }
    });
    const errorResponse = await api.createSaleItem(1, salesItem);
    expect(errorResponse).toEqual('Request failed: 500');
  });
});