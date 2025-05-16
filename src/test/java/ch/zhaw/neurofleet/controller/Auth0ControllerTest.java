package ch.zhaw.neurofleet.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ch.zhaw.neurofleet.model.Auth0UserDTO;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.Auth0Service;
import ch.zhaw.neurofleet.service.UserService;
import static ch.zhaw.neurofleet.security.Roles.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class Auth0ControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Auth0Service auth0Service;

    @MockitoBean
    private UserService userService;

    private static final String USER_ID = "auth0|123";
    private static final String ROLE = "owner";

    @BeforeEach
    void setup() {
        when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
    }

    @Test
    @Order(1)
    void testGetAllUsers_AsAdmin() throws Exception {
        Auth0UserDTO user = new Auth0UserDTO();
        user.setUser_id(USER_ID);
        user.setEmail("user@example.com");

        when(auth0Service.getAllUsers()).thenReturn(List.of(user));

        mvc.perform(get("/api/auth0/users")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user_id").value(USER_ID));
    }

    @Test
    @Order(2)
    void testGetUserRoles_AsOwner() throws Exception {
        when(auth0Service.getUserRoles(USER_ID)).thenReturn(List.of(ROLE));

        mvc.perform(get("/api/auth0/users/" + USER_ID + "/roles")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(ROLE));
    }

    @Test
    @Order(3)
    void testAssignUserRole_AsAdmin() throws Exception {
        doNothing().when(auth0Service).assignUserRole(USER_ID, ROLE);

        mvc.perform(post("/api/auth0/users/" + USER_ID + "/roles/" + ROLE)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string("ASSIGNED"));
    }

    @Test
    @Order(4)
    void testDeleteUserRole_AsAdmin() throws Exception {
        doNothing().when(auth0Service).deleteUserRole(USER_ID, ROLE);

        mvc.perform(delete("/api/auth0/users/" + USER_ID + "/roles/" + ROLE)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string("DELETED"));
    }

    @Test
    @Order(5)
    void testGetAllUsers_Unauthorized() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(false);

        mvc.perform(get("/api/auth0/users")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void testAssignUserRole_Forbidden() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(false);

        mvc.perform(post("/api/auth0/users/" + USER_ID + "/roles/" + ROLE)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void testDeleteUserRole_ServiceError() throws Exception {
        when(userService.userHasAnyRole(ADMIN, OWNER)).thenReturn(true);
        doThrow(new RuntimeException("something failed"))
                .when(auth0Service).deleteUserRole(USER_ID, ROLE);

        mvc.perform(delete("/api/auth0/users/" + USER_ID + "/roles/" + ROLE)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                .andExpect(status().isInternalServerError());
    }

}
