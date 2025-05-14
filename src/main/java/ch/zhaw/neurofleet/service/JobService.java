package ch.zhaw.neurofleet.service;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.JobCreateDTO;
import ch.zhaw.neurofleet.repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserService userService;

    public Job updateJob(String id, JobCreateDTO dto) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));

        if (userService.userHasAnyRole("owner")) {
            String userCompanyId = userService.getCompanyIdOfCurrentUser();
            if (!job.getCompanyId().equals(userCompanyId)) {
                throw new SecurityException("Job does not belong to user's company");
            }
            dto.setCompanyId(userCompanyId);
        }

        if (userService.userHasAnyRole("fleetmanager")) {
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
        job.setVehicleId(dto.getVehicleId());
        job.setCompanyId(dto.getCompanyId());
        job.setJobState(dto.getJobState());
        if (dto.getJobState() == null) {
            throw new IllegalArgumentException("Jobstate must not be null");
        }

        return jobRepository.save(job);
    }

}
