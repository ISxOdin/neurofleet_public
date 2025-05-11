package ch.zhaw.neurofleet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@Document("users")
public class User {
    @Id
    private String id;
    @NonNull
    private String auth0Id;

    private String companyId;
    private String locationId;
    private String vehicleId;
}
