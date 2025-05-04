package ch.zhaw.neurofleet.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.JobCreateDTO;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api")

public class JobController {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    UserService userService;

    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(@RequestBody JobCreateDTO jDTO) {
        if (!userService.userHasAnyRole("admin", "owner", "fleetmanager")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        

        try {
            Job jobDAO = new Job(
                    jDTO.getDescription(),
                    jDTO.getScheduledTime(),
                    jDTO.getOriginId(),
                    jDTO.getDestinationId(),
                    jDTO.getVehicleId(),
                    jDTO.getCompanyId());
            Job job = jobRepository.save(jobDAO);
            return new ResponseEntity<>(job, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<Job>> getAllJobs(
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        Page<Job> allJobs = jobRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        return new ResponseEntity<>(allJobs, HttpStatus.OK);
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

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<String> deleteJobById(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin", "owner", "fleetmanager")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        jobRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("DELETED");
    }

}