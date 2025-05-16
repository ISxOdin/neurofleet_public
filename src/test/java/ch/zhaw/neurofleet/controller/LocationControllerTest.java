package ch.zhaw.neurofleet.controller;

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
import java.util.NoSuchElementException;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.model.LocationCreateDTO;
import ch.zhaw.neurofleet.repository.LocationRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.LocationService;
import ch.zhaw.neurofleet.service.UserService;
import static ch.zhaw.neurofleet.security.Roles.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class LocationControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private LocationRepository locationRepository;

        @MockitoBean
        private LocationService locationService;

        @MockitoBean
        private UserService userService;

        private static final ObjectMapper objectMapper = new ObjectMapper();

        private static final String LOCATION_ID = "mocked-location-id";
        private static final String COMPANY_ID = "mocked-company-id";

        // Test data
        private static final String TEST_NAME = "TestLocation";
        private static final String TEST_ADDRESS = "Bahnhofstrasse 5, Zürich, Switzerland";
        private static final double TEST_LATITUDE = 47.37402059999999;
        private static final double TEST_LONGITUDE = 8.5382115;
        private static final String FLEETMANAGER_ID = "auth0%7C680f789aec95b328eea724e6";

        private static final String UPDATED_TEST_NAME = "UpdatedTestLocation";
        private static final String UPDATED_TEST_ADDRESS = "Bahnhofstrasse 2, 8000 Zürich";

        private Location baseLocation;

        @BeforeEach
        void setup() {
                baseLocation = new Location(
                                TEST_NAME,
                                TEST_ADDRESS,
                                TEST_LATITUDE,
                                TEST_LONGITUDE,
                                COMPANY_ID);
                baseLocation.setId(LOCATION_ID);
                baseLocation.setFleetmanagerId(FLEETMANAGER_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);
                when(userService.getAuthIdOfCurrentUser()).thenReturn(FLEETMANAGER_ID);

                when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(baseLocation));
                when(locationRepository.existsByIdAndCompanyId(LOCATION_ID, COMPANY_ID)).thenReturn(true);
                when(locationRepository.findAll(PageRequest.of(0, 20)))
                                .thenReturn(new PageImpl<>(List.of(baseLocation)));
                when(locationRepository.findAllByCompanyId(COMPANY_ID, PageRequest.of(0, 20)))
                                .thenReturn(new PageImpl<>(List.of(baseLocation)));
                when(locationRepository.findAllByFleetmanagerId(FLEETMANAGER_ID, PageRequest.of(0, 20)))
                                .thenReturn(new PageImpl<>(List.of(baseLocation)));
        }

        // Create location tests

        @Test
        @Order(1)
        void testCreateLocation_AdminRole() throws Exception {
                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName(TEST_NAME);
                dto.setAddress(TEST_ADDRESS);
                dto.setCompanyId(COMPANY_ID);

                when(locationService.createLocation(dto.getName(), dto.getAddress(), dto.getCompanyId()))
                                .thenReturn(baseLocation);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(jsonBody))
                                .andDo(print())
                                .andExpect(status().isCreated());
        }

        @Test
        @Order(2)
        void testCreateLocation_OwnerRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);

                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName(TEST_NAME);
                dto.setAddress(TEST_ADDRESS);

                when(locationService.createLocation(dto.getName(), dto.getAddress(), dto.getCompanyId()))
                                .thenReturn(baseLocation);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(jsonBody))
                                .andDo(print())
                                .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        void testCreateLocation_FleetManagerRole() throws Exception {
                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName(TEST_NAME);
                dto.setAddress(TEST_ADDRESS);
                dto.setCompanyId(COMPANY_ID);

                when(locationService.createLocation(dto.getName(), dto.getAddress(), dto.getCompanyId()))
                                .thenReturn(baseLocation);
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(false);
                when(userService.userHasAnyRole(FLEETMANAGER)).thenReturn(true);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER)
                                .content(jsonBody))
                                .andDo(print())
                                .andExpect(status().isForbidden());
        }

        // Get all locations tests

        @Test
        @Order(4)
        void testGetAllLocations_AdminRole() throws Exception {
                PageImpl<Location> mockPage = new PageImpl<>(List.of(baseLocation));
                when(locationRepository.findAll(PageRequest.of(0, 5))).thenReturn(mockPage);
                when(userService.userHasAnyRole(ADMIN)).thenReturn(true);
                mvc.perform(get("/api/locations?pageNumber=1&pageSize=20")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].name").value(TEST_NAME))
                                .andExpect(jsonPath("$.content[0].address").value(TEST_ADDRESS))
                                .andExpect(jsonPath("$.content[0].companyId").value(COMPANY_ID));
        }

        @Test
        @Order(5)
        void testGetAllLocations_OwnerRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN)).thenReturn(false);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);

                PageImpl<Location> page = new PageImpl<>(List.of(baseLocation));
                when(locationRepository.findAllByCompanyId(COMPANY_ID, PageRequest.of(0, 20))).thenReturn(page);

                mvc.perform(get("/api/locations?pageNumber=1&pageSize=20")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].companyId").value(COMPANY_ID));
        }

        @Test
        @Order(6)
        void testGetAllLocations_FleetmanagerRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(false);
                when(userService.userHasAnyRole(FLEETMANAGER)).thenReturn(true);

                PageImpl<Location> page = new PageImpl<>(List.of(baseLocation));
                when(locationRepository.findAllByFleetmanagerId(FLEETMANAGER_ID, PageRequest.of(0, 20)))
                                .thenReturn(page);

                mvc.perform(get("/api/locations?pageNumber=1&pageSize=20")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].fleetmanagerId").value(FLEETMANAGER_ID));
        }

        @Test
        @Order(7)
        void testGetAllLocations_UnauthorizedRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                mvc.perform(get("/api/locations?pageNumber=1&pageSize=20")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }

        // Get location by ID tests

        @Test
        @Order(8)
        void testGetLocationById_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);

                mvc.perform(get("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(LOCATION_ID))
                                .andExpect(jsonPath("$.name").value(TEST_NAME));
        }

        @Test
        @Order(9)
        void testGetLocationById_OwnerWithAccess() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(locationRepository.existsByIdAndCompanyId(LOCATION_ID, COMPANY_ID)).thenReturn(true);

                mvc.perform(get("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(LOCATION_ID))
                                .andExpect(jsonPath("$.companyId").value(COMPANY_ID));
        }

        @Test
        @Order(10)
        void testGetLocationById_OwnerWithWrongCompany() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);

                when(locationRepository.existsByIdAndCompanyId(LOCATION_ID, COMPANY_ID)).thenReturn(false);

                mvc.perform(get("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(11)
        void testGetLocationById_NotFound() throws Exception {
                when(userService.userHasAnyRole(ADMIN)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);
                when(locationRepository.findById("not-existent-id")).thenReturn(Optional.empty());

                mvc.perform(get("/api/locations/not-existent-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isNotFound());
        }

        // Update location tests

        @Test
        @Order(12)
        void testUpdateLocation_AdminRole() throws Exception {
                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName(UPDATED_TEST_NAME);
                dto.setAddress(UPDATED_TEST_ADDRESS);
                dto.setCompanyId(COMPANY_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);
                when(locationService.updateLocation(LOCATION_ID, dto)).thenReturn(baseLocation);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(jsonBody))
                                .andExpect(status().isOk());
        }

        @Test
        @Order(13)
        void testUpdateLocation_OwnerWithAccess() throws Exception {
                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName(UPDATED_TEST_NAME);
                dto.setAddress(UPDATED_TEST_ADDRESS);

                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(locationRepository.existsByIdAndCompanyId(LOCATION_ID, COMPANY_ID)).thenReturn(true);
                when(locationService.updateLocation(LOCATION_ID, dto)).thenReturn(baseLocation);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(jsonBody))
                                .andExpect(status().isOk());
        }

        @Test
        @Order(14)
        void testUpdateLocation_OwnerWithoutAccess() throws Exception {
                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName("No Access");

                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(locationRepository.existsByIdAndCompanyId(LOCATION_ID, COMPANY_ID)).thenReturn(false);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(jsonBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(15)
        void testUpdateLocation_UnauthorizedRole() throws Exception {
                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName("Blocked");
                dto.setAddress(UPDATED_TEST_ADDRESS);
                dto.setCompanyId(COMPANY_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(false);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(16)
        void testUpdateLocation_NotFound() throws Exception {
                LocationCreateDTO dto = new LocationCreateDTO();
                dto.setName("Missing");
                dto.setAddress(UPDATED_TEST_ADDRESS);
                dto.setCompanyId(COMPANY_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);
                when(locationService.updateLocation(eq("not-found-id"), any(LocationCreateDTO.class)))
                                .thenThrow(new NoSuchElementException());

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/locations/not-found-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(jsonBody))
                                .andExpect(status().isNotFound());
        }

        // Delete location tests

        @Test
        @Order(17)
        void testDeleteLocation_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);

                mvc.perform(delete("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk());
        }

        @Test
        @Order(18)
        void testDeleteLocation_OwnerWithAccess() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(locationRepository.existsByIdAndCompanyId(LOCATION_ID, COMPANY_ID)).thenReturn(true);

                mvc.perform(delete("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isOk());
        }

        @Test
        @Order(19)
        void testDeleteLocation_OwnerWithoutAccess() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(locationRepository.existsByIdAndCompanyId(LOCATION_ID, COMPANY_ID)).thenReturn(false);

                mvc.perform(delete("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(20)
        void testDeleteLocation_UnauthorizedRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(false);

                mvc.perform(delete("/api/locations/" + LOCATION_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }

}
