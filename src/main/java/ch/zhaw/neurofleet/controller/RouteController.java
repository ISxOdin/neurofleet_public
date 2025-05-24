package ch.zhaw.neurofleet.controller;

import static ch.zhaw.neurofleet.security.Roles.ADMIN;
import static ch.zhaw.neurofleet.security.Roles.FLEETMANAGER;
import static ch.zhaw.neurofleet.security.Roles.OWNER;

import java.util.NoSuchElementException;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.zhaw.neurofleet.model.Route;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.repository.LocationRepository;
import ch.zhaw.neurofleet.repository.RouteRepository;
import ch.zhaw.neurofleet.service.RouteService;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api")
public class RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteService routeService;

    @Autowired
    private UserService userService;

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private LocationRepository locationRepository;

    @PostMapping("/routes")
    public ResponseEntity<Route> createRoute(@RequestBody Route route) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (userService.userHasAnyRole(OWNER, FLEETMANAGER)) {
            route.setCompanyId(userService.getCompanyIdOfCurrentUser());
        }

        try {
            var jobs = jobRepository.findAllById(route.getJobIds());
            var originLocation = locationRepository.findById(jobs.get(0).getOriginId())
                    .orElseThrow(() -> new NoSuchElementException("Origin location not found"));
            var destinationLocation = locationRepository.findById(jobs.get(0).getDestinationId())
                    .orElseThrow(() -> new NoSuchElementException("Destination location not found"));
            var generatedDescription = chatModel.call(new Prompt(
                    "The description is: '" + route.getDescription()
                            + "'. If necessary, improve the description based on the following information: "
                            + route.getScheduledTime() + " "
                            + originLocation.getName() + " " + destinationLocation.getName()
                            + jobs.size() + " " + route.getTotalPayloadKg()
                            + ". Improve the current description, if necessary. Return only the improved description. Use following format: ScheduledTime in format DD/MM/YYYY HH:MM: Region in which the route is located, JobSize jobs, Payload in kg"));
            var description = generatedDescription.getResult().getOutput().getText();
            route.setDescription(description);
            Route created = routeService.createRoute(route);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/routes")
    public ResponseEntity<Page<Route>> getAllRoutes(
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (userService.userHasAnyRole(OWNER, FLEETMANAGER)) {
            String companyId = userService.getCompanyIdOfCurrentUser();
            if (companyId == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            Page<Route> companyRoutes = routeRepository.findAllByCompanyId(companyId,
                    PageRequest.of(pageNumber - 1, pageSize));
            return new ResponseEntity<>(companyRoutes, HttpStatus.OK);
        }

        Page<Route> routes = routeRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        return new ResponseEntity<>(routes, HttpStatus.OK);
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable String id) {
        return routeRepository.findById(id)
                .map(route -> new ResponseEntity<>(route, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/routes/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable String id, @RequestBody Route route) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Route updated = routeService.updateRoute(id, route);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<String> deleteRoute(@PathVariable String id) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            routeService.deleteRoute(id);
            return ResponseEntity.ok("DELETED");
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}