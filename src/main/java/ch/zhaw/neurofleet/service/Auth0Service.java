package ch.zhaw.neurofleet.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ch.zhaw.neurofleet.model.Auth0UserDTO;

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

    public List<Auth0UserDTO> getAllUsers() {
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
                Auth0UserDTO[].class);

        List<Auth0UserDTO> users = Arrays.asList(Objects.requireNonNull(response.getBody()));

        for (Auth0UserDTO dto : users) {
            try {
                List<String> roles = getUserRoles(dto.getUser_id());
                dto.setRoles(roles);
            } catch (Exception e) {
                dto.setRoles(List.of("–"));
            }
        }

        return users;
    }

    public List<String> getUserRoles(String userId) {
        String token = getAccessToken();

        URI uri = UriComponentsBuilder
                .fromUriString("https://" + domain + "/api/v2/users/{userId}/roles")
                .buildAndExpand(userId)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>[]> response = restTemplate.exchange(
                uri, HttpMethod.GET, entity, (Class<Map<String, Object>[]>) (Class<?>) Map[].class);

        return Arrays.stream(response.getBody())
                .map(role -> (String) role.get("name"))
                .collect(Collectors.toList());
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

    public void assignUserRole(String userId, String roleName) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String rolesUrl = "https://" + domain + "/api/v2/roles";
        ResponseEntity<Map[]> response = restTemplate.exchange(
                rolesUrl, HttpMethod.GET, entity, Map[].class);

        String roleId = Arrays.stream(response.getBody())
                .filter(role -> roleName.equalsIgnoreCase((String) role.get("name")))
                .map(role -> (String) role.get("id"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setBearerAuth(token);
        postHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("roles", List.of(roleId));
        HttpEntity<Map<String, Object>> assignEntity = new HttpEntity<>(body, postHeaders);

        String assignUrl = "https://" + domain + "/api/v2/users/" + userId + "/roles";

        restTemplate.postForEntity(assignUrl, assignEntity, Void.class);
    }

    public void deleteUserRole(String userId, String roleName) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        String rolesUrl = "https://" + domain + "/api/v2/roles";

        ResponseEntity<Map[]> response = restTemplate.exchange(
                rolesUrl, HttpMethod.GET, getEntity, Map[].class);

        String roleId = Arrays.stream(response.getBody())
                .filter(role -> roleName.equalsIgnoreCase((String) role.get("name")))
                .map(role -> (String) role.get("id"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setBearerAuth(token);
        deleteHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("roles", List.of(roleId));
        HttpEntity<Map<String, Object>> deleteEntity = new HttpEntity<>(body, deleteHeaders);

        String deleteUrl = "https://" + domain + "/api/v2/users/" + userId + "/roles";

        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, deleteEntity, Void.class);
    }
}
