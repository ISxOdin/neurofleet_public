package ch.zhaw.neurofleet.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RouteCreateDTO {
    private String name;
    private List<String> waypoints;
    private String vehicleId;
    private List<String> jobIds;
    private String companyId;
}
