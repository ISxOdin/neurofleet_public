package ch.zhaw.neurofleet.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.MailInformation;
import ch.zhaw.neurofleet.repository.CompanyRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.CompanyService;
import ch.zhaw.neurofleet.service.MailValidatorService;
import ch.zhaw.neurofleet.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class CompanyControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private CompanyRepository companyRepository;

        @MockitoBean
        private CompanyService companyService;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private MailValidatorService mailValidatorService;

        private static final ObjectMapper objectMapper = new ObjectMapper();

        private static final String company_id = "mocked-id";

        // Test data
        private static final String TEST_NAME = "TestCompany";
        private static final String TEST_EMAIL = "test@neurofleet.com";
        private static final String TEST_ADDRESS = "Bahnhofstrasse 1, 8000 Zürich";
        private static final double TEST_LATITUDE = 47.3673442;
        private static final double TEST_LONGITUDE = 8.5398742;
        private static final String TEST_OWNER = "auth0%7C680f789aec95b328eea724e6";

        private static final String UPDATED_TEST_NAME = "UpdatedCompany";
        private static final String UPDATED_TEST_EMAIL = "updatedtest@neurofleet.com";
        private static final String UPDATED_TEST_ADDRESS = "Bahnhofstrasse 2, 8000 Zürich";
        private static final double UPDATED_TEST_LATITUDE = 47.36770019999999;
        private static final double UPDATED_TEST_LONGITUDE = 8.5402586;

        private Company baseCompany;

        @BeforeEach
        void setupMocks() {
                baseCompany = new Company(TEST_NAME, TEST_EMAIL, TEST_ADDRESS, TEST_LATITUDE, TEST_LONGITUDE);
                baseCompany.setId(company_id);
                baseCompany.setOwner(TEST_OWNER);

                when(userService.userHasAnyRole("admin")).thenReturn(true);
                when(companyRepository.findById(company_id)).thenReturn(Optional.of(baseCompany));
                when(companyRepository.existsById(company_id)).thenReturn(true);
                when(companyRepository.save(any())).thenReturn(baseCompany);
                when(companyService.createCompany(any(), any(), any())).thenReturn(baseCompany);
                when(companyService.updateCompany(eq(company_id), any())).thenReturn(baseCompany);
        }

        @Test
        public void testCreateCompany_UserRole() throws Exception {
                when(userService.userHasAnyRole("admin")).thenReturn(false);

                mvc.perform(post("/api/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andDo(print())
                                .andExpect(status().isForbidden());
        }

        @Test
        public void testCreateCompany_DriverRole() throws Exception {
                when(userService.userHasAnyRole("admin")).thenReturn(false);

                mvc.perform(post("/api/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.DRIVER))
                                .andDo(print())
                                .andExpect(status().isForbidden());
        }

        @Test
        public void testCreateCompany_FleetManagerRole() throws Exception {
                when(userService.userHasAnyRole("admin")).thenReturn(false);

                mvc.perform(post("/api/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER))
                                .andDo(print())
                                .andExpect(status().isForbidden());
        }

        @Test
        public void testCreateCompany_OwnerRole() throws Exception {
                when(userService.userHasAnyRole("admin")).thenReturn(false);

                mvc.perform(post("/api/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andDo(print())
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(1)
        void testCreateCompany() throws Exception {
                MailInformation mail = new MailInformation();
                mail.setDisposable(false);
                mail.setFormat(true);
                mail.setDns(true);

                String jsonBody = objectMapper.writeValueAsString(baseCompany);

                when(mailValidatorService.validateEmail(TEST_EMAIL)).thenReturn(mail);

                var result = mvc.perform(post("/api/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(company_id))
                                .andReturn();

                String jsonResponse = result.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(jsonResponse);
                assertTrue(jsonNode.has("id"));
        }

        @Test
        @Order(2)
        public void testGetCompany() throws Exception {
                mvc.perform(get("/api/companies/" + company_id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value(TEST_NAME))
                                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                                .andExpect(jsonPath("$.address").value(TEST_ADDRESS))
                                .andExpect(jsonPath("$.latitude").value(TEST_LATITUDE))
                                .andExpect(jsonPath("$.longitude").value(TEST_LONGITUDE));
        }

        @Test
        @Order(3)
        public void testGetAllCompanies() throws Exception {
                PageImpl<Company> mockPage = new PageImpl<>(List.of(baseCompany));
                when(companyRepository.findAll(PageRequest.of(0, 5))).thenReturn(mockPage);

                mvc.perform(get("/api/companies?pageNumber=1&pageSize=5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].name").value(TEST_NAME))
                                .andExpect(jsonPath("$.content[0].email").value(TEST_EMAIL))
                                .andExpect(jsonPath("$.content[0].address").value(TEST_ADDRESS))
                                .andExpect(jsonPath("$.content[0].latitude").value(TEST_LATITUDE))
                                .andExpect(jsonPath("$.content[0].longitude").value(TEST_LONGITUDE));
        }

        @Test
        @Order(4)
        void testUpdateCompany() throws Exception {
                Company updated = new Company(UPDATED_TEST_NAME, UPDATED_TEST_EMAIL, UPDATED_TEST_ADDRESS,
                                UPDATED_TEST_LATITUDE, UPDATED_TEST_LONGITUDE);
                updated.setOwner(TEST_OWNER);
                updated.setId(company_id);

                when(companyService.updateCompany(eq(company_id), any())).thenReturn(updated);

                String jsonBody = objectMapper.writeValueAsString(updated);

                mvc.perform(put("/api/companies/" + company_id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(jsonBody))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value(UPDATED_TEST_NAME))
                                .andExpect(jsonPath("$.email").value(UPDATED_TEST_EMAIL))
                                .andExpect(jsonPath("$.address").value(UPDATED_TEST_ADDRESS))
                                .andExpect(jsonPath("$.latitude").value(UPDATED_TEST_LATITUDE))
                                .andExpect(jsonPath("$.longitude").value(UPDATED_TEST_LONGITUDE))
                                .andExpect(jsonPath("$.owner").value(TEST_OWNER));
        }

        @Test
        @Order(5)
        void testUpdateCompany_ForbiddenForOwner() throws Exception {
                Company company = new Company(UPDATED_TEST_NAME, UPDATED_TEST_EMAIL, UPDATED_TEST_ADDRESS,
                                UPDATED_TEST_LATITUDE, UPDATED_TEST_LONGITUDE);
                company.setOwner(TEST_OWNER);

                String jsonBody = objectMapper.writeValueAsString(company);

                when(userService.userHasAnyRole("admin")).thenReturn(false);

                mvc.perform(put("/api/companies/" + company_id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andDo(print())
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(6)
        public void testDeleteCompany() throws Exception {
                mvc.perform(delete("/api/companies/" + company_id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @Test
        @Order(7)
        public void testIfCompanyIsDeleted() throws Exception {
                when(companyRepository.existsById(company_id)).thenReturn(false);

                mvc.perform(delete("/api/companies/" + company_id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(8)
        public void testAddUserToCompany() throws Exception {
                when(companyService.addUserToCompany(eq(company_id), eq("user123"))).thenReturn(baseCompany);

                mvc.perform(post("/api/companies/" + company_id + "/users/user123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("ASSIGNED"));
        }

        @Test
        @Order(9)
        public void testRemoveUserFromCompany() throws Exception {

                mvc.perform(delete("/api/companies/" + company_id + "/users/user123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        // Test for invalid email format

        @Test
        public void testCreateCompany_InvalidEmail_Disposable() throws Exception {
                MailInformation mail = new MailInformation();
                mail.setFormat(true);
                mail.setDns(true);
                mail.setDisposable(true);

                when(mailValidatorService.validateEmail(TEST_EMAIL)).thenReturn(mail);

                String jsonBody = objectMapper.writeValueAsString(baseCompany);

                mvc.perform(post("/api/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andDo(print())
                                .andExpect(status().isBadRequest());
        }

}
