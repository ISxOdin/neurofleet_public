package ch.zhaw.neurofleet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import ch.zhaw.neurofleet.model.Company;

public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findByUserIdsContaining(String userId);

    // Case-insensitive exact match
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Optional<Company> findByName(String name);

    @Aggregation(pipeline = {
            "{ $match: { userIds: ?0 } }",
            "{ $project: { _id: 1, name: 1, email: 1, address: 1, latitude: 1, longitude: 1, owner: 1, userIds: 1 } }"
    })
    List<Company> findCompaniesByUserId(String userId);
}
