package ch.zhaw.neurofleet.service;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.LocationCreateDTO;
import ch.zhaw.neurofleet.model.Location;
import ch.zhaw.neurofleet.repository.LocationRepository;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    public Location createLocation(String name, String address, String companyId) {
        Coordinates coordinates = geocodeAddress(address);

        Location location = new Location(
                name,
                address,
                coordinates.getLatitude(),
                coordinates.getLongitude(),
                companyId);

        return locationRepository.save(location);
    }

    public Location updateLocation(String id, LocationCreateDTO dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Location not found"));

        location.setName(dto.getName());
        location.setEmail(dto.getEmail());
        location.setOwner(dto.getOwner());

        if (!location.getAddress().equals(dto.getAddress())) {
            Coordinates coords = geocodeAddress(dto.getAddress());
            location.setAddress(dto.getAddress());
            location.setLatitude(coords.getLatitude());
            location.setLongitude(coords.getLongitude());
        }

        return locationRepository.save(location);
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
    @lombok.Getter
    private static class Coordinates {
        private final double latitude;
        private final double longitude;
    }

    // Mapping classes
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
