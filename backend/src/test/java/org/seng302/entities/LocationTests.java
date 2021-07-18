package org.seng302.entities;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main test class. Testing overall application sanity
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LocationTests {

  private Location testLocation = new Location();
  private Location.Builder locationBuilder;

  @BeforeEach
  public void setUp() {
    locationBuilder = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Christchurch")
            .inRegion("Canterbury").inCountry("New Zealand").withPostCode("8041").atDistrict("Ashburton");
  }

  /**
   * Checks all integers between 1 and 999 return true when passed into the checkValidStreetNumber method
   */
  @Test
  void checkValidStreetNumberValidNumbers() {
    for (int i = 1; i <= 999; i++) {
      assertTrue(testLocation.checkValidStreetNumber(Integer.toString(i)));
    }
  }

  /**
   * Checks several street numbers with a single slash return true when passed into the checkValidStreetNumber method
   */
  @Test
  void checkValidStreetNumberWithSlash() {
    String[] streetNumbers = new String[]{ "1/1", "2/2", "6/9", "11/11", "1/111", "111/1", "111/111", "9999/9999" };
    for (String streetNumber : streetNumbers) {
      assertTrue(testLocation.checkValidStreetNumber(streetNumber));
    }
  }

  /**
   * Checks several street numbers with two forward slashes return false when passed into the checkValidStreetNumber method
   */
  @Test
  void checkInvalidStreetNumberWithDoubleSlash() {
    String[] streetNumbers = new String[]{ "1//1", "/1/", "//", "111/1/111", "1111//111" };
    for (String streetNumber : streetNumbers) {
      assertFalse(testLocation.checkValidStreetNumber(streetNumber));
    }
  }

  /**
   * Checks several large integers above 999 return false when passed into the checkValidStreetNumber method
   */
  @Test
  void checkInvalidStreetNumberTooLargeNumbers() {
    int[] streetNumbers = new int[]{ 1000000000, 1000000001, 1000000002, 1000000003, 2000000000, 1234500000, 1234560000,
            1234567000, 1234567890 };
    for (int streetNumber : streetNumbers) {
      assertFalse(testLocation.checkValidStreetNumber(Integer.toString(streetNumber)));
    }
  }

  /**
   * Checks negative integers fail when passed into the checkValidStreetNumber method
   */
  @Test
  void checkInvalidStreetNumberNegativeNumbers() {
    int[] streetNumbers = new int[]{ -1, -2, -3, -1001, -1002, -1003, -2000, -3000, -4000, -5000, -12345, -123456,
            -1234567, -12345678, -123456789, -1234567890 };
    for (int streetNumber : streetNumbers) {
      assertFalse(testLocation.checkValidStreetNumber(Integer.toString(streetNumber)));
    }
  }

  /**
   * Checks several names pass return true when passed into the checkValidStreetName method
   */
  @Test
  void checkValidStreetNameLetters() {
    String[] streetNames = new String[]{ "Abby Park Street", "Barn Street", "California Street", "Danish Avenue",
                                          "Eastern Cesta", "Farmers Lane", "Galghard Road", "Hazlett Avenue" };
    for (String streetName : streetNames) {
      assertTrue(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidStreetName method
   */
  @Test
  void checkValidStreetNameNumbers() {
    String[] streetNames = new String[]{ "Over 9000 Avenue", "69th Street", "0 Lane", "333 Road" };
    for (String streetName : streetNames) {
      assertFalse(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidStreetName method
   */
  @Test
  void checkValidStreetNameCharacters() {
    String[] streetNames = new String[]{ "Hashtag # Lane", "Dollar $ Avenue", "Me & The Bois Road", "@ Me Street" };
    for (String streetName : streetNames) {
      assertFalse(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Checks several names with over 100 characters fail when passed into the checkValidStreetName method
   */
  @Test
  void checkValidStreetNameOverHundredNine() {
    String[] streetNames = new String[]{
            " abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz",
            "helphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelp",
            "idontneeditidontneeditidontneeditidontneeditidontneeditidontneeditidontneeditidontneeditidontneeditidontneedit" };
    for (String streetName : streetNames) {
      assertFalse(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidStreetName method
   */
  @Test
  void checkValidStreetNameEmpty() {
    assertFalse(testLocation.checkValidStreetName(""));
  }

  /**
   * Checks several names pass return true when passed into the checkValidCity method
   */
  @Test
  void checkValidCityLetters() {
    String[] cities = new String[]{ "Christchurch", "Dunedin", "Nelson", "Wellington", "Hamilton", "Auckland",
                                    "Nightcaps", "Sendai", "Tokyo", "Osaka", "Akihabara", "New York", "London" };
    for (String city : cities) {
      assertTrue(testLocation.checkValidCity(city));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidCity method
   */
  @Test
  void checkValidCityNumbers() {
    String[] cities = new String[]{ "Chr1stchurch", "Dun3d1n", "N3ls0n", "W3ll1ngt0n", "Ham1lt0n", "Auck1and",
                                    "N1ghtcaps", "S3nda1", "T0ky0", "0saka", "Ak1habara", "N3w Y0rk", "L0nd0n" };
    for (String city : cities) {
      assertFalse(testLocation.checkValidCity(city));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidCity method
   */
  @Test
  void checkValidCityCharacters() {
    String[] cities = new String[]{ "Chr!stchurch", "Duned!n", "Ne!son", "Well!ngton", "H@m!lton", "@uck!@nd",
                                    "N!ghtc@p$", "$end@!", "To&yo", "Os@&@", "@k!h@b@r@", "New Yor&", "Lon)on" };
    for (String city : cities) {
      assertFalse(testLocation.checkValidCity(city));
    }
  }

  /**
   * Checks several names with over 50 characters fail when passed into the checkValidCity method
   */
  @Test
  void checkValidCityOverHundred() {
    String[] cities = new String[]{ "this city string contains exactly fifty characters this city string contains exactly fifty characters",
                                    "helpppppppppppppppppppppppppppppppppppppppppppppppppppppp helpppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                    "This is a city name that resembles the name of city This is a city name that resembles the name of city" };
    for (String city : cities) {
      assertFalse(testLocation.checkValidCity(city));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidCity method
   */
  @Test
  void checkValidCityEmpty() {
    assertFalse(testLocation.checkValidCity(""));
  }

  /**
   * Checks several names pass return true when passed into the checkValidRegion method
   */
  @Test
  void checkValidRegionLetters() {
    String[] regions = new String[]{ "Southland", "Otago", "West Coast", "Cantebury", "Clutha", "Marlborough", "Selwyn",
                                      "Tasman", "Waimakariri", "Waimate"};
    for (String region : regions) {
      assertTrue(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidRegion method
   */
  @Test
  void checkValidRegionNumbers() {
    String[] regions = new String[]{ "S0uthland", "0tago", "W3st C0ast", "Cant3bury", "C!utha", "Marlb0r0ugh", "S3lwyn",
                                      "5asman", "Wa1makar1r1", "Wa1mat3"};
    for (String region : regions) {
      assertFalse(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidRegion method
   */
  @Test
  void checkValidRegionCharacters() {
    String[] regions = new String[]{ "Southl@nd", "Ot@go", "We$t Co@st", "C@ntebury", "Cluth@", "M@rlborough", "Se!wyn",
                                      "T@sm@n", "W@!m@kar!r!", "W@!m@te" };
    for (String region : regions) {
      assertFalse(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Checks several names with over 100 characters fail when passed into the checkValidRegion method
   */
  @Test
  void checkValidRegionOverHundred() {
    String[] regions = new String[]{ "this region abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz",
                                      "helpppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                                      "this region is full of surprises you should come have a lookkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk" };
    for (String region : regions) {
      assertFalse(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidRegion method
   */
  @Test
  void checkValidRegionEmpty() {
    assertFalse(testLocation.checkValidRegion(""));
  }

  /**
   * Checks several names pass return true when passed into the checkValidCountry method
   */
  @Test
  void checkValidCountryLetters() {
    String[] countries = new String[]{ "New Zealand", "Australia", "Fiji", "Tonga", "Japan", "Korea", "United Kingdom",
                                        "Great Britan", "England", "Scotland" };
    for (String country : countries) {
      assertTrue(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidCountry method
   */
  @Test
  void checkValidCountryNumbers() {
    String[] countries = new String[]{ "N3w Z3aland", "Austral1a", "F1j1", "T0nga", "Ja9an", "K0r3a", "Un1t3d K1ngd0m",
                                        "Gr3at Br1tan", "3ngland", "Sc0tland" };
    for (String country : countries) {
      assertFalse(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidCountry method
   */
  @Test
  void checkValidCountryCharacters() {
    String[] countries = new String[]{ "New Ze@l@nd", "@ustr@l!@", "F!j!", "Tong@", "J@p@n", "K0r3@", "Un!ted K!ngdom",
                                        "Gre@t Br!t@n", "Engl@nd", "Scotl@nd" };
    for (String country : countries) {
      assertFalse(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Checks several names with over 50 characters fail when passed into the checkValidCountry method
   */
  @Test
  void checkValidCountryOverHundred() {
    String[] countries = new String[]{ "This country is exactlyyyyyy fifty characters long This country is exactlyyyyyy fifty characters long",
                                        "helppppppppppppppppppppppppppppppppppppppppppppppp helppppppppppppppppppppppppppppppppppppppppppppppp",
                                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                        "This country has a lot going for it we are coronavirus free This country has a lot going for it we are coronavirus free" };
    for (String country : countries) {
      assertFalse(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidCountry method
   */
  @Test
  void checkValidCountryEmpty() {
    assertFalse(testLocation.checkValidCountry(""));
  }

  /**
   * Checks several numeric zipcodes return true when passed into the checkValidZipCode method
   */
  @Test
  void checkValidZipCodeNumeric() {
    String[] zipcodes = new String[]{ "1", "2", "3", "4", "5", "10", "20", "30", "40", "100", "1000", "12345", "123456",
                                      "1234567", "12345678", "123456789" };
    for (String zipcode : zipcodes) {
      assertTrue(testLocation.checkValidPostCode(zipcode));
    }
  }

  /**
   * Checks several numeric zipcodes return true when passed into the checkValidZipCode method
   */
  @Test
  void checkValidZipCodeAlphaNumeric() {
    String[] zipcodes = new String[]{ "a1", "b2", "c3", "d4", "e5", "f10", "g20", "h30", "i40", "j100", "k1000",
                                      "l12345", "m123456", "o1234567", "p12345678", "q12345678" };
    for (String zipcode : zipcodes) {
      assertTrue(testLocation.checkValidPostCode(zipcode));
    }
  }

  /**
   * Checks several zipcodes with characters in them that fail when passed into the checkValidZipCode method
   */
  @Test
  void checkValidZipCodeCharacters() {
    String[] zipcodes = new String[]{ "!1", "@2", "#3", "$4", "%5", "^10", "&20", "*30", "(40", ")100", "=1000",
                                      "_12345", "[123456", "]1234567", "{12345678", "}123456789" };
    for (String zipcode : zipcodes) {
      assertFalse(testLocation.checkValidPostCode(zipcode));
    }
  }

  /**
   * Checks several zipcodes over ten characters fail when passed into the checkValidZipCode method
   */
  @Test
  void checkValidZipCodeOverSixteen() {
    String[] zipcodes = new String[]{ "12345678901234567", "99123456789123456789", "456123456789000000" };
    for (String zipcode : zipcodes) {
      assertFalse(testLocation.checkValidPostCode(zipcode));
    }
  }

  /**
   * Checks an empty string fail when passed into the checkValidZipCode method
   */
  @Test
  void checkValidZipCodeEmpty() {
    assertFalse(testLocation.checkValidPostCode(""));
  }

  
  /**
   * Checks several names with over 50 characters fail when passed into the checkValidDistrict method
   */
  @Test
  void checkValidDistrictOverHundredChar() {
    String[] districts = new String[]{"this district string contains above one hundred characters from start to end inclusive of spacessssss",
                                    "this district string contains way above one hundred characters from start to end inclusive of spacessssss" +
                                    "this district string contains way above one hundred characters from start to end inclusive of spacessssss" };
    for (String district : districts) {
      assertFalse(testLocation.checkValidDistrict(district));
    }
  }

  /**
   * Checks if Location object is still valid even if district field is an empty string
   */
  @Test
  void checkLocationValidIfNoDistrict() {
    Location location = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Christchurch")
                                      .inRegion("Canterbury").inCountry("New Zealand").withPostCode("8041").atDistrict("")
                                      .build();
    assertTrue(testLocation.checkValidAllLocationParameters(location));
  }

  /**
   * Checks a Location object with all valid parameters returns true when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersEverythingValid() {
    Location location = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Christchurch")
                                      .inRegion("Canterbury").inCountry("New Zealand").withPostCode("8041").atDistrict("Ashburton")
                                      .build();
    assertTrue(testLocation.checkValidAllLocationParameters(location));
  }

  /**
   * Checks a Location object with an invalid street number parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersStreetNumberInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber("1234567890").onStreet("Elizabeth Street").inCity("Christchurch")
                      .inRegion("Canterbury").inCountry("New Zealand").withPostCode("8041").atDistrict("Ashburton").build();
    });
  }

  /**
   * Checks a Location object with an invalid street name parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersStreetNameInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber("1").onStreet("Eliz@beth Str33t").inCity("Christchurch")
            .inRegion("Canterbury").inCountry("New Zealand").withPostCode("8041").atDistrict("Ashburton").atDistrict("Ashburton")
            .build();
    });
  }

  /**
   * Checks a Location object with an invalid city parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersCityInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Chr!$stchurch")
              .inRegion("Canterbury").inCountry("New Zealand").withPostCode("8041").atDistrict("Ashburton").build();
    });
  }

  /**
   * Checks a Location object with an invalid region parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersRegionInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Christchurch")
              .inRegion("C@nt3rbury").inCountry("New Zealand").withPostCode("8041").atDistrict("Ashburton").build();
    });
  }

  /**
   * Checks a Location object with an invalid country parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersCountryInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Christchurch")
              .inRegion("Canterbury").inCountry("N3w Z3@l@nd").withPostCode("8041").atDistrict("Ashburton").build();
    });
  }

  /**
   * Checks a Location object with an invalid zip code parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersZipCodeInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Christchurch")
                      .inRegion("Canterbury").inCountry("New Zealand").withPostCode("80999999999999941").atDistrict("Ashburton")
                      .build();
    });
  }

  /**
   * Checks a Location object with an invalid district parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  void checkValidAllLocationParametersDistrictInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber("1").onStreet("Elizabeth Street").inCity("Christchurch")
                      .inRegion("Canterbury").inCountry("New Zealand").withPostCode("8041")
                      .atDistrict("this district string contains above one hundred characters from start to end inclusive of spacessssss")
                      .build();
    });
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before street number has
   * been set.
   */
  @Test
  void buildWithoutStreetNumberTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withPostCode("8041")
            .atDistrict("Ashburton");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before street name has
   * been set.
   */
  @Test
  void buildWithoutStreetNameTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber("1")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withPostCode("8041")
            .atDistrict("Ashburton");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before city has been set.
   */
  @Test
  void buildWithoutCityTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber("1")
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withPostCode("8041")
            .atDistrict("Ashburton");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before region has
   * been set.
   */
  @Test
  void buildWithoutRegionTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber("1")
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inCountry("New Zealand")
            .withPostCode("8041")
            .atDistrict("Ashburton");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before country has
   * been set.
   */
  @Test
  void buildWithoutCountryTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber("1")
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .withPostCode("8041")
            .atDistrict("Ashburton");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before zip code has
   * been set.
   */
  @Test
  void buildWithoutZipCodeTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber("1")
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .atDistrict("Ashburton");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() builds successfully even with no district.
   */
  @Test
  void buildWithoutDistrictTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber("1")
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withPostCode("8041")
            .atDistrict("");
    assertNotNull(locationBuilder);
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the street number of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  void buildStreetNumberTest() {
    Location location = locationBuilder.build();
    assertEquals("1", location.getStreetNumber());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the street name of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  void buildStreetNameTest() {
    Location location = locationBuilder.build();
    assertEquals("Elizabeth Street", location.getStreetName());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the city of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  void buildCityTest() {
    Location location = locationBuilder.build();
    assertEquals("Christchurch", location.getCity());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the region of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  void buildRegionTest() {
    Location location = locationBuilder.build();
    assertEquals("Canterbury", location.getRegion());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the country of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  void buildCountryTest() {
    Location location = locationBuilder.build();
    assertEquals("New Zealand", location.getCountry());
  }

  
  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the district of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  void buildDistrictTest() {
    Location location = locationBuilder.build();
    assertEquals("Ashburton", location.getDistrict());
  }

  /**
   * Test that the JSON produced by constructFullJson includes the street number, street name,
   * city, region, country and postcode of the location object.
   */
  @Test
  void constructFullJsonIncludesAllExpectedFieldsTest() {
    Location location = locationBuilder.build();
    JSONObject json = location.constructFullJson();
    assertTrue(json.containsKey("streetNumber"));
    assertTrue(json.containsKey("streetName"));
    assertTrue(json.containsKey("city"));
    assertTrue(json.containsKey("region"));
    assertTrue(json.containsKey("country"));
    assertTrue(json.containsKey("postcode"));
    assertTrue(json.containsKey("district"));
  }

  /**
   * Test that the JSON proced by constructFullJson does not include any attributes appart from street
   * number, street name, city, region, country and postcode.
   */
  @Test
  void constructFullJsonOnlyIncludesExpectedFieldsTest() {
    Location location = locationBuilder.build();
    JSONObject json = location.constructFullJson();
    json.remove("streetNumber");
    json.remove("streetName");
    json.remove("city");
    json.remove("region");
    json.remove("country");
    json.remove("postcode");
    json.remove("district");
    assertTrue(json.isEmpty());
  }

  /**
   * Test that all fields of the JSON produced by constuctFullJson have the expected value.
   */
  @Test
  void constructFullJsonFieldsHaveExpectedValueTest() {
    Location location = locationBuilder.build();
    JSONObject json = location.constructFullJson();
    assertEquals(location.getStreetNumber(), json.getAsString("streetNumber"));
    assertEquals(location.getStreetName(), json.getAsString("streetName"));
    assertEquals(location.getCity(), json.getAsString("city"));
    assertEquals(location.getRegion(), json.getAsString("region"));
    assertEquals(location.getCountry(), json.getAsString("country"));
    assertEquals(location.getPostCode(), json.getAsString("postcode"));
    assertEquals(location.getDistrict(), json.getAsString("district"));
  }

    /**
   * Test that the JSON produced by constructPartialJson includes the city, region and country
   * of the location object.
   */
  @Test
  void constructPartialJsonIncludesAllExpectedFieldsTest() {
    Location location = locationBuilder.build();
    JSONObject json = location.constructPartialJson();
    assertTrue(json.containsKey("city"));
    assertTrue(json.containsKey("region"));
    assertTrue(json.containsKey("country"));
  }

  /**
   * Test that the JSON proced by constructPartialJson does not include any attributes apart from 
   * city, region and country.
   */
  @Test
  void constructPartialJsonOnlyIncludesExpectedFieldsTest() {
    Location location = locationBuilder.build();
    JSONObject json = location.constructPartialJson();
    json.remove("city");
    json.remove("region");
    json.remove("country");
    assertTrue(json.isEmpty());
  }

  /**
   * Test that all fields of the JSON produced by constuctPartialJson have the expected value.
   */
  @Test
  void constructPartialJsonFieldsHaveExpectedValueTest() {
    Location location = locationBuilder.build();
    JSONObject json = location.constructPartialJson();
    assertEquals(location.getCity(), json.getAsString("city"));
    assertEquals(location.getRegion(), json.getAsString("region"));
    assertEquals(location.getCountry(), json.getAsString("country"));
  }
}
