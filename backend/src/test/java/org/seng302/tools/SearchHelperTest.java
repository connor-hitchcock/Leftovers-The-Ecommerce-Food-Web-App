package org.seng302.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.controllers.DGAAController;
import org.seng302.entities.Location;
import org.seng302.entities.User;
import org.seng302.exceptions.SearchFormatException;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.persistence.UserSpecificationsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchHelperTest {

    /**
     * List of users to be used in testsing of getPageInResults.
     */
    private List<User> pagingUserList;

    /**
     * List of users to be saved to the repository and used for testing of getSort.
     */
    private List<User> savedUserList;

    /**
     * Repository storing user entities.
     */
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private DGAAController dgaaController;
    /**
     * Speification for repository queries.
     */
    private Specification<User> spec;

    /**
     * Read user info from file UserSearchHelperTestData1.csv, use this information to construct User objects and add them to userList
     * @throws ParseException
     * @throws IOException
     */
    @BeforeEach
    void setUp() throws ParseException, IOException {
        UserSpecificationsBuilder builder = new UserSpecificationsBuilder()
                .with("firstName", ":", "andy", true)
                .with("middleName", ":", "andy", true)
                .with("lastName", ":", "andy", true)
                .with("nickname", ":", "andy", true);
        spec = builder.build();

        pagingUserList = readUserFile("src/test/testFiles/UserSearchHelperTestData1.csv");
        savedUserList = readUserFile("src/test/testFiles/UserSearchHelperTestData2.csv");

        dgaaController.checkDGAA();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        for (User user : savedUserList) {
            userRepository.save(user);
        }
    }

    private List<User> readUserFile(String filepath) throws IOException {
        List<User> userList = new ArrayList<>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(filepath));
        while ((row = csvReader.readLine()) != null) {
            try {
                String[] userData = row.split("\\|");
                User user = new User.Builder().withFirstName(userData[0]).withMiddleName(userData[1]).withLastName(userData[2]).withNickName(userData[3])
                        .withEmail(userData[4]).withPassword(userData[5]).withAddress(Location.covertAddressStringToLocation(userData[6])).withDob(userData[7]).build();
                userList.add(user);
            } catch (Exception e) {

            }
        }
        csvReader.close();
        return userList;
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a non-null requested
     * page number which is above one and below the maximum, it will return the requested page with the requested number
     * of users on it.
     */
    @Test
    void getPageInResultsValidResultsPerPageValidRequestedPageTest() {
        List<User> result = SearchHelper.getPageInResults(pagingUserList, 2, 10);
        assertArrayEquals(pagingUserList.subList(10, 20).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and null as the page number,
     * it will return the first page in the results with the requested number of users on it.
     */
    @Test
    void getPageInResultsValidResultsPerPageNullRequestedPageTest() {
        List<User> result = SearchHelper.getPageInResults(pagingUserList, null, 10);
        assertArrayEquals(pagingUserList.subList(0, 10).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a negative number as
     * the page number, it will return the first page in the results with the requested number of users on it.
     */
    @Test
    void getPageInResultsValidResultsPerPageNegativeRequestedPageTest() {
        List<User> result = SearchHelper.getPageInResults(pagingUserList, -3, 10);
        assertArrayEquals(pagingUserList.subList(0, 10).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a number above the maximum as
     * the page number, it will return the last page in the results when the results are divided according to the requested
     * number of results per page.
     */
    @Test
    void getPageInResultsValidResultsPerPageAboveMaximumRequestedPageTest() {
        List<User> result = SearchHelper.getPageInResults(pagingUserList, 100, 10);
        assertArrayEquals(pagingUserList.subList(20, 26).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with null as the number of results per page and a non-null requested
     * page number which is above zero and below the maximum, it will return the requested page with 15 users per page.
     */
    @Test
    void getPageInResultsNegativeResultsPerPageValidRequestedPageTest() {
        List<User> result = SearchHelper.getPageInResults(pagingUserList, 1, -100);
        assertArrayEquals(pagingUserList.subList(0, 15).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with negative number as the number of results per page and a non-null
     * requested page number which is above zero and below the maximum, it will return the requested page with 15 users per page.
     */
    @Test
    void getPageInResultsNullResultsPerPageValidRequestedPageTest() {
        List<User> result = SearchHelper.getPageInResults(pagingUserList, 1, null);
        assertArrayEquals(pagingUserList.subList(0, 15).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a the final page as the
     * requested page number, but there are not enough users to fill the last page, it will return the final page with
     * with less than the requested number of users on it.
     */
    @Test
    void getPageInResultsValidResultsPerPageFinalRequestedPageTest() {
        List<User> result = SearchHelper.getPageInResults(pagingUserList, 3, 10);
        assertArrayEquals(pagingUserList.subList(20, 26).toArray(), result.toArray());
    }

    /**
     * Verify that when getPageInResults is called with a non-null number of results per page and a non-null requested
     * page number, but queryResults is an empty list, it will return an empty list.
     */
    @Test
    void getPageInResultsQueryResultsEmptyTest() {
        List<User> emptyList = new ArrayList<User>();
        List<User> result = SearchHelper.getPageInResults(emptyList, 1, 10);
        assertArrayEquals(emptyList.toArray(), result.toArray());
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is null, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's id number.
     */
    @Test
    void getSortOrderByNullTest()  {
        Sort userSort = SearchHelper.getSort(null, null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        Long previousId = firstUser.getUserID();
        for (int i = 1; i < queryResults.size(); i++) {
            Long currentId = queryResults.get(i).getUserID();
            assertTrue(currentId > previousId);
            previousId = currentId;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is firstName, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's firstName parameter.
     */
    @Test
    void getSortOrderByFirstNameTest() {
        Sort userSort = SearchHelper.getSort("firstName", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousFirstName = firstUser.getFirstName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentFirstName = queryResults.get(i).getFirstName();
            assertTrue(currentFirstName.compareTo(previousFirstName) >= 0);
            previousFirstName = currentFirstName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is middleName, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's middleName parameter.
     */
    @Test
    void getSortOrderByMiddleNameTest() {
        Sort userSort = SearchHelper.getSort("middleName", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousMiddleName = firstUser.getMiddleName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentMiddleName = queryResults.get(i).getMiddleName();
            assertTrue(currentMiddleName.compareTo(previousMiddleName) >= 0);
            previousMiddleName = currentMiddleName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is lastName, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's lastName parameter.
     */
    @Test
    void getSortOrderByLastNameTest() {
        Sort userSort = SearchHelper.getSort("lastName", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousLastName = firstUser.getLastName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentLastName = queryResults.get(i).getLastName();
            assertTrue(currentLastName.compareTo(previousLastName) >= 0);
            previousLastName = currentLastName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is nickname, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's nickname parameter.
     */
    @Test
    void getSortOrderByNicknameTest() {
        Sort userSort = SearchHelper.getSort("nickname", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousNickname = firstUser.getNickname();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentNickname = queryResults.get(i).getNickname();
            assertTrue(currentNickname.compareTo(previousNickname) >= 0);
            previousNickname = currentNickname;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is email, it returns a Sort which when applied to
     * a query of UserRepository causes the results to be ordered by the user's email parameter.
     */
    @Test
    void getSortOrderByEmailTest() {
        Sort userSort = SearchHelper.getSort("email", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousEmail = firstUser.getEmail();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentEmail = queryResults.get(i).getEmail();
            assertTrue(currentEmail.compareTo(previousEmail) >= 0);
            previousEmail = currentEmail;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is firstName and the reverse parameter is true,
     * it returns a Sort which when applied to a query of UserRepository causes the results to be in reverse order
     * by the user's firstName parameter.
     */
    @Test
    void getSortOrderByFirstNameReverseTrueTest() {
        Sort userSort = SearchHelper.getSort("firstName", true);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousFirstName = firstUser.getFirstName();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentFirstName = queryResults.get(i).getFirstName();
            assertTrue(currentFirstName.compareTo(previousFirstName) <= 0);
            previousFirstName = currentFirstName;
        }
    }

    /**
     * Verify that when getSort is called, and the orderBy parameter is email and the reverse parameter is false,
     * it returns a Sort which when applied to a query of UserRepository causes the results to be in alphabetical order
     * by the user's email parameter.
     */
    @Test
    void getSortOrderByEmailReverseFalseTest() {
        Sort userSort = SearchHelper.getSort("email", false);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        String previousEmail = firstUser.getEmail();
        for (int i = 1; i < queryResults.size(); i++) {
            String currentEmail = queryResults.get(i).getEmail();
            assertTrue(currentEmail.compareTo(previousEmail) >= 0);
            previousEmail = currentEmail;
        }
    }

    /**
     * Verify that when getSort is called with a parameter for orderBy which does not appear in orderByOptions, it returns
     * a Sort which when applied to a query of UserRepository causes the results to be in numerical order by the user's
     * id number.
     */
    @Test
    void getSortOrderByInvalidOptionTest() {
        Sort userSort = SearchHelper.getSort("dateOfBirth", null);
        List<User> queryResults = userRepository.findAll(spec, userSort);
        User firstUser = queryResults.get(0);
        Long previousId = firstUser.getUserID();
        for (int i = 1; i < queryResults.size(); i++) {
            Long currentId = queryResults.get(i).getUserID();
            assertTrue(currentId > previousId);
            previousId = currentId;
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with an empty string as the argument,
     * as SearchFormatException is thrown.
     */
    @Test
    void constructUserSpecificationFromSearchQueryEmptyStringTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructUserSpecificationFromSearchQuery("");
        });
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which matches Users in the UserRepository which have a name
     * that exactly matches that word.
     */
    @Test
    void constructUserSpecificationFromSearchQueryDoubleQuotesExactMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("\"Carl\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Carl", matches.get(0).getFirstName());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which matches Users in the UserRepository which have a name
     * that exactly matches that word.
     */
    @Test
    void constructUserSpecificationFromSearchQuerySingleQuotesExactMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("'Petra'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        assertEquals("Petra", matches.get(0).getMiddleName());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is
     * a partial match for that word.
     */
    @Test
    void constructUserSpecificationFromSearchQueryDoubleQuotesPartialMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("\"Car\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is
     * a partial match for that word.
     */
    @Test
    void constructUserSpecificationFromSearchQuerySingleQuotesPartialMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("'etra'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which does not match users in the repository whose name does
     * not fully or partially match that word.
     */
    @Test
    void constructUserSpecificationFromSearchQueryDoubleQuotesNoMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("\"zzz\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository whose name does
     * not fully or partially match that word.
     */
    @Test
    void constructUserSpecificationFromSearchQuerySingleQuotesNoMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("'X'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is a
     * substring of the argument
     */
    @Test
    void constructUserSpecificationFromSearchQueryDoubleQuotesNameSubstringTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("\"Carlos\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is a
     * substring of the argument
     */
    @Test
    void constructUserSpecificationFromSearchQuerySingleQuotesNameSubstringTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("'PPPPetra'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in double
     * quotes as its argument, it returns a specification does not match users in the repository who have an name which
     * is the same as the argument but in a different case
     */
    @Test
    void constructUserSpecificationFromSearchQueryDoubleQuotesDifferentCaseMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("\"carl\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word in single
     * quotes as its argument, it returns a specification which does not match users in the repository who have a name
     * which is the same argument but in a different case
     */
    @Test
    void constructUserSpecificationFromSearchQuerySingleQuotesDifferentCaseMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("'PetRA'");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word which is not in quotes
     * as its arguement, it returns a specification which matches users with a name which is an exact match for that word.
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoQuotesExactMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("Andy");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(7, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equals("Andy") ||
                        user.getMiddleName().equals("Andy") ||
                        user.getLastName().equals("Andy") ||
                        user.getNickname().equals("Andy"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word which is not in quotes
     * as its arguement, it returns a specification which matches users with a name which is a partial match for that word.
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoQuotesPartialMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("ndy");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(7, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().contains("ndy") ||
                    user.getMiddleName().contains("ndy") ||
                    user.getLastName().contains("ndy") ||
                    user.getNickname().contains("ndy"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word without
     * quotes as its argument, it returns a specification which does not match users in the repository whose name does
     * not fully or partially match that word.
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoQuotesNoMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("q");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word without
     * quotes as its argument, it returns a specification which does not match users in the repository whose name is a
     * substring of the argument
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoQuotesNameSubstringTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("Andyyyyy");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with as single word without
     * quotes as its argument, it returns a specification will match users in the repository who have an name which
     * is the same as the argument but in a different case
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoQuotesDifferentCaseMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("ANDY");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(7, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("ANDY") ||
                    user.getMiddleName().equalsIgnoreCase("ANDY") ||
                    user.getLastName().equalsIgnoreCase("ANDY") ||
                    user.getNickname().equalsIgnoreCase("ANDY"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with just the word 'and' as its argument,
     * a SearchFormatException is thrown.
     */
    @Test
    void constructUserSpecificationFromSearchQueryJustAndTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructUserSpecificationFromSearchQuery("and");
        });
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryLowerAndBothMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("andy and \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("andy") &&
                    user.getLastName().equals("Graham"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'AND' as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryUpperAndBothMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("andy AND \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("andy") &&
                    user.getLastName().equals("Graham"));
        }
    }


    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, it returns a specification which doesn't match users which contain only one of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryAndOneMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("andy and \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, it returns a specification which doesn't match users which contain neither of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryAndNoMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("tomato and \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with just the word and 'OR' as its argument,
     * a SearchFormatException is thrown.
     */
    @Test
    void constructUserSpecificationFromSearchQueryJustOrTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructUserSpecificationFromSearchQuery("OR");
        });
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryBothMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("peter or \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertEquals("Graham", user.getLastName());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, it returns a specification which matches users which contain only one of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryLowerOrOneMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("peter or \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertFalse(user.getFirstName().equals("Potato") || user.getMiddleName().equals("Potato") ||
                user.getLastName().equals("Potato") || user.getNickname().equals("Potato"));
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'Or' as its
     * argument, it returns a specification which matches users which contain one of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryMixedCaseOneMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("peter Or \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertFalse(user.getFirstName().equals("Potato") || user.getMiddleName().equals("Potato") ||
                user.getLastName().equals("Potato") || user.getNickname().equals("Potato"));
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, it returns a specification which doesn't match users which contain neither of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryOrNoMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("tomato or \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms not joined by a predicate as its
     * argument, it returns a specification which matches users which contain both those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoPredicateBothMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("andy \"Graham\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        for (User user : matches) {
            assertTrue(user.getFirstName().equalsIgnoreCase("andy") &&
                    user.getLastName().equals("Graham"));
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms not joined by a predicate as its
     * argument, it returns a specification which doesn't match users which contain only one of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoPredicateOneMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("andy \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms not joined by a predicate as its
     * argument, it returns a specification which doesn't match users which contain neither of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryNoPredicateNoMatchTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("tomato \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'or' as its
     * argument, and there is additional whitespace between the words in the query, it still returns a specification which
     * matches users which contain only one of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryOrExtraWhitespaceTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("peter         or      \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(1, matches.size());
        User user = matches.get(0);
        assertTrue(user.getMiddleName().equalsIgnoreCase("peter"));
        assertFalse(user.getFirstName().equals("Potato") || user.getMiddleName().equals("Potato") ||
                user.getLastName().equals("Potato") || user.getNickname().equals("Potato"));
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with two terms joined by an 'and' as its
     * argument, and there is additional whitespace between the words in the query, it returns a specification which
     * doesn't match users which contain only one of those terms in their names.
     */
    @Test
    void constructUserSpecificationFromSearchQueryAndExtraWhitespaceTest() {
        Specification<User> specification = SearchHelper.constructUserSpecificationFromSearchQuery("andy   and        \"Potato\"");
        List<User> matches = userRepository.findAll(specification);
        assertEquals(0, matches.size());
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with a string containing an opening quote
     * but no closing quote, a SearchFormatException is thrown.
     */
    @Test
    void constructUserSpecificationFromSearchQueryOpeningQuoteTest() {
        assertThrows(SearchFormatException.class, () -> {
            SearchHelper.constructUserSpecificationFromSearchQuery("\"hello");
        });
    }

    /**
     * Verify that getQueryStringWithoutOr replaces lower case 'or' in the query string.
     */
    @Test
    void getQueryStringWithoutOrLowerCaseTest() {
        assertEquals("this and that", SearchHelper.getQueryStringWithoutOr("this or that"));
    }

    /**
     * Verify that getQueryStringWithoutOr replaces upper case 'OR' in the query string.
     */
    @Test
    void getQueryStringWithoutOrUpperCaseTest() {
        assertEquals("ME and YOU", SearchHelper.getQueryStringWithoutOr("ME OR YOU"));
    }

    /**
     * Verify that getQueryStringWithoutOr doesn't replace or if it is in quotes.
     */
    @Test
    void getQueryStringWithoutOrQuotesTest() {
        assertEquals("chicken 'or' beef", SearchHelper.getQueryStringWithoutOr("chicken 'or' beef"));
    }

    /**
     * Verify that getQueryStringWithoutOr doesn't replace the sequence 'or' within a word
     */
    @Test
    void getQueryStringWithoutOrWithinWordTest() {
        assertEquals("corn horn and orchard", SearchHelper.getQueryStringWithoutOr("corn horn and orchard"));
    }

    /**
     * Verify that getFullMatchesQueryString puts all words inside quotes when there are no quotes present in the string.
     */
    @Test
    void getFullMatchesQueryStringNoQuotesTest() {
        assertEquals("\"apple\" \"banana\" \"carrot\"", SearchHelper.getFullMatchesQueryString("apple banana carrot"));
    }

    /**
     * Verify that getFullMatchesQueryString doesn't put predicates into quotes when they are present within a string.
     */
    @Test
    void getFullMatchesQueryStringPredicateTest() {
        assertEquals("\"Tom\" and \"Dick\" OR \"Harry\"", SearchHelper.getFullMatchesQueryString("Tom and Dick OR Harry"));
    }

    /**
     * Verify that getFullMatchesQueryString doesn't put quotes around parts of the string that are already in quotes.
     */
    @Test
    void getFullMatchesQueryStringQuotesTest() {
        assertEquals("'Wow!' \"Amazing!\" \"Incredible!\"", SearchHelper.getFullMatchesQueryString("'Wow!' Amazing! \"Incredible!\""));
    }

    /**
     * Verify that the list of users returned by getSearchResultsOrderedByRelevance will be in the correct relevance
     * order if they all have different levels of relevance.
     */
    @Test
    void getSearchResultsOrderedByRelevanceCorrectRelevanceOrderTest() throws ParseException {
        userRepository.deleteAll();
        User donaldDuck = new User.Builder().withFirstName("Donald").withLastName("Duck").withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                "Canterbury,8041"))
                .withDob("1934-06-09").withEmail("donald.duck@waddlemail.com").withPassword("HonkHonk1").build();
        User donaldSmith = new User.Builder().withFirstName("Donald").withLastName("Smith").withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                "Canterbury,8041"))
                .withDob("1994-03-08").withEmail("donald.smith@gmail.com").withPassword("abc123456789").build();
        User lucyMcDonald = new User.Builder().withFirstName("Lucy").withLastName("McDonald").withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                "Canterbury,8041"))
                .withDob("2000-11-21").withEmail("lucymcdonald@hotmail.com").withPassword("password123").build();
        userRepository.save(lucyMcDonald);
        userRepository.save(donaldDuck);
        userRepository.save(donaldSmith);
        List<User> result = SearchHelper.getSearchResultsOrderedByRelevance("Donald or Duck", userRepository, null);

        assertEquals("Donald", result.get(0).getFirstName());
        assertEquals("Duck", result.get(0).getLastName());
        assertEquals("Donald", result.get(1).getFirstName());
        assertEquals("Smith", result.get(1).getLastName());
        assertEquals("Lucy", result.get(2).getFirstName());
        assertEquals("McDonald", result.get(2).getLastName());
    }

    /**
     * Verify that the list of users returned by getSearchResultsOrderedByRelevance will be in the reverse relevance
     * order if they all have different levels of relevance and reverse is set to true.
     */
    @Test
    void getSearchResultsOrderedByRelevanceCorrectRelevanceOrderReverseTrueTest() throws ParseException {
        userRepository.deleteAll();
        User donaldDuck = new User.Builder().withFirstName("Donald").withLastName("Duck").withAddress(
                Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withDob("1934-06-09").withEmail("donald.duck@waddlemail.com").withPassword("HonkHonk1").build();
        User donaldSmith = new User.Builder().withFirstName("Donald").withLastName("Smith").withAddress(
                Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withDob("1994-03-08").withEmail("donald.smith@gmail.com").withPassword("abc123456789").build();
        User lucyMcDonald = new User.Builder().withFirstName("Lucy").withLastName("McDonald").withAddress(
                Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand,Canterbury,8041"))
                .withDob("2000-11-21").withEmail("lucymcdonald@hotmail.com").withPassword("password123").build();
        userRepository.save(lucyMcDonald);
        userRepository.save(donaldDuck);
        userRepository.save(donaldSmith);
        List<User> result = SearchHelper.getSearchResultsOrderedByRelevance("Donald or Duck", userRepository, true);

        assertEquals("Lucy", result.get(0).getFirstName());
        assertEquals("McDonald", result.get(0).getLastName());
        assertEquals("Donald", result.get(1).getFirstName());
        assertEquals("Smith", result.get(1).getLastName());
        assertEquals("Donald", result.get(2).getFirstName());
        assertEquals("Duck", result.get(2).getLastName());

    }

    /**
     * Verify that when getSearchResultsOrderedByRelevance is called but the users matching the search query all have
     * the same level of relevance, they are ordered by their id number.
     */
    @Test
    void getSearchResultsOrderedByRelevanceCorrectIdOrderTest() {
        List<User> result = SearchHelper.getSearchResultsOrderedByRelevance("andy", userRepository, null);
        User firstUser = result.get(0);
        Long previousId = firstUser.getUserID();
        for (int i = 1; i < result.size(); i++) {
            Long currentId = result.get(i).getUserID();
            assertTrue(currentId > previousId);
            previousId = currentId;
        }
    }

    /**
     * Verify that when getSearchUserResultsOrderedByRelevance is called with a search query that will match
     * users with different levels of relevance, there is no duplication of users in the results.
     */
    @Test
    void getSearchResultsOrderedByRelevanceNoDuplicationTest() {
        List<User> result = SearchHelper.getSearchResultsOrderedByRelevance("a or Donna or Percy", userRepository, null);
        HashSet<Long> ids = new HashSet<>();
        for (User user : result) {
            assertFalse(ids.contains(user.getUserID()));
            ids.add(user.getUserID());
        }
    }

    /**
     * Verify that when constructUserSpecificationFromSearchQuery is called with "DGAA" in double or single
     * quotes as its argument, it will not return the DGAA result.
     */
    @Test
    void constructUserSpecificationFromSearchQueryToMatchDGAATest() {
        Specification<User> specificationDouble = SearchHelper.constructUserSpecificationFromSearchQuery("\"DGAA\"");
        List<User> matchesDouble = userRepository.findAll(specificationDouble);
        Specification<User> specificationSingle = SearchHelper.constructUserSpecificationFromSearchQuery("\'DGAA\'");
        List<User> matchesSingle = userRepository.findAll(specificationSingle);
        assertEquals(0, matchesDouble.size());
        assertEquals(0, matchesSingle.size());
    }

    /**
     * Tests that the DGAA filter does not remove regular users
     */
    @Test
    void removingDGAAAcountsDoesNotRemoveNormalAccounts() {
        List<User> listCopy = new ArrayList<>(savedUserList);
        List<User> filteredUserList = SearchHelper.removeDGAAAccountFromResults(listCopy);
        assertEquals(savedUserList, filteredUserList);
    }

    /**
     * Tests that the DGAA filter does not remove regular users
     */
    @Test
    void removingDGAAAcountsDoesNotRemovesDGAA() throws ParseException {
        List<User> listCopy = new ArrayList<>(savedUserList);

        User dgaa = new User.Builder()
                .withFirstName("Caroline")
                .withMiddleName("Jane")
                .withLastName("Smith")
                .withNickName("Carrie")
                .withEmail("carriesmith@hotmail.com")
                .withPassword("h375dj82")
                .withDob("2001-03-11")
                .withPhoneNumber("+64 3 748 7562")
                .withAddress(Location.covertAddressStringToLocation("24,Albert Road,Ashburton,Auckland,Auckland,New KZealand,0624"))
                .build();
        dgaa.setRole("defaultGlobalApplicationAdmin");

        listCopy.add(3, dgaa);

        List<User> filteredUserList = SearchHelper.removeDGAAAccountFromResults(listCopy);
        assertEquals(savedUserList, filteredUserList);
    }
}