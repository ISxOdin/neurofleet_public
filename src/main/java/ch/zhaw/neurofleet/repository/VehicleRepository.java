package ch.zhaw.neurofleet.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Vehicle;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    
}
