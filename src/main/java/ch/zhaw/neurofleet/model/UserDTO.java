package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String auth0Id;
    private String companyId;
    private String locationId;
    private String vehicleId;
}
