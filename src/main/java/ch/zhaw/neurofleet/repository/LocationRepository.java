package ch.zhaw.neurofleet.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;

import ch.zhaw.neurofleet.model.Location;

public interface LocationRepository extends MongoRepository<Location, String> {
    Page<Location> findAllByCompanyId(String companyId, Pageable pageable);

    Page<Location> findAllByFleetmanagerId(String fleetmanagerId, Pageable pageable);

    boolean existsByIdAndCompanyId(String id, String cid);

    @Aggregation(pipeline = {
            "{ $match: { $or: [ { userIds: ?0 }, { fleetmanagerId: ?0 } ] } }",
            "{ $project: { _id: 1, name: 1, address: 1, latitude: 1, longitude: 1, companyId: 1, fleetmanagerId: 1, userIds: 1 } }"
    })
    List<Location> findLocationsByUserId(String userId);

    // Helper method to get first location for a fleet manager
    default Optional<Location> findFirstByFleetmanagerId(String fleetmanagerId) {
        return findAllByFleetmanagerId(fleetmanagerId, PageRequest.of(0, 1))
                .getContent()
                .stream()
                .findFirst();
    }
}
