package ch.zhaw.neurofleet.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ch.zhaw.neurofleet.model.Vehicle;
import ch.zhaw.neurofleet.model.VehicleState;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
        Optional<Vehicle> findByLicensePlate(String licensePlate);

        Optional<Vehicle> findByVin(String vin);

        long countByState(VehicleState state);

        Page<Vehicle> findAllById(String vehicleId, Pageable pageable);

        Page<Vehicle> findAllByCompanyId(String companyId, Pageable pageable);

        Page<Vehicle> findAllByLocationId(String locationId, Pageable pageable);

        Page<Vehicle> findAllByCompanyIdAndLocationId(String companyId, String locationId, Pageable pageable);

        boolean existsByIdAndCompanyId(String id, String cid);

        boolean existsByIdAndLocationId(String id, String lid);
}
