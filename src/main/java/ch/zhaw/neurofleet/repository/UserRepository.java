package ch.zhaw.neurofleet.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByAuth0Id(String auth0Id);
    Optional<User> findByCompanyId(String companyId);
    Optional<User> findByLocationId(String locationId);
    
}
