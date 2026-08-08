package com.berruhanedar.app.repository;

import com.berruhanedar.app.entity.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, Long> {

    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);
}