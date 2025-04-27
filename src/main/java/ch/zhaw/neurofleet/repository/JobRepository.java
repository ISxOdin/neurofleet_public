package ch.zhaw.neurofleet.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Job;

public interface JobRepository extends MongoRepository<Job, String> {
    
    
}
