package ch.zhaw.neurofleet.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.model.User;
import ch.zhaw.neurofleet.repository.CompanyRepository;
import ch.zhaw.neurofleet.repository.LocationRepository;
import ch.zhaw.neurofleet.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    // Auth0 Methods

    public boolean userHasAnyRole(String... roles) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");
        if (userRoles == null)
            return false;

        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    public String getCompanyIdOfCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String me = jwt.getSubject();
                return companyRepository
                .findByUserIdsContaining(me)
                .map(Company::getId)
                .orElseThrow(() -> new IllegalStateException("No company found for user " + me));
    }
    public String getLocationIdOfFleetManager() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String me = jwt.getSubject();
                return locationRepository
                .findByFleetmanagerId(me)
                .map(Location::getId)
                .orElseThrow(() -> new IllegalStateException("No location found for user " + me));
    }

    // MongoDB Methods
    public User getUserByAuthId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String auth0Id = jwt.getSubject();

        return userRepository
                .findByAuth0Id(auth0Id)
                .orElseThrow(() -> new IllegalStateException("No User for: " + auth0Id));
    }

}