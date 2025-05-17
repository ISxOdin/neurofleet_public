package ch.zhaw.neurofleet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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

import ch.zhaw.neurofleet.model.*;
import ch.zhaw.neurofleet.repository.VehicleRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.UserService;
import ch.zhaw.neurofleet.service.VehicleService;
import static ch.zhaw.neurofleet.security.Roles.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class VehicleControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private VehicleRepository vehicleRepository;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private VehicleService vehicleService;

        private static final ObjectMapper objectMapper = new ObjectMapper();

        private static final String VEHICLE_ID = "veh-123";
        private static final String COMPANY_ID = "comp-1";
        private static final String LOCATION_ID = "loc-1";

        private Vehicle baseVehicle;

        @BeforeEach
        void setup() {
                baseVehicle = new Vehicle("ZH 123", "VIN123", LOCATION_ID, COMPANY_ID);
                baseVehicle.setId(VEHICLE_ID);
                baseVehicle.setVehicleType(VehicleType.VAN);
                baseVehicle.setCapacity(VehicleType.VAN.getCapacityKg());

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);
                when(userService.getLocationIdOfCurrentUser()).thenReturn(LOCATION_ID);

                when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(baseVehicle));
                when(vehicleRepository.existsById(VEHICLE_ID)).thenReturn(true);
                when(vehicleRepository.save(any())).thenReturn(baseVehicle);
                when(vehicleRepository.findAll(PageRequest.of(0, 5)))
                                .thenReturn(new PageImpl<>(List.of(baseVehicle)));
                when(vehicleRepository.findAllByCompanyId(eq(COMPANY_ID), any()))
                                .thenReturn(new PageImpl<>(List.of(baseVehicle)));
                when(vehicleRepository.findAllByCompanyIdAndLocationId(eq(COMPANY_ID), eq(LOCATION_ID), any()))
                                .thenReturn(new PageImpl<>(List.of(baseVehicle)));
        }

        @Test
        @Order(1)
        void testCreateVehicle_AdminRole() throws Exception {
                VehicleCreateDTO dto = new VehicleCreateDTO();
                dto.setLicensePlate("ZH 123");
                dto.setVin("VIN123");
                dto.setCompanyId(COMPANY_ID);
                dto.setLocationId(LOCATION_ID);
                dto.setVehicleType(VehicleType.VAN);

                Vehicle created = new Vehicle("ZH 123", "VIN123", LOCATION_ID, COMPANY_ID);
                created.setId(VEHICLE_ID);
                created.setVehicleType(VehicleType.VAN);
                created.setCapacity(VehicleType.VAN.getCapacityKg());

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(vehicleRepository.save(any())).thenReturn(created);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(jsonBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.licensePlate").value("ZH 123"))
                                .andExpect(jsonPath("$.vin").value("VIN123"))
                                .andExpect(jsonPath("$.vehicleType").value("VAN"));
        }

        @Test
        @Order(2)
        void testGetVehicles_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN)).thenReturn(true);
                when(vehicleRepository.findAll(PageRequest.of(0, 5)))
                                .thenReturn(new PageImpl<>(List.of(baseVehicle)));

                mvc.perform(get("/api/vehicles?pageNumber=1&pageSize=5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(VEHICLE_ID))
                                .andExpect(jsonPath("$.content[0].companyId").value(COMPANY_ID))
                                .andExpect(jsonPath("$.content[0].vehicleType").value("VAN"));
        }

        @Test
        @Order(3)
        void testCreateVehicle_OwnerRole() throws Exception {
                VehicleCreateDTO dto = new VehicleCreateDTO();
                dto.setLicensePlate("ZH 456");
                dto.setVin("VIN456");
                dto.setCompanyId("ignored-company"); // wird vom Controller überschrieben
                dto.setLocationId(LOCATION_ID);
                dto.setVehicleType(VehicleType.VAN);

                Vehicle created = new Vehicle("ZH 456", "VIN456", LOCATION_ID, COMPANY_ID);
                created.setId(VEHICLE_ID);
                created.setVehicleType(VehicleType.VAN);
                created.setCapacity(VehicleType.VAN.getCapacityKg());

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);
                when(vehicleRepository.save(any())).thenReturn(created);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(jsonBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.companyId").value(COMPANY_ID))
                                .andExpect(jsonPath("$.companyId").value(COMPANY_ID));
        }

        @Test
        @Order(4)
        void testCreateVehicle_FleetmanagerRole() throws Exception {
                VehicleCreateDTO dto = new VehicleCreateDTO();
                dto.setLicensePlate("ZH 789");
                dto.setVin("VIN789");
                dto.setCompanyId("ignored-company");
                dto.setLocationId("ignored-location");
                dto.setVehicleType(VehicleType.VAN);

                Vehicle created = new Vehicle("ZH 789", "VIN789", LOCATION_ID, COMPANY_ID);
                created.setId(VEHICLE_ID);
                created.setVehicleType(VehicleType.VAN);
                created.setCapacity(VehicleType.VAN.getCapacityKg());

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(userService.userHasAnyRole(FLEETMANAGER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);
                when(userService.getLocationIdOfCurrentUser()).thenReturn(LOCATION_ID);
                when(vehicleRepository.save(any())).thenReturn(created);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER)
                                .content(jsonBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.companyId").value(COMPANY_ID))
                                .andExpect(jsonPath("$.locationId").value(LOCATION_ID));
        }

        @Test
        @Order(5)
        void testGetVehicles_OwnerRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN)).thenReturn(false);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);

                mvc.perform(get("/api/vehicles?pageNumber=1&pageSize=5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].companyId").value(COMPANY_ID));
        }

        @Test
        @Order(6)
        void testGetVehicles_FleetmanagerRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN)).thenReturn(false);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);
                when(userService.userHasAnyRole(FLEETMANAGER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);
                when(userService.getLocationIdOfCurrentUser()).thenReturn(LOCATION_ID);

                mvc.perform(get("/api/vehicles?pageNumber=1&pageSize=5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].companyId").value(COMPANY_ID))
                                .andExpect(jsonPath("$.content[0].locationId").value(LOCATION_ID));
        }

        @Test
        @Order(7)
        void testGetVehicles_UnauthorizedRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN)).thenReturn(false);
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);
                when(userService.userHasAnyRole(FLEETMANAGER)).thenReturn(false);

                mvc.perform(get("/api/vehicles?pageNumber=1&pageSize=5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(8)
        void testGetVehicleById_AdminRole() throws Exception {
                when(userService.userHasAnyRole(OWNER)).thenReturn(false);
                when(userService.userHasAnyRole(FLEETMANAGER)).thenReturn(false);

                mvc.perform(get("/api/vehicles/" + VEHICLE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(VEHICLE_ID));
        }

        @Test
        @Order(9)
        void testGetVehicleById_OwnerWithAccess() throws Exception {
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);

                mvc.perform(get("/api/vehicles/" + VEHICLE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.companyId").value(COMPANY_ID));
        }

        @Test
        @Order(10)
        void testGetVehicleById_OwnerWithoutAccess() throws Exception {
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn("other-company");

                mvc.perform(get("/api/vehicles/" + VEHICLE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(11)
        void testGetVehicleById_FleetmanagerWithoutAccess() throws Exception {
                when(userService.userHasAnyRole(FLEETMANAGER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn("wrong-company");
                when(userService.getLocationIdOfCurrentUser()).thenReturn("wrong-location");

                mvc.perform(get("/api/vehicles/" + VEHICLE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(12)
        void testGetVehicleById_NotFound() throws Exception {
                when(vehicleRepository.findById("invalid-id")).thenReturn(Optional.empty());

                mvc.perform(get("/api/vehicles/invalid-id")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(13)
        void testUpdateVehicle_AdminRole() throws Exception {
                VehicleCreateDTO dto = new VehicleCreateDTO();
                dto.setLicensePlate("ZH 987");
                dto.setVin("VIN987");
                dto.setCompanyId(COMPANY_ID);
                dto.setLocationId(LOCATION_ID);
                dto.setVehicleType(VehicleType.VAN);

                Vehicle updated = new Vehicle(dto.getLicensePlate(), dto.getVin(), dto.getLocationId(),
                                dto.getCompanyId());
                updated.setVehicleType(dto.getVehicleType());
                updated.setCapacity(dto.getVehicleType().getCapacityKg());
                updated.setId(VEHICLE_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(vehicleService.updateVehicle(VEHICLE_ID, dto)).thenReturn(updated);

                String jsonBody = objectMapper.writeValueAsString(dto);

                String json = mvc.perform(put("/api/vehicles/" + VEHICLE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(jsonBody))
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                System.out.println("Response JSON: " + json);
        }

        @Test
        @Order(14)
        void testUpdateVehicle_UnauthorizedRole() throws Exception {
                VehicleCreateDTO dto = new VehicleCreateDTO();
                dto.setLicensePlate("BLOCKED");
                dto.setVin("BLOCKED");

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/vehicles/" + VEHICLE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER)
                                .content(jsonBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(15)
        void testUpdateVehicle_NotFound() throws Exception {
                VehicleCreateDTO dto = new VehicleCreateDTO();
                dto.setLicensePlate("Missing");

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(vehicleService.updateVehicle(eq("not-found-id"), any(VehicleCreateDTO.class)))
                                .thenThrow(new NoSuchElementException());

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/vehicles/not-found-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(jsonBody))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(16)
        void testUpdateVehicle_SecurityException() throws Exception {
                VehicleCreateDTO dto = new VehicleCreateDTO();
                dto.setLicensePlate("WrongAccess");

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(vehicleService.updateVehicle(eq(VEHICLE_ID), any(VehicleCreateDTO.class)))
                                .thenThrow(new SecurityException());

                String jsonBody = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/vehicles/" + VEHICLE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(jsonBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(17)
        void testDeleteVehicle_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(vehicleRepository.existsById(VEHICLE_ID)).thenReturn(true);

                mvc.perform(delete("/api/vehicles/" + VEHICLE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(content().string("DELETED"));
        }

        @Test
        @Order(18)
        void testDeleteVehicle_NotFound() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(vehicleRepository.existsById("invalid-id")).thenReturn(false);

                mvc.perform(delete("/api/vehicles/invalid-id")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(19)
        void testDeleteVehicle_UnauthorizedRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                mvc.perform(delete("/api/vehicles/" + VEHICLE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(20)
        void testGetVehicleTypes_AlwaysAllowed() throws Exception {
                mvc.perform(get("/api/vehicles/types")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(VehicleType.values().length))
                                .andExpect(jsonPath("$[0].label").isNotEmpty());
        }

}
