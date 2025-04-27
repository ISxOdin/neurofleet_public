package ch.zhaw.neurofleet.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Company;

public interface CompanyRepository extends MongoRepository<Company, String> {

}
