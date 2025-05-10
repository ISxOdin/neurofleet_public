package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class VehicleCreateDTO {
    private String licensePlate;
    private String vin;
    private String type;
    private int capacity;    
    private String locationId;
    private String companyId;
}
