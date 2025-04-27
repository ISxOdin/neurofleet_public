package ch.zhaw.neurofleet.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RouteCreateDTO {
    private String name;
    private List<String> waypoints;
    private String vehicleId;
    private List<String> jobIds;
    private String companyId;
}
