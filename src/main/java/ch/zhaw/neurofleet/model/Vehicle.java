package ch.zhaw.neurofleet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@Document("vehicles")
public class Vehicle {
    @Id
    private String id;
    @NonNull
    private String licensePlate;
    @NonNull
    private String vin;
    @NonNull
    private String type;
    @NonNull
    private int capacity;
    private VehicleState status = VehicleState.AVAILABLE;
    @NonNull
    private String locationId;
    @NonNull
    private String companyId;

}
