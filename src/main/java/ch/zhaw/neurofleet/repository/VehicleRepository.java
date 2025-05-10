package ch.zhaw.neurofleet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.model.Vehicle;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
        Page<Location> findAllById(String vehicleId, Pageable pageable);
        boolean existsByIdAndCompanyId(String id, String cid);
        boolean existsByIdAndLocationId(String id, String lid);
}
