package ch.zhaw.neurofleet.tools;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.repository.LocationRepository;
import ch.zhaw.neurofleet.service.CompanyService;
import ch.zhaw.neurofleet.service.UserService;

public class NeuroFleetTools {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserService userService;

    @Tool(description = "Information about the jobs in the database.")
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Tool(description = "Information about the companies in the database.")
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @Tool(description = "Information about the locations in the database.")
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Tool(description = "Information about the current users company.")
    public String getCurrentUserCompany() {
        return userService.getCompanyIdOfCurrentUser();
    }
}
