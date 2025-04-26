package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class VehicleCreateDTO {
    private String licensePlate;
    private String vin;
    private String type;
    private int capacity;    
    private String locationId;
    private String companyId;
}
