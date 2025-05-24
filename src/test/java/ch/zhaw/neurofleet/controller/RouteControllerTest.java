package ch.zhaw.neurofleet.controller;

import static ch.zhaw.neurofleet.security.Roles.ADMIN;
import static ch.zhaw.neurofleet.security.Roles.FLEETMANAGER;
import static ch.zhaw.neurofleet.security.Roles.OWNER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Answers;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.Route;
import ch.zhaw.neurofleet.repository.RouteRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.RouteService;
import ch.zhaw.neurofleet.service.UserService;
import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.repository.LocationRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class RouteControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private RouteRepository routeRepository;

        @MockitoBean
        private RouteService routeService;

        @MockitoBean
        private UserService userService;

        @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
        private OpenAiChatModel chatModel;

        @MockitoBean
        private JobRepository jobRepository;

        @MockitoBean
        private LocationRepository locationRepository;

        private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        private static final String ROUTE_ID = "route-123";
        private static final String COMPANY_ID = "comp-1";
        private static final String VEHICLE_ID = "veh-1";
        private static final List<String> JOB_IDS = List.of("job-1", "job-2");
        private static final String TEST_ROUTE_DESCRIPTION = "Test Route";
        private static final String ORIGIN_ID = "loc-1";
        private static final String DESTINATION_ID = "loc-2";

        private Route baseRoute;
        private Job testJob;
        private Location originLocation;
        private Location destinationLocation;

        @BeforeEach
        void setup() {
                // AI-Mock
                when(chatModel.call(any(Prompt.class))
                                .getResult()
                                .getOutput()
                                .getText()).thenReturn(TEST_ROUTE_DESCRIPTION);

                // Setup locations
                originLocation = new Location("Origin", "Origin Address", 47.37402059999999, 8.5382115, COMPANY_ID);
                originLocation.setId(ORIGIN_ID);
                destinationLocation = new Location("Destination", "Destination Address", 47.37402059999999, 8.5382115,
                                COMPANY_ID);
                destinationLocation.setId(DESTINATION_ID);

                // Setup test job
                testJob = new Job(TEST_ROUTE_DESCRIPTION, LocalDateTime.now(), ORIGIN_ID, DESTINATION_ID, COMPANY_ID,
                                100);
                testJob.setId(JOB_IDS.get(0));

                // Setup base route
                baseRoute = new Route();
                baseRoute.setId(ROUTE_ID);
                baseRoute.setDescription(TEST_ROUTE_DESCRIPTION);
                baseRoute.setScheduledTime(LocalDateTime.now());
                baseRoute.setVehicleId(VEHICLE_ID);
                baseRoute.setCompanyId(COMPANY_ID);
                baseRoute.setJobIds(new ArrayList<>(JOB_IDS));
                baseRoute.setTotalPayloadKg(200);

                // Setup repository mocks
                when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(baseRoute));
                when(routeRepository.existsById(ROUTE_ID)).thenReturn(true);
                when(routeRepository.save(any())).thenReturn(baseRoute);
                when(routeRepository.findAll(PageRequest.of(0, 5))).thenReturn(new PageImpl<>(List.of(baseRoute)));

                when(jobRepository.findAllById(JOB_IDS)).thenReturn(List.of(testJob));
                when(locationRepository.findById(ORIGIN_ID)).thenReturn(Optional.of(originLocation));
                when(locationRepository.findById(DESTINATION_ID)).thenReturn(Optional.of(destinationLocation));
        }

        @Test
        @Order(1)
        void testCreateRoute_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(routeService.createRoute(any())).thenReturn(baseRoute);

                String json = objectMapper.writeValueAsString(baseRoute);

                mvc.perform(post("/api/routes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(ROUTE_ID))
                                .andExpect(jsonPath("$.description").value(TEST_ROUTE_DESCRIPTION))
                                .andExpect(jsonPath("$.vehicleId").value(VEHICLE_ID));
        }

        @Test
        @Order(2)
        void testCreateRoute_OwnerRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(routeService.createRoute(any())).thenReturn(baseRoute);

                String json = objectMapper.writeValueAsString(baseRoute);

                mvc.perform(post("/api/routes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.description").value(TEST_ROUTE_DESCRIPTION));
        }

        @Test
        @Order(3)
        void testCreateRoute_FleetmanagerRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(routeService.createRoute(any())).thenReturn(baseRoute);

                String json = objectMapper.writeValueAsString(baseRoute);

                mvc.perform(post("/api/routes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.description").value(TEST_ROUTE_DESCRIPTION));
        }

        @Test
        @Order(4)
        void testCreateRoute_UnauthorizedRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                String json = objectMapper.writeValueAsString(baseRoute);

                mvc.perform(post("/api/routes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER)
                                .content(json))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(5)
        void testGetAllRoutes_AsAdmin() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER, FLEETMANAGER)).thenReturn(false);
                when(routeRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(baseRoute)));

                mvc.perform(get("/api/routes")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(ROUTE_ID));
        }

        @Test
        @Order(6)
        void testGetAllRoutes_AsOwner() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER, FLEETMANAGER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);
                when(routeRepository.findAllByCompanyId(eq(COMPANY_ID), any(PageRequest.class)))
                                .thenReturn(new PageImpl<>(List.of(baseRoute)));

                mvc.perform(get("/api/routes")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(ROUTE_ID))
                                .andExpect(jsonPath("$.content[0].companyId").value(COMPANY_ID));
        }

        @Test
        @Order(7)
        void testGetAllRoutes_AsUnauthorizedUser() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                mvc.perform(get("/api/routes")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(8)
        void testUpdateRoute_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(routeService.updateRoute(eq(ROUTE_ID), any())).thenReturn(baseRoute);

                String json = objectMapper.writeValueAsString(baseRoute);

                mvc.perform(put("/api/routes/" + ROUTE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(ROUTE_ID));
        }

        @Test
        @Order(9)
        void testUpdateRoute_NotFound() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(routeService.updateRoute(eq(ROUTE_ID), any()))
                                .thenThrow(new NoSuchElementException("Route not found"));

                String json = objectMapper.writeValueAsString(baseRoute);

                mvc.perform(put("/api/routes/" + ROUTE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(json))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(10)
        void testDeleteRoute_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);

                mvc.perform(delete("/api/routes/" + ROUTE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(content().string("DELETED"));
        }

        @Test
        @Order(11)
        void testDeleteRoute_UnauthorizedRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                mvc.perform(delete("/api/routes/" + ROUTE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }
}
