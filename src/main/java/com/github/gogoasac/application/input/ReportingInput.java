package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.CollectionReport;

import java.io.IOException;
import java.util.List;

public interface ReportingInput {
    List<CollectionReport> generateCollectionReports() throws IOException;
}
