package ch.zhaw.neurofleet.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public boolean userHasAnyRole(String... roles) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");
        if (userRoles == null)
            return false;

        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    public String getCompanyIdOfCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> metadata = jwt.getClaim("app_metadata");
        if (metadata == null || !metadata.containsKey("companyId")) {
            throw new IllegalStateException("companyId not set in app_metadata");
        }
        return (String) metadata.get("companyId");
    }

}