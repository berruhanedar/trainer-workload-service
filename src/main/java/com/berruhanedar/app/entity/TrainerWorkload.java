package com.berruhanedar.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_workloads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trainerUsername;

    @Column(nullable = false)
    private String trainerFirstName;

    @Column(nullable = false)
    private String trainerLastName;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "trainerWorkload", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<YearSummary> years = new ArrayList<>();
}