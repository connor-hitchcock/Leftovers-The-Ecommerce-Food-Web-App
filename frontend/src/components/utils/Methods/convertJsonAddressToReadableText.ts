import { Location } from "@/api/internal";

//method to return the appropriate format for addresses depending on the fields provided
function convertAddressToReadableText(address: Location, status: "full" | "partial") {
  //full means the address format will show all fields, partial means the format will show some location fields only
  if (status === "full" && address.district !== null)
    return `${address.streetNumber} ${address.streetName}, ${address.district}\n` +
      `${address.city} ${address.postcode}\n ${address.region}, ${address.country}`;
  if (status === "full" && address.district === null)
    return `${address.streetNumber} ${address.streetName}\n` +
      `${address.city} ${address.postcode}\n${address.region}, ${address.country}`;
  if (status === "partial") return `${address.city}, ${address.region}, ${address.country}`;
  else throw new Error("Invalid status for address format used.");
}
export default convertAddressToReadableText;