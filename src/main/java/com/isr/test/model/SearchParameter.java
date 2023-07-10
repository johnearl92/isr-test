package com.isr.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchParameter {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> attribute1;
    private List<String> attribute2;
    private List<String> attribute3;
    private List<String> attribute4;
}
