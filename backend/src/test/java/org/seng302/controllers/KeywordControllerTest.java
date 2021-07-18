package org.seng302.controllers;

import io.cucumber.java.sl.In;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.tomcat.jni.Local;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.seng302.entities.*;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.persistence.*;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class KeywordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KeywordRepository keywordRepository;

    private MockedStatic<AuthenticationTokenManager> authenticationTokenManager;

    @BeforeEach
    public void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);

        // By default this will mock checkAuthenticationToken method to do nothing, which simulates a valid authentication token
        authenticationTokenManager = Mockito.mockStatic(AuthenticationTokenManager.class);

        var keywordController = new KeywordController(keywordRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(keywordController).build();
    }


    @AfterEach
    public void tearDown() {
        authenticationTokenManager.close();
    }

    @Test
    void searchKeywords_noAuthentication_401Response() throws Exception {
        // Mock the AuthenticationTokenManager to respond as it would when the authentication token is missing or invalid
        authenticationTokenManager.when(() -> AuthenticationTokenManager.checkAuthenticationToken(any()))
                .thenThrow(new AccessTokenException());

        // Verify that a 401 response is received in response to the GET request
        mockMvc.perform(get("/keywords/search"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Check that the authentication token manager was called
        authenticationTokenManager.verify(() -> AuthenticationTokenManager.checkAuthenticationToken(any()));
    }

    @Test
    void searchKeywords_withAuthentication_returnsKeywordList() throws Exception {
        List<Keyword> keywords = new ArrayList<>();
        for (String keywordName : List.of("Keyword One", "Keyword Two", "Keyword Three")) {
            Keyword mockKeyword = mock(Keyword.class);

            JSONObject mockResponse = new JSONObject();
            mockResponse.put("name", keywordName);
            when(mockKeyword.constructJSONObject()).thenReturn(mockResponse);

            keywords.add(mockKeyword);
        }

        when(keywordRepository.findByOrderByNameAsc()).thenReturn(keywords);

        MvcResult result = mockMvc.perform(get("/keywords/search"))
                .andExpect(status().isOk())
                .andReturn();

        verify(keywordRepository).findByOrderByNameAsc();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        Object response = parser.parse(result.getResponse().getContentAsString());

        JSONArray expected = new JSONArray();

        expected.addAll(keywords.stream().map(Keyword::constructJSONObject).collect(Collectors.toList()));

        assertEquals(expected, response);
    }
}
