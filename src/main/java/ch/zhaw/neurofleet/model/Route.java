package ch.zhaw.neurofleet.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@Document("routes")
public class Route {
    @Id
    private String id;
    @NonNull
    private String name;
    @NonNull
    private List<String> waypoints;
    @NonNull
    private String vehicleId;
    @NonNull
    private List<String> jobIds;
    @NonNull
    private String companyId;
    private RouteState routestate = RouteState.NEW;
}
