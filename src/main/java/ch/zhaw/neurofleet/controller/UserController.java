package ch.zhaw.neurofleet.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Auth0UserDTO;
import ch.zhaw.neurofleet.model.User;
import ch.zhaw.neurofleet.repository.UserRepository;
import ch.zhaw.neurofleet.service.Auth0Service;
import ch.zhaw.neurofleet.service.UserService;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    Auth0Service auth0Service;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/sync")
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
                    authUser.getUser_id()
                );

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


    
    
}
