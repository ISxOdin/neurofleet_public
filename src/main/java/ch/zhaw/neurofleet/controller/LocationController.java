package ch.zhaw.neurofleet.controller;

import java.util.NoSuchElementException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.CompanyCreateDTO;
import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.model.LocationCreateDTO;
import ch.zhaw.neurofleet.repository.LocationRepository;
import ch.zhaw.neurofleet.service.LocationService;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api")

public class LocationController {

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    LocationService locationService;

    @Autowired
    UserService userService;

    @PostMapping("/locations")
    public ResponseEntity<Location> createLocation(@RequestBody LocationCreateDTO lDTO) {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String companyId = lDTO.getCompanyId();
            if (userService.userHasAnyRole("owner")) {
                companyId = userService.getCompanyIdOfCurrentUser(); // sichere Methode vorausgesetzt
            }
            Location locationDAO = locationService.createLocation(
                    lDTO.getName(),
                    lDTO.getAddress(),
                    companyId);

            Location location = locationRepository.save(locationDAO);
            return new ResponseEntity<>(location, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<Page<Location>> getAllLocations(
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize) {

        Page<Location> locations;
        if (userService.userHasAnyRole("admin")) {
            locations = locationRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        } else if (userService.userHasAnyRole("owner")) {
            String companyId = userService.getCompanyIdOfCurrentUser();
            locations = locationRepository.findAllByCompanyId(companyId, PageRequest.of(pageNumber - 1, pageSize));
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable String id) {
        Optional<Location> c = locationRepository.findById(id);
        if (c.isPresent()) {
            return new ResponseEntity<>(c.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<Location> updateLocation(
            @PathVariable String id,
            @RequestBody LocationCreateDTO dto) {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Location updated = locationService.updateLocation(id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<String> deleteLocationById(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        locationRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("DELETED");
    }

}