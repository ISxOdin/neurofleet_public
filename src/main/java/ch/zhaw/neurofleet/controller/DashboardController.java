package ch.zhaw.neurofleet.controller;

import static ch.zhaw.neurofleet.security.Roles.ADMIN;
import static ch.zhaw.neurofleet.security.Roles.FLEETMANAGER;
import static ch.zhaw.neurofleet.security.Roles.OWNER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.JobState;
import ch.zhaw.neurofleet.model.VehicleState;
import ch.zhaw.neurofleet.repository.CompanyRepository;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.repository.VehicleRepository;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getDashboardData() {
        try {
            if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
                return ResponseEntity.status(403).body("Access denied");
            }

            // Get company information
            String companyName = "";
            String location = "";

            String companyId = userService.getCompanyIdOfCurrentUser();
            if (companyId != null) {
                Optional<Company> companyOpt = companyRepository.findById(companyId);
                if (companyOpt.isPresent()) {
                    Company company = companyOpt.get();
                    companyName = company.getName();
                    // Location information would need to be fetched from a separate location
                    // repository
                    // if needed
                }
            }

            // Get job statistics
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            List<Job> allJobs = jobRepository.findAll();

            long activeVehicles = vehicleRepository.countByState(VehicleState.AVAILABLE);
            long scheduledJobs = allJobs.stream()
                    .filter(job -> job.getJobState() == JobState.SCHEDULED)
                    .count();
            long inProgressJobs = allJobs.stream()
                    .filter(job -> job.getJobState() == JobState.IN_PROGRESS)
                    .count();
            long completedJobsToday = allJobs.stream()
                    .filter(job -> job.getJobState() == JobState.COMPLETED &&
                            job.getScheduledTime() != null &&
                            job.getScheduledTime().isAfter(startOfDay))
                    .count();

            // Get alerts (cancelled and aborted jobs)
            List<Map<String, Object>> alerts = allJobs.stream()
                    .filter(job -> job.getJobState() == JobState.CANCELLED ||
                            job.getJobState() == JobState.ABORTED)
                    .map(job -> {
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("type", job.getJobState() == JobState.CANCELLED ? "cancelled" : "aborted");
                        alert.put("title", "Job " + job.getJobState().toString().toLowerCase());
                        alert.put("message", "Job #" + job.getId() + " was " +
                                job.getJobState().toString().toLowerCase());
                        alert.put("timestamp", job.getScheduledTime().toString());
                        return alert;
                    })
                    .collect(Collectors.toList());

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("company", companyName);
            response.put("location", location);
            response.put("activeVehicles", activeVehicles);
            response.put("scheduledJobs", scheduledJobs);
            response.put("inProgressJobs", inProgressJobs);
            response.put("completedJobsToday", completedJobsToday);
            response.put("alerts", alerts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching dashboard data: " + e.getMessage());
        }
    }
}