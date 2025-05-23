package ch.zhaw.neurofleet.controller;

import static ch.zhaw.neurofleet.security.Roles.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.Route;
import ch.zhaw.neurofleet.repository.RouteRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.RouteService;
import ch.zhaw.neurofleet.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class RouteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RouteRepository routeRepository;

    @MockitoBean
    private RouteService routeService;

    @MockitoBean
    private UserService userService;

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final String ROUTE_ID = "route-123";
    private static final String COMPANY_ID = "comp-1";
    private static final String VEHICLE_ID = "veh-1";
    private static final List<String> JOB_IDS = List.of("job-1", "job-2");

    private Route baseRoute;

    @BeforeEach
    void setup() {
        baseRoute = new Route();
        baseRoute.setId(ROUTE_ID);
        baseRoute.setDescription("Test Route");
        baseRoute.setScheduledTime(LocalDateTime.now());
        baseRoute.setVehicleId(VEHICLE_ID);
        baseRoute.setCompanyId(COMPANY_ID);
        baseRoute.setJobIds(new ArrayList<>(JOB_IDS));
        baseRoute.setTotalPayloadKg(200);

        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(baseRoute));
        when(routeRepository.findAll(PageRequest.of(0, 5))).thenReturn(new PageImpl<>(List.of(baseRoute)));
    }

    @Test
    void testCreateRoute_AdminRole() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        when(routeService.createRoute(any())).thenReturn(baseRoute);

        String json = objectMapper.writeValueAsString(baseRoute);

        mvc.perform(post("/api/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TestSecurityConfig.ADMIN)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ROUTE_ID))
                .andExpect(jsonPath("$.vehicleId").value(VEHICLE_ID));
    }

    @Test
    void testCreateRoute_UnauthorizedRole() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

        String json = objectMapper.writeValueAsString(baseRoute);

        mvc.perform(post("/api/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TestSecurityConfig.USER)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateRoute_ExceedsCapacity() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        when(routeService.createRoute(any())).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total payload exceeds vehicle capacity"));

        String json = objectMapper.writeValueAsString(baseRoute);

        mvc.perform(post("/api/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TestSecurityConfig.ADMIN)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllRoutes() throws Exception {
        mvc.perform(get("/api/routes")
                .header("Authorization", TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ROUTE_ID));
    }

    @Test
    void testGetRouteById_Found() throws Exception {
        mvc.perform(get("/api/routes/" + ROUTE_ID)
                .header("Authorization", TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROUTE_ID));
    }

    @Test
    void testGetRouteById_NotFound() throws Exception {
        String nonExistentId = "non-existent";
        when(routeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        mvc.perform(get("/api/routes/" + nonExistentId)
                .header("Authorization", TestSecurityConfig.ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateRoute_Success() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        when(routeService.updateRoute(eq(ROUTE_ID), any())).thenReturn(baseRoute);

        String json = objectMapper.writeValueAsString(baseRoute);

        mvc.perform(put("/api/routes/" + ROUTE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TestSecurityConfig.ADMIN)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROUTE_ID));
    }

    @Test
    void testUpdateRoute_Unauthorized() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

        String json = objectMapper.writeValueAsString(baseRoute);

        mvc.perform(put("/api/routes/" + ROUTE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TestSecurityConfig.USER)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateRoute_NotFound() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        when(routeService.updateRoute(eq(ROUTE_ID), any()))
                .thenThrow(new NoSuchElementException("Route not found"));

        String json = objectMapper.writeValueAsString(baseRoute);

        mvc.perform(put("/api/routes/" + ROUTE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TestSecurityConfig.ADMIN)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateRoute_WrongCompany() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        when(routeService.updateRoute(eq(ROUTE_ID), any()))
                .thenThrow(new SecurityException("Route does not belong to user's company"));

        String json = objectMapper.writeValueAsString(baseRoute);

        mvc.perform(put("/api/routes/" + ROUTE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TestSecurityConfig.OWNER)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteRoute_Success() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);

        mvc.perform(delete("/api/routes/" + ROUTE_ID)
                .header("Authorization", TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string("DELETED"));
    }

    @Test
    void testDeleteRoute_Unauthorized() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

        mvc.perform(delete("/api/routes/" + ROUTE_ID)
                .header("Authorization", TestSecurityConfig.USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteRoute_NotFound() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        doThrow(new NoSuchElementException("Route not found"))
                .when(routeService).deleteRoute(ROUTE_ID);

        mvc.perform(delete("/api/routes/" + ROUTE_ID)
                .header("Authorization", TestSecurityConfig.ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRoute_WrongCompany() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        doThrow(new SecurityException("Route does not belong to user's company"))
                .when(routeService).deleteRoute(ROUTE_ID);

        mvc.perform(delete("/api/routes/" + ROUTE_ID)
                .header("Authorization", TestSecurityConfig.OWNER))
                .andExpect(status().isForbidden());
    }
}
