package ch.zhaw.neurofleet.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Company;

public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findByUserIdsContaining(String auth0Id);
}
