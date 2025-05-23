package ch.zhaw.neurofleet.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.Route;
import ch.zhaw.neurofleet.model.Vehicle;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.repository.RouteRepository;
import ch.zhaw.neurofleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import static ch.zhaw.neurofleet.security.Roles.*;

@Service
@RequiredArgsConstructor
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    public Route createRoute(Route route) {
        validateVehicleCapacity(route.getVehicleId(), route.getJobIds());
        Route savedRoute = routeRepository.save(route);
        assignJobsToRoute(savedRoute);
        return savedRoute;
    }

    public Route updateRoute(String id, Route updatedRoute) {
        Route existingRoute = routeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Route not found"));

        if (userService.userHasAnyRole(OWNER)) {
            String userCompanyId = userService.getCompanyIdOfCurrentUser();
            if (!existingRoute.getCompanyId().equals(userCompanyId)) {
                throw new SecurityException("Route does not belong to user's company");
            }
        }

        // Unassign old jobs
        unassignJobsFromRoute(existingRoute);

        // Update fields
        existingRoute.setDescription(updatedRoute.getDescription());
        existingRoute.setScheduledTime(updatedRoute.getScheduledTime());
        existingRoute.setVehicleId(updatedRoute.getVehicleId());
        existingRoute.setJobIds(updatedRoute.getJobIds());
        existingRoute.setTotalPayloadKg(updatedRoute.getTotalPayloadKg());

        // Validate and assign new jobs
        validateVehicleCapacity(existingRoute.getVehicleId(), existingRoute.getJobIds());
        Route savedRoute = routeRepository.save(existingRoute);
        assignJobsToRoute(savedRoute);

        return savedRoute;
    }

    public void deleteRoute(String id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Route not found"));

        if (userService.userHasAnyRole(OWNER)) {
            String userCompanyId = userService.getCompanyIdOfCurrentUser();
            if (!route.getCompanyId().equals(userCompanyId)) {
                throw new SecurityException("Route does not belong to user's company");
            }
        }

        // Unassign all jobs before deleting
        unassignJobsFromRoute(route);
        routeRepository.deleteById(id);
    }

    private void validateVehicleCapacity(String vehicleId, List<String> jobIds) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found"));

        int totalPayload = jobIds.stream()
                .map(jobId -> jobRepository.findById(jobId)
                        .orElseThrow(() -> new NoSuchElementException("Job not found: " + jobId)))
                .mapToInt(Job::getPayloadKg)
                .sum();

        if (totalPayload > vehicle.getVehicleType().getCapacityKg()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Total payload exceeds vehicle capacity. Available: " + vehicle.getVehicleType().getCapacityKg()
                            + "kg, Required: " + totalPayload + "kg");
        }
    }

    private void assignJobsToRoute(Route route) {
        route.getJobIds().forEach(jobId -> {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new NoSuchElementException("Job not found: " + jobId));

            if (jobService.isJobAssigned(job) && !job.getRouteId().equals(route.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Job " + jobId + " is already assigned to another route");
            }

            jobService.assignToRoute(job, route.getId());
        });
    }

    private void unassignJobsFromRoute(Route route) {
        route.getJobIds().forEach(jobId -> {
            jobRepository.findById(jobId).ifPresent(job -> jobService.unassignFromRoute(job));
        });
    }
}