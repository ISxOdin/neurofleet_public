package ch.zhaw.neurofleet.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Route;

public interface RouteRepository extends MongoRepository<Route, String> {
    List<Route> findByCompanyId(String companyId);

    List<Route> findByVehicleId(String vehicleId);

    List<Route> findByCompanyIdAndVehicleId(String companyId, String vehicleId);
}
