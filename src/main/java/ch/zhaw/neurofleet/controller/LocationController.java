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

import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.model.LocationCreateDTO;
import ch.zhaw.neurofleet.repository.LocationRepository;
import ch.zhaw.neurofleet.service.LocationService;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/locations/{id}")
    public ResponseEntity<Location> createLocation(@RequestBody LocationCreateDTO dto) {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            if (userService.userHasAnyRole("owner")) {
                dto.setCompanyId(userService.getCompanyIdOfCurrentUser());
            }
            Location saved = locationService.createLocation(
                    dto.getName(),
                    dto.getAddress(),
                    dto.getCompanyId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<Page<Location>> getAllLocations(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize) {

        PageRequest pr = PageRequest.of(pageNumber - 1, pageSize);
        Page<Location> page;
        if (userService.userHasAnyRole("admin")) {
            page = locationRepository.findAll(pr);
        } else if (userService.userHasAnyRole("owner")) {
            String cid = userService.getCompanyIdOfCurrentUser();
            page = locationRepository.findAllByCompanyId(cid, pr);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(page);
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (userService.userHasAnyRole("owner")) {
            String cid = userService.getCompanyIdOfCurrentUser();
            if (!locationRepository.existsByIdAndCompanyId(id, cid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        Optional<Location> opt = locationRepository.findById(id);
        return opt.map(loc -> ResponseEntity.ok(loc))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<Location> updateLocation(
            @PathVariable String id,
            @RequestBody LocationCreateDTO dto) {

        if (!userService.userHasAnyRole("admin", "owner")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            if (userService.userHasAnyRole("owner")) {
                String cid = userService.getCompanyIdOfCurrentUser();
                if (!locationRepository.existsByIdAndCompanyId(id, cid)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                dto.setCompanyId(cid);
            }
            Location updated = locationService.updateLocation(id, dto);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a Location. Owners can delete only their own company's locations.
     */
    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin", "owner")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (userService.userHasAnyRole("owner")) {
            String cid = userService.getCompanyIdOfCurrentUser();
            if (!locationRepository.existsByIdAndCompanyId(id, cid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        locationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
