package ch.zhaw.neurofleet.service;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.JobCreateDTO;
import ch.zhaw.neurofleet.model.JobState;
import ch.zhaw.neurofleet.repository.JobRepository;
import static ch.zhaw.neurofleet.security.Roles.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    UserService userService;

    public Job updateJob(String id, JobCreateDTO dto) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));

        if (userService.userHasAnyRole(OWNER)) {
            String userCompanyId = userService.getCompanyIdOfCurrentUser();
            if (!job.getCompanyId().equals(userCompanyId)) {
                throw new SecurityException("Job does not belong to user's company");
            }
            dto.setCompanyId(userCompanyId);
        }

        if (userService.userHasAnyRole(FLEETMANAGER)) {
            String userCompanyId = userService.getCompanyIdOfCurrentUser();
            if (!job.getCompanyId().equals(userCompanyId)) {
                throw new SecurityException("Job not accessible for fleetmanager");
            }
            dto.setCompanyId(userCompanyId);
        }

        job.setDescription(dto.getDescription());
        job.setScheduledTime(dto.getScheduledTime());
        job.setOriginId(dto.getOriginId());
        job.setDestinationId(dto.getDestinationId());
        job.setPayloadKg(dto.getPayloadKg());
        job.setJobState(Objects.requireNonNull(dto.getJobState(), "Jobstate must not be null"));

        return jobRepository.save(job);
    }

    public void assignToRoute(Job job, String routeId) {
        Job freshJob = jobRepository.findById(job.getId())
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + job.getId()));
        freshJob.setRouteId(routeId);
        freshJob.setJobState(JobState.SCHEDULED);
        jobRepository.save(freshJob);
    }

    public void unassignFromRoute(Job job) {
        Job freshJob = jobRepository.findById(job.getId())
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + job.getId()));
        if (freshJob.getJobState() != JobState.COMPLETED) {
            freshJob.setRouteId(null);
            freshJob.setJobState(JobState.NEW);
            jobRepository.save(freshJob);
        }
    }

    public boolean isJobAssigned(Job job) {
        return job.getRouteId() != null;
    }

    public void setJobState(Job job, JobState state) {
        // Always get a fresh copy of the job from the database to avoid stale data
        Job freshJob = jobRepository.findById(job.getId())
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + job.getId()));

        // Only update if the state is actually changing
        if (freshJob.getJobState() != state && freshJob.getJobState() != JobState.COMPLETED) {
            freshJob.setJobState(state);
            jobRepository.save(freshJob);
        }
    }
}
