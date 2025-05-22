package ch.zhaw.neurofleet.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;

import ch.zhaw.neurofleet.model.Company;

public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findByUserIdsContaining(String auth0Id);

    @Aggregation(pipeline = {
            "{ $match: { userIds: ?0 } }",
            "{ $project: { _id: 1, name: 1, email: 1, address: 1, latitude: 1, longitude: 1, owner: 1, userIds: 1 } }"
    })
    List<Company> findCompaniesByUserId(String userId);
}
