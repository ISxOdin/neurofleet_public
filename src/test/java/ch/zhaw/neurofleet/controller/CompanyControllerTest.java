package ch.zhaw.neurofleet.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.security.TestSecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class CompanyControllerTest {
    @Autowired
    private MockMvc mvc;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static String company_id = "";

    private static final String TEST_NAME = "TestCompany";
    private static final String TEST_EMAIL = "test@neurofleet.com";
    private static final String TEST_ADDRESS = "Bahnhofstrasse 1, 8000 Zürich";
    private static final double TEST_LATITUDE = 47.3673442;
    private static final double TEST_LONGITUDE = 8.5398742;

    @Test
    @Order(1)
    void testCreateCompany() throws Exception {
        Company company = new Company(TEST_NAME, TEST_EMAIL, TEST_ADDRESS,
                TEST_LATITUDE, TEST_LONGITUDE);
        String jsonBody = objectMapper.writeValueAsString(company);

        var result = mvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                .content(jsonBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        // get the id of the created company
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        company_id = jsonNode.get("id").asText();
    }

    @Test
    @Order(2)
    public void testGetCompany() throws Exception {
        mvc.perform(get("/api/companies/" + company_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(
                        TEST_NAME))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.address").value(
                        TEST_ADDRESS))
                .andExpect(jsonPath("$.latitude").value(
                        TEST_LATITUDE))
                .andExpect(jsonPath("$.longitude").value(
                        TEST_LONGITUDE))
                .andReturn();
    }

    @Test
    @Order(3)
    public void testDeleteCompany() throws Exception {
        mvc.perform(delete("/api/companies/" + company_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testCreateCompany_RoleNotAllowed() throws Exception {
        mvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
