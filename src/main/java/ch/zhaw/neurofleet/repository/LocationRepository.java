package ch.zhaw.neurofleet.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Location;

public interface LocationRepository extends MongoRepository<Location, String> {
    List<Location> findAllByCompanyId(String companyId);
}
