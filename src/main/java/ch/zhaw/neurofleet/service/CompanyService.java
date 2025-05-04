package ch.zhaw.neurofleet.service;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.CompanyCreateDTO;
import ch.zhaw.neurofleet.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.Getter;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    public Company createCompany(String name, String email, String address) {
        Coordinates coordinates = geocodeAddress(address);

        Company company = new Company(
                name,
                email,
                address,
                coordinates.getLatitude(),
                coordinates.getLongitude());

        return companyRepository.save(company);
    }

    public Company updateCompany(String id, CompanyCreateDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        company.setName(dto.getName());
        company.setEmail(dto.getEmail());

        if (!company.getAddress().equals(dto.getAddress())) {
            Coordinates coords = geocodeAddress(dto.getAddress());
            company.setAddress(dto.getAddress());
            company.setLatitude(coords.getLatitude());
            company.setLongitude(coords.getLongitude());
        }

        return companyRepository.save(company);
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
}
