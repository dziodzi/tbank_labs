package io.github.dziodzi.service;

import io.github.dziodzi.entity.Location;
import io.github.dziodzi.entity.dto.LocationDTO;
import io.github.dziodzi.exception.ResourceNotFoundException;
import io.github.dziodzi.tools.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
@LogExecutionTime
public class LocationService {

    private final InMemoryStore<String, LocationDTO> locationStore;

    public LocationService(InMemoryStore<String, LocationDTO> locationStore) {
        this.locationStore = locationStore;
    }

    public Collection<Location> getAllLocations() {
        var all = locationStore.getAll();
        Collection<Location> locations = new ArrayList<>();
        for (String key : all.keySet()) {
            locations.add(getLocationBySlug(key));
        }
        return locations;
    }

    public Location getLocationBySlug(String key) {
        if (!locationStore.getAll().containsKey(key)) {
            log.warn("Location with slug {} not found for GET operation", key);
            throw new ResourceNotFoundException("Location with slug " + key + " not found");
        }
        return new Location(key, locationStore.get(key));
    }

    public Location createLocation(Location location) {
        if (locationStore.get(location.getSlug()) != null) {
            log.warn("Location with slug {} already exists", location.getSlug());
            throw new IllegalArgumentException("Location with slug " + location.getSlug() + " already exists");
        }
        locationStore.create(location.getSlug(), location.toDTO());
        return location;
    }

    public Location updateLocation(String key, LocationDTO locationDTO) {
        if (locationStore.get(key) == null) {
            log.warn("Location with slug {} not found for UPDATE operation", key);
            throw new ResourceNotFoundException("Location with slug " + key + " not found");
        }
        locationStore.update(key, locationDTO);
        return new Location(key, locationDTO);
    }

    public boolean deleteLocation(String key) {
        if (locationStore.get(key) == null) {
            log.warn("Location with slug {} not found for DELETE operation", key);
            throw new ResourceNotFoundException("Location with slug " + key + " not found");
        }
        locationStore.delete(key);
        return true;
    }

    protected void initializeLocations(Collection<Location> locations) {
        for (Location location : locations) {
            locationStore.create(location.getSlug(), location.toDTO());
        }
    }
}
