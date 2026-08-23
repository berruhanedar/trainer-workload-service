package com.berruhanedar.app.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workloads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    private String id;

    private String trainerUsername;

    private String trainerFirstName;

    private String trainerLastName;

    private Boolean status;

    private List<YearSummary> years = new ArrayList<>();
}