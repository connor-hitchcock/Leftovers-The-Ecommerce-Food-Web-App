export const insertResultsFromAPI = async function (url: RequestInfo, items: string[]) {
  await fetch(url).then(res => res.json()).then(res => {
    //limiting results to five
    res.features.slice(0,5).forEach((feature: { properties: { name: string; }; }) => {
      //each result returned by the api will be pushed into the item list for autocomplete
      items.push(feature.properties.name);
    });
  }).catch(err => {
    console.log(err);
  });
};