package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VehicleType {

    LIEFERWAGEN("Lieferwagen 3,5 t", 1000, 500, 7),
    LIEFERWAGEN_ANHÄNGER("Lieferwagen 3,5 t mit Anhänger", 2850, 500, 14),
    LASTWAGEN("Lastwagen 15–18 t", 8600, 1500, 21),
    CITYLINER("City-Liner 28 t", 15000, 2000, 26),
    ANHÄNGERZUG("Anhängerzug 36 t", 22000, 2000, 36),
    SATTELSCHLEPPER("Sattelschlepper 36–40 t", 24000, 2000, 33);
 
    private final String label;
    private final int capacityKg;
    private final int liftCapacityKg;
    private final int palletCount;
}
