package com.berruhanedar.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "year_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_year", nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_workload_id", nullable = false)
    private TrainerWorkload trainerWorkload;

    @OneToMany(mappedBy = "yearSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonthSummary> months = new ArrayList<>();
}