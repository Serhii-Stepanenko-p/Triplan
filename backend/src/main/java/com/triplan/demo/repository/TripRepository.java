package com.triplan.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.triplan.demo.model.Trip;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByUserId(Long userId);
    List<Trip> findByUserIdOrderByCreatedAtDesc(Long userId);
}