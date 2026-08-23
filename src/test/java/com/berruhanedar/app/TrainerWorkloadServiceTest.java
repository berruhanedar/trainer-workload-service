package com.berruhanedar.app.service;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.entity.MonthSummary;
import com.berruhanedar.app.entity.TrainerWorkload;
import com.berruhanedar.app.entity.YearSummary;
import com.berruhanedar.app.enums.ActionType;
import com.berruhanedar.app.mapper.TrainerWorkloadMapper;
import com.berruhanedar.app.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private TrainerWorkloadMapper trainerWorkloadMapper;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

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
    void shouldCreateNewTrainerWithYearAndMonthWhenTrainerDoesNotExist() {
        TrainerWorkload newTrainer = new TrainerWorkload();
        newTrainer.setTrainerUsername(request.getTrainerUsername());
        newTrainer.setTrainerFirstName(request.getTrainerFirstName());
        newTrainer.setTrainerLastName(request.getTrainerLastName());
        newTrainer.setActive(request.getActive());
        newTrainer.setYears(new ArrayList<>());

        when(trainerWorkloadRepository.findByTrainerUsername("john.smith"))
                .thenReturn(Optional.empty());

        when(trainerWorkloadMapper.toEntity(request))
                .thenReturn(newTrainer);

        trainerWorkloadService.processWorkload(request);

        assertEquals(1, newTrainer.getYears().size());

        YearSummary yearSummary = newTrainer.getYears().get(0);

        assertEquals(2026, yearSummary.getYear());
        assertEquals(1, yearSummary.getMonths().size());

        MonthSummary monthSummary = yearSummary.getMonths().get(0);

        assertEquals(8, monthSummary.getMonth());
        assertEquals(60, monthSummary.getTrainingSummaryDuration());

        verify(trainerWorkloadRepository)
                .findByTrainerUsername("john.smith");

        verify(trainerWorkloadMapper)
                .toEntity(request);

        verify(trainerWorkloadRepository)
                .save(newTrainer);
    }

    @Test
    void shouldCreateNewYearAndMonthWhenYearDoesNotExist() {
        YearSummary existingYear = new YearSummary(
                2025,
                new ArrayList<>()
        );

        TrainerWorkload trainer = createTrainer(
                new ArrayList<>(List.of(existingYear))
        );

        when(trainerWorkloadRepository.findByTrainerUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        trainerWorkloadService.processWorkload(request);

        assertEquals(2, trainer.getYears().size());

        YearSummary newYear = trainer.getYears()
                .stream()
                .filter(year -> year.getYear().equals(2026))
                .findFirst()
                .orElse(null);

        assertNotNull(newYear);
        assertEquals(1, newYear.getMonths().size());

        MonthSummary monthSummary = newYear.getMonths().get(0);

        assertEquals(8, monthSummary.getMonth());
        assertEquals(60, monthSummary.getTrainingSummaryDuration());

        verify(trainerWorkloadMapper, never())
                .toEntity(any());

        verify(trainerWorkloadRepository)
                .save(trainer);
    }

    @Test
    void shouldCreateNewMonthWhenMonthDoesNotExist() {
        MonthSummary july = new MonthSummary(7, 120);

        YearSummary yearSummary = new YearSummary(
                2026,
                new ArrayList<>(List.of(july))
        );

        TrainerWorkload trainer = createTrainer(
                new ArrayList<>(List.of(yearSummary))
        );

        when(trainerWorkloadRepository.findByTrainerUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        trainerWorkloadService.processWorkload(request);

        assertEquals(2, yearSummary.getMonths().size());

        MonthSummary august = yearSummary.getMonths()
                .stream()
                .filter(month -> month.getMonth().equals(8))
                .findFirst()
                .orElse(null);

        assertNotNull(august);
        assertEquals(60, august.getTrainingSummaryDuration());

        verify(trainerWorkloadRepository)
                .save(trainer);
    }

    @Test
    void shouldAddTrainingDurationToExistingMonth() {
        MonthSummary august = new MonthSummary(8, 120);

        YearSummary yearSummary = new YearSummary(
                2026,
                new ArrayList<>(List.of(august))
        );

        TrainerWorkload trainer = createTrainer(
                new ArrayList<>(List.of(yearSummary))
        );

        when(trainerWorkloadRepository.findByTrainerUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        trainerWorkloadService.processWorkload(request);

        assertEquals(
                180,
                august.getTrainingSummaryDuration()
        );

        verify(trainerWorkloadRepository)
                .save(trainer);
    }

    @Test
    void shouldSubtractTrainingDurationWhenActionTypeIsDelete() {
        request.setActionType(ActionType.DELETE);
        request.setTrainingDuration(60);

        MonthSummary august = new MonthSummary(8, 180);

        YearSummary yearSummary = new YearSummary(
                2026,
                new ArrayList<>(List.of(august))
        );

        TrainerWorkload trainer = createTrainer(
                new ArrayList<>(List.of(yearSummary))
        );

        when(trainerWorkloadRepository.findByTrainerUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        trainerWorkloadService.processWorkload(request);

        assertEquals(
                120,
                august.getTrainingSummaryDuration()
        );

        verify(trainerWorkloadRepository)
                .save(trainer);
    }

    @Test
    void shouldNotAllowNegativeDurationWhenDeletingTraining() {
        request.setActionType(ActionType.DELETE);
        request.setTrainingDuration(100);

        MonthSummary august = new MonthSummary(8, 40);

        YearSummary yearSummary = new YearSummary(
                2026,
                new ArrayList<>(List.of(august))
        );

        TrainerWorkload trainer = createTrainer(
                new ArrayList<>(List.of(yearSummary))
        );

        when(trainerWorkloadRepository.findByTrainerUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        trainerWorkloadService.processWorkload(request);

        assertEquals(
                0,
                august.getTrainingSummaryDuration()
        );

        verify(trainerWorkloadRepository)
                .save(trainer);
    }

    private TrainerWorkload createTrainer(List<YearSummary> years) {
        TrainerWorkload trainer = new TrainerWorkload();

        trainer.setId("trainer-id");
        trainer.setTrainerUsername("john.smith");
        trainer.setTrainerFirstName("John");
        trainer.setTrainerLastName("Smith");
        trainer.setActive(true);
        trainer.setYears(years);

        return trainer;
    }
}