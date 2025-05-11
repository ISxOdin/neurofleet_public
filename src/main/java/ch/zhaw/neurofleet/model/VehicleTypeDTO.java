package ch.zhaw.neurofleet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VehicleTypeDTO {
    private String name;
    private String label;
    private int capacityKg;
    private int liftCapacityKg;
    private int palletCount;
}
