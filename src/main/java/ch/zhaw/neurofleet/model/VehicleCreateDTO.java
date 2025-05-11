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
    private String locationId;
    private String companyId;
    private VehicleState state;
    private VehicleType vehicleType;
    
    public int getCapacity() {
        return vehicleType != null ? vehicleType.getCapacityKg() : 0;
    }

}
