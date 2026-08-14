package com.berruhanedar.app.messaging;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadConsumer {

    private final TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "${app.messaging.trainer-workload-queue}")
    public void consume(TrainerWorkloadRequestDto request) {
        log.info("Received trainer workload message. trainerUsername={}, actionType={}",
                request.getTrainerUsername(),
                request.getActionType());
        trainerWorkloadService.processWorkload(request);
    }
}