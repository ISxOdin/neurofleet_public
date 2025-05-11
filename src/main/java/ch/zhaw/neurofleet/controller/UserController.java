package ch.zhaw.neurofleet.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Auth0UserDTO;
import ch.zhaw.neurofleet.model.User;
import ch.zhaw.neurofleet.model.UserDTO;
import ch.zhaw.neurofleet.repository.UserRepository;
import ch.zhaw.neurofleet.service.Auth0Service;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    Auth0Service auth0Service;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/users/sync")
    public ResponseEntity<List<User>> syncUsersWithAuth0() {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Auth0UserDTO> auth0Users = auth0Service.getAllUsers();
            List<User> syncedUsers = new ArrayList<>();

            for (Auth0UserDTO authUser : auth0Users) {
                Optional<User> existing = userRepository.findByAuth0Id(authUser.getUser_id());

                if (existing.isEmpty()) {
                    User newUser = new User(
                            authUser.getUser_id());

                    syncedUsers.add(userRepository.save(newUser));
                } else {
                    syncedUsers.add(existing.get());
                }
            }

            return ResponseEntity.ok(syncedUsers);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/users/{auth0Id}")
    public ResponseEntity<User> updateUser(
            @PathVariable String auth0Id,
            @RequestBody UserDTO dto) {

        if (!userService.userHasAnyRole("admin", "owner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            User updated = userService.updateUser(auth0Id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
