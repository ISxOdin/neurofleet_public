package ch.zhaw.neurofleet.service;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.neurofleet.model.Vehicle;
import ch.zhaw.neurofleet.model.VehicleCreateDTO;
import ch.zhaw.neurofleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    UserService userService;

    public Vehicle updateVehicle(String id, VehicleCreateDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found"));

        if (userService.userHasAnyRole("owner")) {
            String userCompanyId = userService.getCompanyIdOfCurrentUser();
            if (!vehicle.getCompanyId().equals(userCompanyId)) {
                throw new SecurityException("Vehicle does not belong to user's company");
            }
            dto.setCompanyId(userCompanyId);
        }

        if (userService.userHasAnyRole("fleetmanager")) {
            String userCompanyId = userService.getCompanyIdOfCurrentUser();
            String userLocationId = userService.getLocationIdOfCurrentUser();

            if (!vehicle.getCompanyId().equals(userCompanyId) ||
                    !vehicle.getLocationId().equals(userLocationId)) {
                throw new SecurityException("Vehicle not accessible for fleetmanager");
            }

            dto.setCompanyId(userCompanyId);
            dto.setLocationId(userLocationId);
        }

        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setVin(dto.getVin());
        vehicle.setVehicleType(dto.getVehicleType());
        vehicle.setCapacity(dto.getVehicleType().getCapacityKg());
        vehicle.setCompanyId(dto.getCompanyId());
        vehicle.setLocationId(dto.getLocationId());
        vehicle.setState(dto.getState());

        return vehicleRepository.save(vehicle);
    }

}
