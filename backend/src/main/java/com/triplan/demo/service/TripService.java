package com.triplan.demo.service;

import org.springframework.stereotype.Service;
import com.triplan.demo.dto.TripDTO;
import com.triplan.demo.model.Trip;
import com.triplan.demo.model.User;
import com.triplan.demo.repository.TripRepository;
import com.triplan.demo.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    public TripService(TripRepository tripRepository,
                       UserRepository userRepository,
                       CacheService cacheService) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    public TripDTO createTrip(TripDTO tripDTO, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setStartLocation(tripDTO.getStartLocation());
        trip.setEndLocation(tripDTO.getEndLocation());
        trip.setStartDate(tripDTO.getStartDate());
        trip.setEndDate(tripDTO.getEndDate());
        trip.setBudget(tripDTO.getBudget());
        trip.setAccommodationPreferences(tripDTO.getAccommodationPreferences());
        trip.setTransportPreferences(tripDTO.getTransportPreferences());
        trip.setFoodPreferences(tripDTO.getFoodPreferences());
        trip.setEntertainmentPreferences(tripDTO.getEntertainmentPreferences());

        Trip savedTrip = tripRepository.save(trip);
        TripDTO result = convertToDTO(savedTrip);

        // Кешуємо новий trip
        cacheService.cacheTrip(result.getId(), result);
        // Інвалідуємо кеш списку подорожей користувача
        cacheService.evictUserTrips(user.getId());

        return result;
    }

    public List<TripDTO> getUserTrips(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Спробуємо отримати з кешу
        List<TripDTO> cachedTrips = cacheService.getCachedUserTrips(user.getId());
        if (cachedTrips != null && !cachedTrips.isEmpty()) {
            return cachedTrips;
        }

        // Якщо в кеші немає - отримуємо з БД
        List<TripDTO> trips = tripRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Кешуємо результат
        cacheService.cacheUserTrips(user.getId(), trips);

        return trips;
    }

    public TripDTO updateTrip(Long id, TripDTO tripDTO, String email) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        trip.setStartLocation(tripDTO.getStartLocation());
        trip.setEndLocation(tripDTO.getEndLocation());
        trip.setStartDate(tripDTO.getStartDate());
        trip.setEndDate(tripDTO.getEndDate());
        trip.setBudget(tripDTO.getBudget());
        trip.setAccommodationPreferences(tripDTO.getAccommodationPreferences());
        trip.setTransportPreferences(tripDTO.getTransportPreferences());
        trip.setFoodPreferences(tripDTO.getFoodPreferences());
        trip.setEntertainmentPreferences(tripDTO.getEntertainmentPreferences());

        Trip updatedTrip = tripRepository.save(trip);
        TripDTO result = convertToDTO(updatedTrip);

        // Оновлюємо кеш
        cacheService.cacheTrip(id, result);
        cacheService.evictUserTrips(trip.getUser().getId());

        return result;
    }

    public void deleteTrip(Long id, String email) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        Long userId = trip.getUser().getId();
        tripRepository.delete(trip);

        // Видаляємо з кешу
        cacheService.evictTrip(id);
        cacheService.evictUserTrips(userId);
    }

    private TripDTO convertToDTO(Trip trip) {
        TripDTO dto = new TripDTO();
        dto.setId(trip.getId());
        dto.setStartLocation(trip.getStartLocation());
        dto.setEndLocation(trip.getEndLocation());
        dto.setStartDate(trip.getStartDate());
        dto.setEndDate(trip.getEndDate());
        dto.setBudget(trip.getBudget());
        dto.setAccommodationPreferences(trip.getAccommodationPreferences());
        dto.setTransportPreferences(trip.getTransportPreferences());
        dto.setFoodPreferences(trip.getFoodPreferences());
        dto.setEntertainmentPreferences(trip.getEntertainmentPreferences());
        dto.setStatus(trip.getStatus().name());
        return dto;
    }
}
