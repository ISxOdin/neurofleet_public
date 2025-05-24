package ch.zhaw.neurofleet.tools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.model.Route;
import ch.zhaw.neurofleet.model.Vehicle;
import ch.zhaw.neurofleet.model.VehicleState;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.repository.LocationRepository;
import ch.zhaw.neurofleet.repository.RouteRepository;
import ch.zhaw.neurofleet.repository.VehicleRepository;
import ch.zhaw.neurofleet.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NeuroFleetTools {

    private static final Logger logger = LoggerFactory.getLogger(NeuroFleetTools.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserService userService;

    @Tool(description = "Get all routes and their current efficiency metrics.")
    public String analyzeRouteEfficiency(String inputCompanyId, String inputLocationId) {
        // If no companyId provided, get from current user
        final String companyId;
        if (inputCompanyId == null) {
            try {
                companyId = userService.getCompanyIdOfCurrentUser();
                logger.debug("Using user's company ID: {}", companyId);
            } catch (Exception e) {
                logger.warn("Could not get user's company ID: {}", e.getMessage());
                return "Unable to determine company. Please ensure you are properly authenticated.";
            }
        } else {
            companyId = inputCompanyId;
        }

        // If no locationId provided, try to get from current user if they're a fleet
        // manager
        final String locationId = determineLocationId(inputLocationId);

        List<Route> routes = routeRepository.findAll().stream()
                .filter(r -> r.getCompanyId().equals(companyId))
                .collect(Collectors.toList());
        StringBuilder analysis = new StringBuilder();

        if (routes.isEmpty()) {
            return "No routes found for the specified company.";
        }

        for (Route route : routes) {
            Vehicle vehicle = vehicleRepository.findById(route.getVehicleId()).orElse(null);
            if (vehicle == null || (locationId != null && !vehicle.getLocationId().equals(locationId)))
                continue;

            List<Job> jobs = jobRepository.findAllById(route.getJobIds());
            int totalPayload = jobs.stream().mapToInt(Job::getPayloadKg).sum();
            double capacityUtilization = (double) totalPayload / vehicle.getVehicleType().getCapacityKg() * 100;

            analysis.append(String.format("Route %s:\n", route.getId()))
                    .append(String.format("- Vehicle: %s (%s)\n", vehicle.getLicensePlate(), vehicle.getVehicleType()))
                    .append(String.format("- Capacity Utilization: %.1f%%\n", capacityUtilization))
                    .append(String.format("- Number of Jobs: %d\n", jobs.size()))
                    .append("---\n");
        }

        return analysis.toString();
    }

    @Tool(description = "Suggest better job distributions across available vehicles.")
    public String suggestJobDistribution(String inputCompanyId, String inputLocationId) {
        // If no companyId provided, get from current user
        final String companyId;
        if (inputCompanyId == null) {
            try {
                companyId = userService.getCompanyIdOfCurrentUser();
                logger.debug("Using user's company ID: {}", companyId);
            } catch (Exception e) {
                logger.warn("Could not get user's company ID: {}", e.getMessage());
                return "Unable to determine company. Please ensure you are properly authenticated.";
            }
        } else {
            companyId = inputCompanyId;
        }

        // If no locationId provided, try to get from current user if they're a fleet
        // manager
        final String locationId = determineLocationId(inputLocationId);

        List<Vehicle> availableVehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getState() == VehicleState.AVAILABLE)
                .filter(v -> v.getCompanyId().equals(companyId))
                .filter(v -> locationId == null || v.getLocationId().equals(locationId))
                .collect(Collectors.toList());

        if (availableVehicles.isEmpty()) {
            return "No available vehicles found for the specified company and location.";
        }

        List<Job> unassignedJobs = jobRepository.findAll().stream()
                .filter(j -> j.getRouteId() == null)
                .filter(j -> j.getCompanyId().equals(companyId))
                .collect(Collectors.toList());

        StringBuilder suggestions = new StringBuilder();
        suggestions.append("Available Vehicles:\n");
        for (Vehicle vehicle : availableVehicles) {
            suggestions.append(String.format("- %s (%s): Capacity %dkg\n",
                    vehicle.getLicensePlate(),
                    vehicle.getVehicleType(),
                    vehicle.getVehicleType().getCapacityKg()));
        }

        suggestions.append("\nUnassigned Jobs:\n");
        Map<String, List<Job>> jobsByLocation = unassignedJobs.stream()
                .collect(Collectors.groupingBy(Job::getOriginId));

        for (Map.Entry<String, List<Job>> entry : jobsByLocation.entrySet()) {
            Location location = locationRepository.findById(entry.getKey()).orElse(null);
            if (location == null || (locationId != null && !location.getId().equals(locationId)))
                continue;

            int totalPayload = entry.getValue().stream().mapToInt(Job::getPayloadKg).sum();
            suggestions.append(String.format("Location %s:\n", location.getName()))
                    .append(String.format("- Total Jobs: %d\n", entry.getValue().size()))
                    .append(String.format("- Total Payload: %dkg\n", totalPayload));

            for (Vehicle vehicle : availableVehicles) {
                if (totalPayload <= vehicle.getVehicleType().getCapacityKg()) {
                    suggestions.append(String.format("- Suggested Vehicle: %s (Utilization: %.1f%%)\n",
                            vehicle.getLicensePlate(),
                            (double) totalPayload / vehicle.getVehicleType().getCapacityKg() * 100));
                    break;
                }
            }
            suggestions.append("---\n");
        }

        return suggestions.toString();
    }

    @Tool(description = "Identify opportunities to consolidate routes and improve efficiency.")
    public String findRouteConsolidationOpportunities(String inputCompanyId, String inputLocationId) {
        // If no companyId provided, get from current user
        final String companyId;
        if (inputCompanyId == null) {
            try {
                companyId = userService.getCompanyIdOfCurrentUser();
                logger.debug("Using user's company ID: {}", companyId);
            } catch (Exception e) {
                logger.warn("Could not get user's company ID: {}", e.getMessage());
                return "Unable to determine company. Please ensure you are properly authenticated.";
            }
        } else {
            companyId = inputCompanyId;
        }

        // If no locationId provided, try to get from current user if they're a fleet
        // manager
        final String locationId = determineLocationId(inputLocationId);

        List<Route> activeRoutes = routeRepository.findAll().stream()
                .filter(r -> r.getCompanyId().equals(companyId))
                .collect(Collectors.toList());

        if (activeRoutes.isEmpty()) {
            return "No active routes found for the specified company.";
        }

        StringBuilder opportunities = new StringBuilder();

        Map<String, List<Route>> routesByOrigin = activeRoutes.stream()
                .filter(r -> !r.getJobIds().isEmpty())
                .collect(Collectors.groupingBy(r -> {
                    Job firstJob = jobRepository.findById(r.getJobIds().get(0)).orElse(null);
                    return firstJob != null ? firstJob.getOriginId() : "unknown";
                }));

        for (Map.Entry<String, List<Route>> entry : routesByOrigin.entrySet()) {
            Location location = locationRepository.findById(entry.getKey()).orElse(null);
            if (location == null || (locationId != null && !location.getId().equals(locationId)))
                continue;

            List<Route> routes = entry.getValue();
            if (routes.size() < 2)
                continue;

            opportunities.append(String.format("Potential consolidation at %s:\n", location.getName()));

            for (Route route : routes) {
                Vehicle vehicle = vehicleRepository.findById(route.getVehicleId()).orElse(null);
                if (vehicle == null)
                    continue;

                List<Job> jobs = jobRepository.findAllById(route.getJobIds());
                int totalPayload = jobs.stream().mapToInt(Job::getPayloadKg).sum();
                double utilization = (double) totalPayload / vehicle.getVehicleType().getCapacityKg() * 100;

                opportunities.append(String.format("- Route %s:\n", route.getId()))
                        .append(String.format("  * Vehicle: %s (%s)\n", vehicle.getLicensePlate(),
                                vehicle.getVehicleType()))
                        .append(String.format("  * Current Utilization: %.1f%%\n", utilization))
                        .append(String.format("  * Jobs: %d\n", jobs.size()));
            }

            opportunities.append("\nConsolidation Suggestion:\n")
                    .append("Consider combining these routes to improve vehicle utilization.\n")
                    .append("This could reduce the number of vehicles needed and improve efficiency.\n")
                    .append("---\n");
        }

        return opportunities.toString();
    }

    private String determineLocationId(String inputLocationId) {
        if (inputLocationId != null) {
            return inputLocationId;
        }

        if (userService.userHasAnyRole("FLEETMANAGER")) {
            try {
                String locationId = userService.getLocationIdOfCurrentUser();
                logger.debug("Using user's location ID: {}", locationId);
                return locationId;
            } catch (Exception e) {
                logger.warn("Could not get user's location ID: {}", e.getMessage());
            }
        }
        return null;
    }
}
