Feature: Location

    Scenario: Create a valid location from an address
        Given the address "69,Riccarton Road,Ashburton,Christchurch,New Zealand,Canterbury,8041" does not exist
        When the address "69,Riccarton Road,Ashburton,Christchurch,New Zealand,Canterbury,8041" is created
        Then the address "69,Riccarton Road,Ashburton,Christchurch,New Zealand,Canterbury,8041" exists
        Then the address has the street number "69"
        Then the address has the street name "Riccarton Road"
        Then the address has the district "Ashburton"
        Then the address has the city name "Christchurch"
        Then the address has the region name "Canterbury"
        Then the address has the country name "New Zealand"
        Then the address has the post code "8041"

    Scenario: Create a second valid location from an address
        Given the address "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010" does not exist
        When the address "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010" is created
        Then the address "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010" exists
        Then the address has the street number "100"
        Then the address has the street name "Ocean View Crescent"
        Then the address has the district "Ashburton"
        Then the address has the city name "Auckland"
        Then the address has the region name "Rakino Island"
        Then the address has the country name "New Zealand"
        Then the address has the post code "1010"