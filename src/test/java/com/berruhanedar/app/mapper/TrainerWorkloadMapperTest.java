package com.berruhanedar.app.mapper;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.entity.TrainerWorkload;
import com.berruhanedar.app.enums.ActionType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainerWorkloadMapperTest {

    private final TrainerWorkloadMapper trainerWorkloadMapper =
            Mappers.getMapper(TrainerWorkloadMapper.class);

    @Test
    void shouldMapTrainerWorkloadRequestDtoToEntity() {

        TrainerWorkloadRequestDto request =
                new TrainerWorkloadRequestDto();

        request.setTrainerUsername("john.smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2026, 8, 23));
        request.setTrainingDuration(60);
        request.setActionType(ActionType.ADD);

        TrainerWorkload result =
                trainerWorkloadMapper.toEntity(request);

        assertNotNull(result);

        assertEquals(
                request.getTrainerUsername(),
                result.getTrainerUsername()
        );

        assertEquals(
                request.getTrainerFirstName(),
                result.getTrainerFirstName()
        );

        assertEquals(
                request.getTrainerLastName(),
                result.getTrainerLastName()
        );

        assertEquals(
                request.getActive(),
                result.getActive()
        );

        assertNull(result.getId());

        assertTrue(
                result.getYears() == null ||
                        result.getYears().isEmpty()
        );
    }
}