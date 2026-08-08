package com.berruhanedar.app.service;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.entity.MonthSummary;
import com.berruhanedar.app.entity.TrainerWorkload;
import com.berruhanedar.app.entity.YearSummary;
import com.berruhanedar.app.enums.ActionType;
import com.berruhanedar.app.mapper.TrainerWorkloadMapper;
import com.berruhanedar.app.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final TrainerWorkloadMapper trainerWorkloadMapper;

    @Transactional
    public void processWorkload(TrainerWorkloadRequestDto request) {
        log.info("Processing trainer workload. trainerUsername={}, trainingDate={}, duration={}, actionType={}",
                request.getTrainerUsername(),
                request.getTrainingDate(),
                request.getTrainingDuration(),
                request.getActionType());
        TrainerWorkload trainer = trainerWorkloadRepository.findByTrainerUsername(request.getTrainerUsername()).orElseGet(() -> trainerWorkloadMapper.toEntity(request));
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        YearSummary yearSummary = trainer.getYears().stream().filter(y -> y.getYear().equals(year)).findFirst().orElseGet(() -> createYearSummary(trainer, year));
        MonthSummary monthSummary = yearSummary.getMonths().stream().filter(m -> m.getMonth().equals(month)).findFirst().orElseGet(() -> createMonthSummary(yearSummary, month));
        updateDuration(monthSummary, request);
        trainerWorkloadRepository.save(trainer);
        log.info("Trainer workload processed successfully. trainerUsername={}, year={}, month={}, totalDuration={}",
                request.getTrainerUsername(),
                year,
                month,
                monthSummary.getTrainingSummaryDuration());
    }

    private YearSummary createYearSummary(TrainerWorkload trainer, int year) {
        YearSummary summary = new YearSummary();
        summary.setYear(year);
        summary.setTrainerWorkload(trainer);
        trainer.getYears().add(summary);
        return summary;
    }

    private MonthSummary createMonthSummary(YearSummary yearSummary, int month) {
        MonthSummary summary = new MonthSummary();
        summary.setMonth(month);
        summary.setTrainingSummaryDuration(0);
        summary.setYearSummary(yearSummary);
        yearSummary.getMonths().add(summary);
        return summary;
    }

    private void updateDuration(MonthSummary monthSummary, TrainerWorkloadRequestDto request) {
        int duration = monthSummary.getTrainingSummaryDuration();
        if (request.getActionType() == ActionType.ADD) {
            duration += request.getTrainingDuration();
        } else if (request.getActionType() == ActionType.DELETE) {
            duration -= request.getTrainingDuration();
        }
        monthSummary.setTrainingSummaryDuration(Math.max(duration, 0));
    }
}