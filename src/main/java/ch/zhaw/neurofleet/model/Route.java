package ch.zhaw.neurofleet.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Document("routes")
public class Route {
    @Id
    private String id;
    @NonNull
    private String description;
    @NonNull
    private LocalDateTime scheduledTime;
    @NonNull
    private String vehicleId;
    @NonNull
    private String companyId;
    @NonNull
    private List<String> jobIds = new ArrayList<>();
    @NonNull
    private Integer totalPayloadKg = 0;
    @NonNull
    private RouteState state = RouteState.NEW;
}
