package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.CollectionReport;
import java.util.List;

/**
 * Reporting input port.
 *
 * <p>Defines the contract used by upper layers (CLI, GUI, tests) to request
 * generation of collection reports. Implementations are responsible for
 * gathering data from persistence ports, building presentation-ready
 * {@link com.github.gogoasac.application.dto.CollectionReport} objects and
 * producing any side-effects such as writing a textual report file.
 *
 * <p>Semantics:
 * - Implementations should return an immutable list of CollectionReport.
 * - Implementations may write files or open viewers as a side-effect but must
 *   keep the returned model detached from I/O concerns so it can be tested.
 */
public interface ReportingInput {
    List<CollectionReport> generateCollectionReports();
}
