package com.berruhanedar.app.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.assertj.core.api.Assertions.assertThat;

public class SmokeSteps {

    @Given("the Trainer Workload test environment is available")
    public void trainerWorkloadTestEnvironmentIsAvailable() {
        assertThat(true).isTrue();
    }

    @Then("Cucumber should run successfully")
    public void cucumberShouldRunSuccessfully() {
        assertThat(true).isTrue();
    }
}