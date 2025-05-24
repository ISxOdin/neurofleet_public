package ch.zhaw.neurofleet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.zhaw.neurofleet.service.UserService;
import ch.zhaw.neurofleet.tools.NeuroFleetTools;
import ch.zhaw.neurofleet.repository.CompanyRepository;
import ch.zhaw.neurofleet.model.Company;
import static ch.zhaw.neurofleet.security.Roles.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/optimization")
public class OptimizationController {

    private static final Logger logger = LoggerFactory.getLogger(OptimizationController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private NeuroFleetTools neuroFleetTools;

    private String getCompanyIdWithPermissionCheck() {
        if (userService.userHasAnyRole(OWNER, FLEETMANAGER)) {
            return userService.getCompanyIdOfCurrentUser();
        }
        return null; // Admin can access all companies
    }

    private String getLocationIdWithPermissionCheck() {
        if (userService.userHasAnyRole(FLEETMANAGER)) {
            return userService.getLocationIdOfCurrentUser();
        }
        return null; // Admin and Owner can access all locations in their scope
    }

    @GetMapping("/route-efficiency")
    public ResponseEntity<String> analyzeRouteEfficiency(
            @RequestParam(required = false) String companyId) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String effectiveCompanyId = companyId != null ? companyId : getCompanyIdWithPermissionCheck();
            if (effectiveCompanyId == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String locationId = getLocationIdWithPermissionCheck();
            String analysis = neuroFleetTools.analyzeRouteEfficiency(effectiveCompanyId, locationId);
            return ResponseEntity.ok(analysis);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/job-distribution")
    public ResponseEntity<String> suggestJobDistribution(
            @RequestParam(required = false) String companyId) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String effectiveCompanyId = companyId != null ? companyId : getCompanyIdWithPermissionCheck();
            if (effectiveCompanyId == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String locationId = getLocationIdWithPermissionCheck();
            String suggestions = neuroFleetTools.suggestJobDistribution(effectiveCompanyId, locationId);
            return ResponseEntity.ok(suggestions);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/route-consolidation")
    public ResponseEntity<String> findRouteConsolidationOpportunities(
            @RequestParam(required = false) String companyId) {
        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String effectiveCompanyId = companyId != null ? companyId : getCompanyIdWithPermissionCheck();
            if (effectiveCompanyId == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String locationId = getLocationIdWithPermissionCheck();
            String opportunities = neuroFleetTools.findRouteConsolidationOpportunities(effectiveCompanyId, locationId);
            return ResponseEntity.ok(opportunities);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getOptimizationSummary(
            @RequestParam(required = false) String companyId) {
        logger.info("Optimization summary requested for company: {}", companyId);

        if (!userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)) {
            logger.warn("Unauthorized access attempt by user: {}", userService.getAuthIdOfCurrentUser());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String effectiveCompanyId = companyId != null ? companyId : getCompanyIdWithPermissionCheck();
            logger.debug("Using company ID: {}", effectiveCompanyId);

            if (effectiveCompanyId == null) {
                logger.warn("Company not found or user has no company access");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String locationId = getLocationIdWithPermissionCheck();
            logger.debug("Using location ID: {}", locationId);

            List<Map<String, Object>> suggestions = new ArrayList<>();

            String routeEfficiency = neuroFleetTools.analyzeRouteEfficiency(effectiveCompanyId, locationId);
            if (!routeEfficiency.trim().isEmpty()) {
                Pattern utilizationPattern = Pattern.compile("Capacity Utilization: (\\d+\\.?\\d*)%");
                Matcher matcher = utilizationPattern.matcher(routeEfficiency);
                double utilization = matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;

                Map<String, Object> routeOptimization = new HashMap<>();
                routeOptimization.put("id", 1);
                routeOptimization.put("type", "route_optimization");
                routeOptimization.put("title", "Route Optimization Available");
                routeOptimization.put("description", String.format(
                        "Current route utilization is %.1f%%. Optimization could improve efficiency.", utilization));
                routeOptimization.put("impact", utilization < 30 ? "high" : utilization < 60 ? "medium" : "low");
                routeOptimization.put("potentialSavings",
                        String.format("Improve utilization from %.1f%%", utilization));
                routeOptimization.put("confidence", 0.89);
                suggestions.add(routeOptimization);
            }

            String jobDistribution = neuroFleetTools.suggestJobDistribution(effectiveCompanyId, locationId);
            if (!jobDistribution.trim().isEmpty()) {
                Pattern jobsPattern = Pattern.compile("Total Jobs: (\\d+)");
                Matcher matcher = jobsPattern.matcher(jobDistribution);
                int totalJobs = 0;
                while (matcher.find()) {
                    totalJobs += Integer.parseInt(matcher.group(1));
                }

                Map<String, Object> loadBalancing = new HashMap<>();
                loadBalancing.put("id", 2);
                loadBalancing.put("type", "load_balancing");
                loadBalancing.put("title", "Load Distribution Improvement");
                loadBalancing.put("description", String.format(
                        "Redistributing %d jobs across available vehicles could improve efficiency", totalJobs));
                loadBalancing.put("impact", totalJobs > 5 ? "high" : "medium");
                loadBalancing.put("potentialSavings", String.format("Optimize %d jobs distribution", totalJobs));
                loadBalancing.put("confidence", 0.78);
                suggestions.add(loadBalancing);
            }

            String consolidation = neuroFleetTools.findRouteConsolidationOpportunities(effectiveCompanyId, locationId);
            if (!consolidation.trim().isEmpty()) {
                Map<String, Object> scheduleOptimization = new HashMap<>();
                scheduleOptimization.put("id", 3);
                scheduleOptimization.put("type", "schedule_optimization");
                scheduleOptimization.put("title", "Schedule Adjustment Recommended");
                scheduleOptimization.put("description", "Route consolidation opportunities identified");
                scheduleOptimization.put("impact", "medium");
                scheduleOptimization.put("potentialSavings", "Potential route consolidation savings");
                scheduleOptimization.put("confidence", 0.85);
                suggestions.add(scheduleOptimization);
            }

            Map<String, List<Map<String, Object>>> response = new HashMap<>();
            response.put("optimizationSuggestions", suggestions);

            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            logger.warn("Security exception: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error processing optimization summary", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}