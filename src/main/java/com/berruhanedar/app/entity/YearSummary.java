package com.berruhanedar.app.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {

    private Integer year;

    private List<MonthSummary> months = new ArrayList<>();
}