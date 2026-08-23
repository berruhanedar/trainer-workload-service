package com.berruhanedar.app.messaging;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadConsumer {

    private static final String TRANSACTION_ID = "transactionId";
    private final TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "${app.messaging.trainer-workload-queue}")
    public void consume(@Valid TrainerWorkloadRequestDto request, @Header(name = TRANSACTION_ID, required = false) String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        try {
            MDC.put(TRANSACTION_ID, transactionId);
            log.info("Received trainer workload message. trainerUsername={}, actionType={}", request.getTrainerUsername(), request.getActionType());
            trainerWorkloadService.processWorkload(request);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}