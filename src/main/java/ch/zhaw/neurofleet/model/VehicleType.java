package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

    VAN("Van 3.5 t", 1000, 500, 7),
    VAN_TRAILER("Van 3.5 t with Trailer", 2850, 500, 14),
    TRUCK("Truck 15–18 t", 8600, 1500, 21),
    CITY_LINER("City Liner 28 t", 15000, 2000, 26),
    TRAILER_TRUCK("Trailer Truck 36 t", 22000, 2000, 36),
    SEMI_TRAILER("Semi-Trailer Truck 36–40 t", 24000, 2000, 33);

    private final String label;
    private final int capacityKg;
    private final int liftCapacityKg;
    private final int palletCount;
}
