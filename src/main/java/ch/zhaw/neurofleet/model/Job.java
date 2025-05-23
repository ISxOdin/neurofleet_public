package ch.zhaw.neurofleet.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@Document("jobs")
public class Job {
    @Id
    private String id;
    @NonNull
    private String description;
    @NonNull
    private LocalDateTime scheduledTime;
    @NonNull
    private String originId;
    @NonNull
    private String destinationId;
    @NonNull
    private String companyId;
    @NonNull
    private Integer payloadKg;
    private JobState jobState = JobState.NEW;
    private String routeId;
}
