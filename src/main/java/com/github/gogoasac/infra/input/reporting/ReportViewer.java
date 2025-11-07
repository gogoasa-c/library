package com.github.gogoasac.infra.input.reporting;

import com.github.gogoasac.application.dto.CollectionReport;

import java.util.List;

public interface ReportViewer {
    void showReports(final List<CollectionReport> reports);
}
