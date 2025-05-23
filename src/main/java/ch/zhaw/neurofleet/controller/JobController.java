package ch.zhaw.neurofleet.controller;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.JobCreateDTO;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.service.JobService;
import ch.zhaw.neurofleet.service.UserService;
import static ch.zhaw.neurofleet.security.Roles.*;

@RestController
@RequestMapping("/api")
public class JobController {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    UserService userService;

    @Autowired
    JobService jobService;

    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(@RequestBody JobCreateDTO jDTO) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (userService.userHasAnyRole(OWNER)) {
            jDTO.setCompanyId(userService.getCompanyIdOfCurrentUser());
        }

        if (userService.userHasAnyRole(FLEETMANAGER)) {
            String locationId = userService.getLocationIdOfCurrentUser();
            if (locationId == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            // Ensure origin matches fleet manager's location
            if (!locationId.equals(jDTO.getOriginId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }
            jDTO.setCompanyId(userService.getCompanyIdOfCurrentUser());
        }

        try {
            Job jobDAO = new Job(
                    jDTO.getDescription(),
                    jDTO.getScheduledTime(),
                    jDTO.getOriginId(),
                    jDTO.getDestinationId(),
                    jDTO.getCompanyId(),
                    jDTO.getPayloadKg());
            Job job = jobRepository.save(jobDAO);
            return new ResponseEntity<>(job, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<Job>> getAllJobs(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize) {

        PageRequest pr = PageRequest.of(pageNumber - 1, pageSize);
        Page<Job> page;
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Owners see only their company's jobs
        if (userService.userHasAnyRole(OWNER)) {
            String companyId = userService.getCompanyIdOfCurrentUser();
            page = jobRepository.findAllByCompanyId(companyId,
                    pr);
            return new ResponseEntity<>(page, HttpStatus.OK);
        }

        // Fleet managers see all jobs (could be restricted to their location in the
        // future if needed)
        page = jobRepository.findAll(pr);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable String id) {
        Optional<Job> c = jobRepository.findById(id);
        if (c.isPresent()) {
            return new ResponseEntity<>(c.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable String id, @RequestBody JobCreateDTO dto) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Job updated = jobService.updateJob(id, dto);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<String> deleteJobById(@PathVariable String id) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        jobRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("DELETED");
    }
}