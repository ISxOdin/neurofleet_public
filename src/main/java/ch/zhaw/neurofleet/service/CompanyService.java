package ch.zhaw.neurofleet.service;

import ch.zhaw.neurofleet.model.Auth0UserDTO;
import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.CompanyCreateDTO;
import ch.zhaw.neurofleet.repository.CompanyRepository;
import ch.zhaw.neurofleet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CompanyService {

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    Auth0Service auth0Service;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    public CompanyWithOwnerEmail createCompany(String name, String email, String address) {
        Coordinates coordinates = geocodeAddress(address);

        Company company = new Company(name, email, address, coordinates.getLatitude(), coordinates.getLongitude());

        AtomicReference<String> ownerEmail = new AtomicReference<>(null); // Container for owner email

        try {
            List<Auth0UserDTO> users = auth0Service.getAllUsers();
            users.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .ifPresent(u -> {
                        company.setOwner(u.getUser_id());
                        ownerEmail.set(u.getEmail());
                    });
        } catch (Exception e) {
            System.err.println("Auth0 lookup failed: " + e.getMessage());
        }

        return new CompanyWithOwnerEmail(companyRepository.save(company), ownerEmail.get());

    }

    public CompanyWithOwnerEmail updateCompany(String id, CompanyCreateDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        String oldOwner = company.getOwner();
        String newOwner = dto.getOwner();
        String ownerEmail = null;

        if (newOwner != null && !newOwner.equals(oldOwner)) {
            if (oldOwner != null && !oldOwner.isBlank()) {
                try {
                    auth0Service.deleteUserRole(oldOwner, "owner");
                } catch (Exception e) {
                    System.err.println("Could not remove role from old owner: " + e.getMessage());
                }
            }

            try {
                auth0Service.assignUserRole(newOwner, "owner");
            } catch (Exception e) {
                System.err.println("Could not assign role to new owner: " + e.getMessage());
            }

            try {
                ownerEmail = auth0Service.getAllUsers().stream()
                        .filter(u -> u.getUser_id().equals(newOwner))
                        .map(Auth0UserDTO::getEmail)
                        .findFirst()
                        .orElse(null);
            } catch (Exception e) {
                System.err.println("Failed to resolve new owner email: " + e.getMessage());
            }

            company.setOwner(newOwner);
        }

        company.setName(dto.getName());
        company.setEmail(dto.getEmail());

        if (!company.getAddress().equals(dto.getAddress())) {
            Coordinates coords = geocodeAddress(dto.getAddress());
            company.setAddress(dto.getAddress());
            company.setLatitude(coords.getLatitude());
            company.setLongitude(coords.getLongitude());
        }

        return new CompanyWithOwnerEmail(companyRepository.save(company), ownerEmail);
    }

    public Company addUserToCompany(String companyId, String userId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        if (!company.getUserIds().contains(userId)) {
            company.getUserIds().add(userId);
            return companyRepository.save(company);
        }
        return company;
    }

    public Company removeUserFromCompany(String companyId, String userId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        if (company.getUserIds().remove(userId)) {
            return companyRepository.save(company);
        }
        return company;
    }

    private Coordinates geocodeAddress(String address) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + address.replace(" ", "+")
                + "&key=" + googleApiKey;

        GoogleGeocodingResponse response = restTemplate.getForObject(url, GoogleGeocodingResponse.class);

        if (response != null && response.status.equals("OK") && !response.results.isEmpty()) {
            double lat = response.results.get(0).geometry.location.lat;
            double lon = response.results.get(0).geometry.location.lng;
            return new Coordinates(lat, lon);
        } else {
            throw new RuntimeException("Could not geocode address: " + address);
        }
    }

    // Helper classes

    @RequiredArgsConstructor
    @Getter
    private static class Coordinates {
        private final double latitude;
        private final double longitude;
    }

    // Response mapping classes for Google Geocoding API
    private static class GoogleGeocodingResponse {
        public String status;
        public java.util.List<Result> results;
    }

    private static class Result {
        public Geometry geometry;
    }

    private static class Geometry {
        public LocationData location;
    }

    private static class LocationData {
        public double lat;
        public double lng;
    }

    @Getter
    @AllArgsConstructor
    public static class CompanyWithOwnerEmail {
        private final Company company;
        private final String ownerEmail;
    }

}
