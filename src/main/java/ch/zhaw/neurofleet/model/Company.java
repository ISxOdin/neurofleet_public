package ch.zhaw.neurofleet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Document("companies")
public class Company {
    @Id
    private String id;
    @NonNull
    private String name;
    @NonNull
    private String email;
    @NonNull
    private String address;
    @NonNull
    private Double latitude;
    @NonNull
    private Double longitude;
    private String owner;
}
