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

import ch.zhaw.neurofleet.model.Vehicle;
import ch.zhaw.neurofleet.model.VehicleCreateDTO;
import ch.zhaw.neurofleet.repository.VehicleRepository;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    UserService userService;

    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> createVehicle(
            @RequestBody VehicleCreateDTO vDTO) {
        if (!userService.userHasAnyRole("admin", "owner", "fleetmanager")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Vehicle vDAO = new Vehicle(
                vDTO.getLicensePlate(),
                vDTO.getVin(),
                vDTO.getType(),
                vDTO.getCapacity(),
                vDTO.getLocationId(),
                vDTO.getCompanyId());
        Vehicle v = vehicleRepository.save(vDAO);
        return new ResponseEntity<>(v, HttpStatus.CREATED);
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Page<Vehicle>> getVehicles(
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        Page<Vehicle> allCompanies = vehicleRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        return new ResponseEntity<>(allCompanies, HttpStatus.OK);
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable String id) {
        Optional<Vehicle> c = vehicleRepository.findById(id);
        if (c.isPresent()) {
            return new ResponseEntity<>(c.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<String> deleteVehicleById(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin", "owner", "fleetmanager")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("DELETED");
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
