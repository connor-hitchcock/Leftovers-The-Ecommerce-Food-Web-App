import { currencyFromCountry, queryCurrencyAPI, getCurrencyFromAPIResponse } from "@/api/currency";

describe('currency.ts', () => {

  let consoleOutput: string[];

  beforeEach(() => {
    consoleOutput = [];
    console.warn = (output: string) => consoleOutput.push(output);
  });

  it('Returns with an error message to the frontend when API can\'t find the country', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 404,
      json: () => Promise.resolve([{
        errorMessage: "No country with name Some wrong country was found"
      }])
    } as any
    );
    const response = await currencyFromCountry("Some wrong country");
    expect('errorMessage' in response).toBeTruthy();
    expect(response).toStrictEqual({"errorMessage": "No currency for country with name Some wrong country was found"});
  });

  it('Returns with a format error message to the frontend when API responds with a country', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{ currencies: [{ potato: 'potato' }] }]) as any
    } as any
    );
    const response = await currencyFromCountry("Australia");
    expect('errorMessage' in response).toBeTruthy();
    expect(response).toStrictEqual({"errorMessage": "API response was not in readable format"});
  });

  it('Returns with no error message to the frontend when API responds with a country with correct format', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{
        currencies: [{
          code: 'AUD',
          name: 'Australian Dollar',
          symbol: '$'
        }]
      }]) as any
    });
    const response = await currencyFromCountry("Some correct country");
    expect(response).toStrictEqual({
      code: 'AUD',
      name: 'Australian Dollar',
      symbol: '$'
    });
  });

  it('Returns no currency to the frontend and outputs to the console when API can\'t be reached', async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.reject("API is down")
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "Failed to reach https://restcountries.eu/rest/v2/name/Australia?fullText=true&fields=currencies"
    ]);
  });

  it('Returns no currency to the frontend and outputs to the console when 404 response received', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 404
    } as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "No currency for country with name Australia was found"
    ]);
  });

  it('Returns no currency to the frontend and outputs to the console when 400 response received', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 400
    } as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "Request failed: 400"
    ]);
  });

  it('Returns no currency to the frontend and outputs to the console when 200 response received but response is not in expected format', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{ currencies: [{ potato: 'potato' }] }]) as any
    } as any
    );
    const currency = await currencyFromCountry("Australia");
    expect(consoleOutput).toEqual([
      "API response was not in readable format"
    ]);
  });

  it('Return currency received from API when response list contains one currency', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{
        currencies: [{
          code: 'AUD',
          name: 'Australian Dollar',
          symbol: '$'
        }]
      }]) as any
    });
    const currency = await currencyFromCountry("Australia");
    expect(currency).toEqual({
      code: 'AUD',
      name: 'Australian Dollar',
      symbol: '$'
    });
    expect(consoleOutput).toEqual([]);
  });

  it('Return first currency received from API when response list contains multiple currencies', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue({
      status: 200,
      json: () => Promise.resolve([{
        "currencies": [
          {
            "code": "BTN",
            "name": "Bhutanese ngultrum",
            "symbol": "Nu."
          },
          {
            "code": "INR",
            "name": "Indian rupee",
            "symbol": "₹"
          }
        ]
      }]) as any
    });
    const currency = await currencyFromCountry("Bhutan");
    expect(currency).toEqual({
      "code": "BTN",
      "name": "Bhutanese ngultrum",
      "symbol": "Nu."
    });
    expect(consoleOutput).toEqual([]);
  });
});