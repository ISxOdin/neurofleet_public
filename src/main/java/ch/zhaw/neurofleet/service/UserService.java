package ch.zhaw.neurofleet.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class UserService {
    public boolean userHasAnyRole(String... roles) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");
        if (userRoles == null)
            return false;

        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

}