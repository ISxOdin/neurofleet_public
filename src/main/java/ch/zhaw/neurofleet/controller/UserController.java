package ch.zhaw.neurofleet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.AppMetadataDTO;
import ch.zhaw.neurofleet.model.EnrichedUserDTO;
import ch.zhaw.neurofleet.service.Auth0Service;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api/auth0")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    Auth0Service auth0Service;

    @GetMapping("/users")
    public ResponseEntity<List<EnrichedUserDTO>> getAuth0Users() {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            List<EnrichedUserDTO> users = auth0Service.fetchAllEnrichedUsers();
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
            List<String> roles = auth0Service.getUserRoles(id);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/users/{id}/roles/{roleName}")
    public ResponseEntity<String> assignUserRole(
            @PathVariable String id,
            @PathVariable String roleName) {

        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            auth0Service.assignUserRole(id, roleName);
            return ResponseEntity.status(HttpStatus.OK).body("ASSIGNED");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/users/{id}/roles/{roleName}")
    public ResponseEntity<String> deleteUserRole(
            @PathVariable String id,
            @PathVariable String roleName) {

        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            auth0Service.deleteUserRole(id, roleName);
            return ResponseEntity.status(HttpStatus.OK).body("DELETED");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/{id}/metadata")
    public ResponseEntity<AppMetadataDTO> getUserMetadata(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            AppMetadataDTO metadata = auth0Service.getUserAppMetadata(id);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PatchMapping("/users/{id}/metadata")
    public ResponseEntity<String> updateUserMetadata(
            @PathVariable String id,
            @RequestBody AppMetadataDTO dto) {

        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            auth0Service.updateUserAppMetadata(id, dto);
            return ResponseEntity.ok("UPDATED");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
