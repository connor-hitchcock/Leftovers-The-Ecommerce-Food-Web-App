import { MaybeError } from "@/api/internal";
import { is } from "typescript-is";

/**
 * Currency information for a specific country.
 */
export type Currency = {
  code: string,
  name: string,
  symbol: string,
};

/**
 * Returned value should either be of type Currency or a string which contains an error message
 */
type CurrencyOrError = Currency | { errorMessage: string }

/**
 * An object which only has the attribute 'currencies', which is a list of Currency objects. The API response is expected
 * to contain an array of objects of this type
 */
type CurrenciesContainer = {
  currencies: Currency[];
};

/**
 * Make a request to the RESTCounties API to find the currency associated with the given country name.
 * If the request is successful the currency from the API will be returned. If it is unsuccessful then
 * a default currency object with blank code, name and symbol fields will be returned.
 * @param country The name of a country to use in the API request for the currency.
 * @returns A currency object, or an error message
 */
export async function currencyFromCountry(country: string): Promise<CurrencyOrError> {

  const response = await queryCurrencyAPI(country);

  if (typeof response === 'string') {
    console.warn(response);
    return { errorMessage: response };
  }

  const currency = await getCurrencyFromAPIResponse(response);

  if (typeof currency === 'string') {
    console.warn(currency);
    return { errorMessage: currency };
  }

  return currency;
}

/**
 * This method takes a string with the name of a country and queries the RESTCountries API to find the
 * currency associated with that country. The API's response will be returned if a response with status
 * code 200 is received, otherwise an error message will be returned.
 * @param country The name of the country to query the API for.
 * @return the response received from the RESTCounties API or a string error message.
 */
export async function queryCurrencyAPI(country: string): Promise<MaybeError<Response>> {

  const queryUrl = `https://restcountries.eu/rest/v2/name/${country}?fullText=true&fields=currencies`;

  const response = await fetch(queryUrl)
    .catch(error => {
      return `Failed to reach ${queryUrl}`;
    });
  if (typeof response === 'string') {
    return response;
  }
  if (response.status === 404) {
    return `No currency for country with name ${country} was found`;
  }
  if (response.status !== 200) {
    return `Request failed: ${response.status}`;
  }
  return response;
}

/**
 * This method checks the format of the API response and extracts a currency object from the JSON body
 * if the response format is correct. If it is not correct then an error message is returned.
 * @param response A currency object extracted from the response body or an error message.
 */
export async function getCurrencyFromAPIResponse(response: Response): Promise<MaybeError<Currency>> {
  const responseBody = await response.json();

  if (!is<[CurrenciesContainer]>(responseBody)) {
    return 'API response was not in readable format';
  }

  return responseBody[0].currencies[0];
}