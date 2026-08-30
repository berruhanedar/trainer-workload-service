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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

public class TrainerWorkloadConsumerSteps {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    private TrainerWorkloadConsumer trainerWorkloadConsumer;
    private TrainerWorkloadRequestDto request;
    private String transactionId;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        trainerWorkloadConsumer = new TrainerWorkloadConsumer(trainerWorkloadService);
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

    @And("the transaction id is missing")
    public void theTransactionIdIsMissing() {
        transactionId = null;
    }

    @When("the trainer workload message is consumed")
    public void theTrainerWorkloadMessageIsConsumed() {
        trainerWorkloadConsumer.consume(request, transactionId);
    }

    @Then("the trainer workload should be processed successfully")
    public void theTrainerWorkloadShouldBeProcessedSuccessfully() {
        verify(trainerWorkloadService)
                .processWorkload(request);
    }
}