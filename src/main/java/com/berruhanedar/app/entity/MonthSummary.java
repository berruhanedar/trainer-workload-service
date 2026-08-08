package com.berruhanedar.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "month_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_month", nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer trainingSummaryDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_summary_id", nullable = false)
    private YearSummary yearSummary;
}