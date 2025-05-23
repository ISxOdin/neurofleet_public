package ch.zhaw.neurofleet.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RouteCreateDTO {
    private String description;
    private LocalDateTime scheduledTime;
    private String vehicleId;
    private String companyId;
    private List<String> jobIds;
    private Integer totalPayloadKg;
    private RouteState state;
}
