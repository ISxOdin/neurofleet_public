package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class LocationCreateDTO {
    private String name;
    private String address;
    private String companyId;
    private String fleetmanagerId;
}
