package com.berruhanedar.app.repository;

import com.berruhanedar.app.entity.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);
}