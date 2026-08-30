package com.berruhanedar.app.cucumber.steps;

import com.berruhanedar.app.dto.TrainerWorkloadRequestDto;
import com.berruhanedar.app.enums.ActionType;
import com.berruhanedar.app.messaging.TrainerWorkloadConsumer;
import com.berruhanedar.app.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TrainerWorkloadConsumerSteps {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    private TrainerWorkloadConsumer trainerWorkloadConsumer;
    private TrainerWorkloadRequestDto request;
    private String transactionId;

    private Validator validator;
    private Set<ConstraintViolation<TrainerWorkloadRequestDto>> violations;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        trainerWorkloadConsumer = new TrainerWorkloadConsumer(trainerWorkloadService);
        validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }

    @Given("a valid trainer workload message")
    public void aValidTrainerWorkloadMessage() {
        request = new TrainerWorkloadRequestDto();

        request.setTrainerUsername("john.smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2026, 8, 23));
        request.setTrainingDuration(60);
        request.setActionType(ActionType.ADD);

        transactionId = "test-transaction-id";
    }

    @Given("a trainer workload message with invalid duration")
    public void aTrainerWorkloadMessageWithInvalidDuration() {
        request = new TrainerWorkloadRequestDto();

        request.setTrainerUsername("john.smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2026, 8, 23));

        // Invalid because trainingDuration has @Positive
        request.setTrainingDuration(0);

        request.setActionType(ActionType.ADD);

        transactionId = "test-transaction-id";
    }

    @And("the transaction id is missing")
    public void theTransactionIdIsMissing() {
        transactionId = null;
    }

    @When("the trainer workload message is consumed")
    public void theTrainerWorkloadMessageIsConsumed() {
        trainerWorkloadConsumer.consume(request, transactionId);
    }

    @When("the trainer workload message is validated")
    public void theTrainerWorkloadMessageIsValidated() {
        violations = validator.validate(request);
    }

    @Then("the trainer workload should be processed successfully")
    public void theTrainerWorkloadShouldBeProcessedSuccessfully() {
        verify(trainerWorkloadService)
                .processWorkload(request);
    }

    @Then("the trainer workload message should be invalid")
    public void theTrainerWorkloadMessageShouldBeInvalid() {
        assertThat(violations).isNotEmpty();

        assertThat(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals("trainingDuration"))
        ).isTrue();
    }

    @And("the trainer workload service should not be called")
    public void theTrainerWorkloadServiceShouldNotBeCalled() {
        verify(trainerWorkloadService, never())
                .processWorkload(request);
    }
}