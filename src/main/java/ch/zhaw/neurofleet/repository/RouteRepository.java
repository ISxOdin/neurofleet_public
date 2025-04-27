package ch.zhaw.neurofleet.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Route;

public interface RouteRepository extends MongoRepository<Route, String> {
    
}
