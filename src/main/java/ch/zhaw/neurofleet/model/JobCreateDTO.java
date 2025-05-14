package ch.zhaw.neurofleet.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JobCreateDTO {
    private String description;
    private LocalDateTime scheduledTime;
    private String originId;
    private String destinationId;
    private String vehicleId;
    private String companyId;
    private JobState jobState;
}
