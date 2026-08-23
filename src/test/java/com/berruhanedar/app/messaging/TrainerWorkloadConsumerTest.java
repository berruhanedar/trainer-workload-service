package com.berruhanedar.app.messaging;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.enums.ActionType;
import com.berruhanedar.app.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadConsumerTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private TrainerWorkloadConsumer trainerWorkloadConsumer;

    private TrainerWorkloadRequestDto request;

    @BeforeEach
    void setUp() {
        request = new TrainerWorkloadRequestDto();
        request.setTrainerUsername("john.smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2026, 8, 23));
        request.setTrainingDuration(60);
        request.setActionType(ActionType.ADD);
    }

    @Test
    void shouldProcessTrainerWorkloadWhenMessageIsReceived() {

        trainerWorkloadConsumer.consume(request, "test-transaction-id");

        verify(trainerWorkloadService)
                .processWorkload(request);

        verifyNoMoreInteractions(trainerWorkloadService);
    }
}