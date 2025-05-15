package ch.zhaw.neurofleet.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.User;
import ch.zhaw.neurofleet.model.UserDTO;
import ch.zhaw.neurofleet.repository.UserRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.Auth0Service;
import ch.zhaw.neurofleet.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Auth0Service auth0Service;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH0_ID = "auth0|12345";
    private static final String COMPANY_ID = "comp-001";
    private static final String LOCATION_ID = "loc-001";
    private static final String VEHICLE_ID = "veh-001";

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User(AUTH0_ID);
        testUser.setId("user-db-id");
        testUser.setCompanyId(COMPANY_ID);
        testUser.setLocationId(LOCATION_ID);
        testUser.setVehicleId(VEHICLE_ID);

        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(testUser));
    }

    @Test
    @Order(1)
    void testGetAllUsers_AsAdmin() throws Exception {
        when(userService.userHasAnyRole("admin", "owner")).thenReturn(true);

        mvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auth0Id").value(AUTH0_ID))
                .andExpect(jsonPath("$[0].companyId").value(COMPANY_ID))
                .andExpect(jsonPath("$[0].locationId").value(LOCATION_ID))
                .andExpect(jsonPath("$[0].vehicleId").value(VEHICLE_ID));
    }

    @Test
    @Order(2)
    void testGetAllUsers_Forbidden() throws Exception {
        when(userService.userHasAnyRole("admin", "owner")).thenReturn(false);

        mvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void testSyncUsersWithAuth0_Admin() throws Exception {
        when(userService.userHasAnyRole("admin", "owner")).thenReturn(true);
        when(auth0Service.getAllUsers()).thenReturn(List.of());

        mvc.perform(get("/api/users/sync")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testSyncUsersWithAuth0_Forbidden() throws Exception {
        when(userService.userHasAnyRole("admin", "owner")).thenReturn(false);

        mvc.perform(get("/api/users/sync")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void testUpdateUser_Admin() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setAuth0Id(AUTH0_ID);
        dto.setCompanyId(COMPANY_ID);
        dto.setLocationId(LOCATION_ID);
        dto.setVehicleId(VEHICLE_ID);

        User existingUser = new User(AUTH0_ID);
        existingUser.setId("user-db-id");
        existingUser.setAuth0Id(AUTH0_ID);
        existingUser.setCompanyId(dto.getCompanyId());
        existingUser.setLocationId(dto.getLocationId());
        existingUser.setVehicleId(dto.getVehicleId());

        when(userService.userHasAnyRole("admin", "owner")).thenReturn(true);
        when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(AUTH0_ID, dto)).thenReturn(existingUser);

        String json = objectMapper.writeValueAsString(dto);

        mvc.perform(put("/api/users/" + AUTH0_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                .content(json))
                .andDo(result -> System.out.println("RESPONSE: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk());

    }

    @Test
    @Order(6)
    void testUpdateUser_NotFound() throws Exception {
        when(userService.userHasAnyRole("admin", "owner")).thenReturn(true);

        UserDTO dto = new UserDTO();
        dto.setCompanyId(COMPANY_ID);
        when(userService.updateUser(eq("invalid-id"), any(UserDTO.class)))
                .thenThrow(new NoSuchElementException());

        mvc.perform(put("/api/users/invalid-id")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    void testUpdateUser_Forbidden() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setCompanyId(COMPANY_ID);

        when(userService.userHasAnyRole("admin", "owner")).thenReturn(false);

        mvc.perform(put("/api/users/" + AUTH0_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
