package ch.zhaw.neurofleet.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ch.zhaw.neurofleet.model.AppMetadataDTO;
import ch.zhaw.neurofleet.model.Auth0RoleDTO;
import ch.zhaw.neurofleet.model.Auth0UserDTO;
import ch.zhaw.neurofleet.model.EnrichedUserDTO;

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
                Auth0UserDTO[].class);

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
                Auth0RoleDTO[].class);

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

    public AppMetadataDTO getUserAppMetadata(String userId) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "https://" + domain + "/api/v2/users/" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map<String, Object> body = response.getBody();
        Map<String, Object> metadata = (Map<String, Object>) body.get("app_metadata");

        AppMetadataDTO dto = new AppMetadataDTO();
        if (metadata != null) {
            dto.setCompanyId((String) metadata.getOrDefault("companyId", ""));
        } else {
            dto.setCompanyId("");
        }

        return dto;
    }

    public void updateUserAppMetadata(String userId, AppMetadataDTO dto) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of("app_metadata", Map.of("companyId", dto.getCompanyId()));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        String url = "https://" + domain + "/api/v2/users/" + userId;
        restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
    }

    public List<EnrichedUserDTO> fetchAllEnrichedUsers() {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromUriString("https://" + domain + "/api/v2/users")
                .queryParam("per_page", 100)
                .toUriString();

        ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map[].class);

        Map[] users = response.getBody();
        List<EnrichedUserDTO> result = new ArrayList<>();

        for (Map user : users) {
            EnrichedUserDTO dto = new EnrichedUserDTO();
            dto.setUser_id((String) user.get("user_id"));
            dto.setEmail((String) user.get("email"));
            dto.setName((String) user.get("name"));
            dto.setGiven_name((String) user.get("given_name"));
            dto.setFamily_name((String) user.get("family_name"));

            // app_metadata.companyId
            Map<String, Object> metadata = (Map<String, Object>) user.get("app_metadata");
            if (metadata != null && metadata.containsKey("companyId")) {
                dto.setCompanyId((String) metadata.get("companyId"));
            }

            // Rollen separat laden
            try {
                List<String> roles = getUserRoles(dto.getUser_id());
                dto.setRoles(roles);
            } catch (Exception e) {
                dto.setRoles(List.of("–"));
            }

            result.add(dto);
        }

        return result;
    }

}
