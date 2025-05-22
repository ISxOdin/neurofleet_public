package ch.zhaw.neurofleet.controller;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.zhaw.neurofleet.model.Vehicle;
import ch.zhaw.neurofleet.model.VehicleCreateDTO;
import ch.zhaw.neurofleet.model.VehicleType;
import ch.zhaw.neurofleet.model.VehicleTypeDTO;
import ch.zhaw.neurofleet.repository.VehicleRepository;
import ch.zhaw.neurofleet.service.UserService;
import ch.zhaw.neurofleet.service.VehicleService;
import static ch.zhaw.neurofleet.security.Roles.*;

@RestController
@RequestMapping("/api")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody VehicleCreateDTO vDTO) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (vDTO.getVehicleType() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (userService.userHasAnyRole(OWNER)) {
            vDTO.setCompanyId(userService.getCompanyIdOfCurrentUser());
        }

        if (userService.userHasAnyRole(FLEETMANAGER)) {
            vDTO.setCompanyId(userService.getCompanyIdOfCurrentUser());
            vDTO.setLocationId(userService.getLocationIdOfCurrentUser());
        }

        Vehicle vDAO = new Vehicle(
                vDTO.getLicensePlate(),
                vDTO.getVin(),
                vDTO.getLocationId(),
                vDTO.getCompanyId());
        vDAO.setVehicleType(vDTO.getVehicleType());
        vDAO.setCapacity(vDTO.getVehicleType().getCapacityKg());

        Vehicle v = vehicleRepository.save(vDAO);
        return ResponseEntity.status(HttpStatus.CREATED).body(v);
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Page<Vehicle>> getVehicles(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageRequest pr = PageRequest.of(pageNumber - 1, pageSize);
        Page<Vehicle> page;

        if (userService.userHasAnyRole(ADMIN)) {
            page = vehicleRepository.findAll(pr);
        } else if (userService.userHasAnyRole(OWNER)) {
            String cid = userService.getCompanyIdOfCurrentUser();
            page = vehicleRepository.findAllByCompanyId(cid, pr);
        } else if (userService.userHasAnyRole(FLEETMANAGER)) {
            String cid = userService.getCompanyIdOfCurrentUser();
            String lid = userService.getLocationIdOfCurrentUser();
            page = vehicleRepository.findAllByCompanyIdAndLocationId(cid, lid, pr);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(page);
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable String id) {
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();

        Vehicle v = opt.get();

        if (userService.userHasAnyRole(OWNER) &&
                !v.getCompanyId().equals(userService.getCompanyIdOfCurrentUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (userService.userHasAnyRole(FLEETMANAGER) &&
                (!v.getCompanyId().equals(userService.getCompanyIdOfCurrentUser()) ||
                        !v.getLocationId().equals(userService.getLocationIdOfCurrentUser()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(v);
    }

    @PutMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable String id, @RequestBody VehicleCreateDTO dto) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Vehicle updated = vehicleService.updateVehicle(id, dto);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<String> deleteVehicleById(@PathVariable String id) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return ResponseEntity.ok("DELETED");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/vehicles/types")
    public ResponseEntity<List<VehicleTypeDTO>> getVehicleTypes() {
        List<VehicleTypeDTO> list = Arrays.stream(VehicleType.values())
                .map(vt -> new VehicleTypeDTO(
                        vt.name(),
                        vt.getLabel(),
                        vt.getCapacityKg(),
                        vt.getLiftCapacityKg(),
                        vt.getPalletCount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
