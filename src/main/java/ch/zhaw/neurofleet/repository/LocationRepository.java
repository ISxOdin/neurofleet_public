package ch.zhaw.neurofleet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Location;

public interface LocationRepository extends MongoRepository<Location, String> {
    Page<Location> findAllByCompanyId(String companyId, Pageable pageable);

    boolean existsByIdAndCompanyId(String id, String cid);
}
