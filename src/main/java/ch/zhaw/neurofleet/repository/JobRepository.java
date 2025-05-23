package ch.zhaw.neurofleet.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Job;

public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByCompanyId(String companyId);

    Page<Job> findAllByCompanyId(String companyId, Pageable pageable);
}
