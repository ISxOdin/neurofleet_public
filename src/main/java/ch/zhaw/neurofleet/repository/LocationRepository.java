package ch.zhaw.neurofleet.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.neurofleet.model.Location;

public interface LocationRepository extends MongoRepository<Location, String> {
    Page<Location> findAllByCompanyId(String companyId, Pageable pageable);
    Optional<Location> findByUserIdsContaining(String auth0Id);
    boolean existsByIdAndCompanyId(String id, String cid);
    Optional<Location> findByFleetmanagerId(String fleetmanagerId);
}
