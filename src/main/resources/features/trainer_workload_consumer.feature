Feature: Trainer workload message processing

  Scenario: Successfully process a valid trainer workload message
    Given a valid trainer workload message
    When the trainer workload message is consumed
    Then the trainer workload should be processed successfully

  Scenario: Process a trainer workload message without transaction id
    Given a valid trainer workload message
    And the transaction id is missing
    When the trainer workload message is consumed
    Then the trainer workload should be processed successfully

  Scenario: Reject a trainer workload message with invalid duration
    Given a trainer workload message with invalid duration
    When the trainer workload message is validated
    Then the trainer workload message should be invalid
    And the trainer workload service should not be called