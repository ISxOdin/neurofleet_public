package ch.zhaw.neurofleet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.Route;
import ch.zhaw.neurofleet.model.RouteCreateDTO;
import ch.zhaw.neurofleet.repository.RouteRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.UserService;
import static ch.zhaw.neurofleet.security.Roles.*;

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
    private UserService userService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ROUTE_ID = "route-001";
    private static final String COMPANY_ID = "comp-001";
    private static final String VEHICLE_ID = "veh-001";
    private static final List<String> WAYPOINTS = List.of("A", "B", "C");
    private static final List<String> JOB_IDS = List.of("job-1", "job-2");

    private Route route;

    @BeforeEach
    void setup() {
        route = new Route("Route A", WAYPOINTS, VEHICLE_ID, JOB_IDS, COMPANY_ID);
        route.setId(ROUTE_ID);

        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route));
        when(routeRepository.findAll(PageRequest.of(0, 5))).thenReturn(new PageImpl<>(List.of(route)));
    }

    @Test
    @Order(1)
    void testCreateRoute_AsAdmin() throws Exception {
        RouteCreateDTO dto = new RouteCreateDTO();
        dto.setName("Route A");
        dto.setWaypoints(WAYPOINTS);
        dto.setVehicleId(VEHICLE_ID);
        dto.setJobIds(JOB_IDS);
        dto.setCompanyId(COMPANY_ID);

        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
        when(routeRepository.save(any())).thenReturn(route);

        String json = objectMapper.writeValueAsString(dto);

        mvc.perform(post("/api/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Route A"));
    }

    @Test
    @Order(2)
    void testCreateRoute_Forbidden() throws Exception {
        RouteCreateDTO dto = new RouteCreateDTO();
        dto.setName("Route B");
        dto.setWaypoints(WAYPOINTS);
        dto.setVehicleId(VEHICLE_ID);
        dto.setJobIds(JOB_IDS);
        dto.setCompanyId(COMPANY_ID);

        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

        mvc.perform(post("/api/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void testGetAllRoutes() throws Exception {
        mvc.perform(get("/api/routes?pageNumber=1&pageSize=5")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ROUTE_ID))
                .andExpect(jsonPath("$.content[0].name").value("Route A"));
    }

    @Test
    @Order(4)
    void testGetRouteById_Found() throws Exception {
        mvc.perform(get("/api/routes/" + ROUTE_ID)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROUTE_ID))
                .andExpect(jsonPath("$.name").value("Route A"));
    }

    @Test
    @Order(5)
    void testGetRouteById_NotFound() throws Exception {
        when(routeRepository.findById("invalid")).thenReturn(Optional.empty());

        mvc.perform(get("/api/routes/invalid")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void testDeleteRoute_AsOwner() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);

        mvc.perform(delete("/api/routes/" + ROUTE_ID)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void testDeleteRoute_Forbidden() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

        mvc.perform(delete("/api/routes/" + ROUTE_ID)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andExpect(status().isForbidden());
    }
}
