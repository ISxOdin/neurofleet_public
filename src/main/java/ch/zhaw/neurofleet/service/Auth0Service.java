package ch.zhaw.neurofleet.service;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ch.zhaw.neurofleet.model.Auth0RoleDTO;
import lombok.Data;

@Service
public class Auth0Service {

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.clientId}")
    private String clientId;

    @Value("${auth0.clientSecret}")
    private String clientSecret;

    @Value("${auth0.audience}")
    private String audience;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Auth0UserDTO> fetchAllUsers() {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromUriString("https://" + domain + "/api/v2/users")
                .queryParam("per_page", 100)
                .toUriString();

        ResponseEntity<Auth0UserDTO[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Auth0UserDTO[].class
        );

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public List<String> getUserRoles(String userId) {
        String token = getAccessToken();
    
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    
        HttpEntity<Void> entity = new HttpEntity<>(headers);
    
        String url = "https://" + domain + "/api/v2/users/" + userId + "/roles";
    
        ResponseEntity<Auth0RoleDTO[]> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Auth0RoleDTO[].class
        );
    
        return Arrays.stream(response.getBody())
                     .map(Auth0RoleDTO::getName)
                     .toList();
    }

    private String getAccessToken() {
        String url = "https://" + domain + "/oauth/token";

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("audience", audience);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        return (String) response.get("access_token");
    }

    @Data
    public static class Auth0UserDTO {
        private String user_id;
        private String email;
        private String name;
    }
}
