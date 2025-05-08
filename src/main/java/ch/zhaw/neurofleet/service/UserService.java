package ch.zhaw.neurofleet.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.repository.CompanyRepository;

@Service
public class UserService {

    @Autowired
    private CompanyRepository companyRepository;

    public boolean userHasAnyRole(String... roles) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");
        if (userRoles == null)
            return false;

        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    public String getCompanyIdOfCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String me = jwt.getSubject(); // sub
        return companyRepository
                .findByUserIdsContaining(me)
                .map(Company::getId)
                .orElseThrow(() -> new IllegalStateException("No company found for user " + me));
    }

}