package ch.zhaw.neurofleet.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Route;
import ch.zhaw.neurofleet.model.RouteCreateDTO;
import ch.zhaw.neurofleet.repository.RouteRepository;
import ch.zhaw.neurofleet.service.UserService;
import static ch.zhaw.neurofleet.security.Roles.*;

@RestController
@RequestMapping("/api")

public class RouteController {

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    UserService userService;

    @PostMapping("/routes")
    public ResponseEntity<Route> createRoute(@RequestBody RouteCreateDTO rDTO) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Route routeDAO = new Route(
                    rDTO.getName(),
                    rDTO.getWaypoints(),
                    rDTO.getVehicleId(),
                    rDTO.getJobIds(),
                    rDTO.getCompanyId());
            Route Route = routeRepository.save(routeDAO);
            return new ResponseEntity<>(Route, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/routes")
    public ResponseEntity<Page<Route>> getAllRoutes(
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        Page<Route> allRoutes = routeRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        return new ResponseEntity<>(allRoutes, HttpStatus.OK);
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable String id) {
        Optional<Route> c = routeRepository.findById(id);
        if (c.isPresent()) {
            return new ResponseEntity<>(c.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<String> deleteRouteById(@PathVariable String id) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        routeRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("DELETED");
    }

}