package ch.zhaw.neurofleet.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.service.Auth0Service;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api/auth0")
public class Auth0Controller {

    @Autowired
    UserService userService;

    @Autowired
    Auth0Service auth0Service;

    @GetMapping("/users")
    public ResponseEntity<List<Auth0Service.Auth0UserDTO>> getAuth0Users() {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            List<Auth0Service.Auth0UserDTO> users = auth0Service.fetchAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/{id}/roles")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String decodedId = URLDecoder.decode(id, StandardCharsets.UTF_8); // manually decode
            List<String> roles = auth0Service.getUserRoles(decodedId);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
