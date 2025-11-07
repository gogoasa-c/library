package com.github.gogoasac.infra.input;

import com.github.gogoasac.application.dto.CollectionReport;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.input.ReportingInput;
import com.github.gogoasac.common.JCFUtils;
import com.github.gogoasac.common.StringUtils;
import com.github.gogoasac.infra.input.menu.AuthorMenu;
import com.github.gogoasac.infra.input.menu.BookMenu;
import com.github.gogoasac.infra.input.menu.CollectionMenu;
import com.github.gogoasac.infra.input.reporting.ReportViewer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Simple terminal UI for the application. Keeps logic thin and delegates to application services.
 * The parser is IO-agnostic: input and output are injected so it can be tested easily.
 */
public final class CLIInputParser {
    private final AuthorManagementInput authorInput;
    private final BookManagementInput bookInput;
    private final CollectionManagementInput collectionInput;
    private final ReportingInput reportingInput;

    // single shared IO objects
    private final BufferedReader sharedReader;
    private final PrintStream sharedWriter;

    private final ReportViewer reportViewer;
    private final AuthorMenu authorMenu;
    private final CollectionMenu collectionMenu;
    private final BookMenu bookMenu;

    private static final String REPORT_GENERATED = "Report generated. A file named report_YYYY-MM-DD.txt was written to the working directory.";
    private static final String COLLECTIONS_FOUND = "Collections found: %d";
    private static final String PROMPT_OPEN_REPORT = "Open report in GUI? (y/N): ";
    private static final String OPEN_OPTION = "y";
    private static final String REPORT_VIEWER_OPENED = "Report viewer opened.";
    private static final String SKIP_GUI = "Skipping GUI view.";
    private static final String UNABLE_TO_OPEN = "Unable to open GUI viewer: report format not recognized.";
    private static final String FAILED_TO_OPEN = "Failed to open report viewer: %s";
    private static final String GENERATION_FAILED = "Failed to generate reports: %s";
    private static final String GENERATING_REPORTS = "Generating collection reports...";

    private static final String MAIN_MENU = """
        === Main Menu ===
        1) Authors
        2) Collections
        3) Books
        4) Generate Collection Reports (writes report_YYYY-MM-DD.txt)
        0) Exit\s
        """;
    private static final String SELECT_AN_OPTION = "Select an option: ";

    private static final String UNKNOWN_OPTION = "Unknown option. Please choose a valid menu number.";
    private static final String EXITING = "Exiting. Goodbye!";

    public CLIInputParser(
        AuthorManagementInput authorInput,
        BookManagementInput bookInput,
        CollectionManagementInput collectionInput,
        ReportingInput reportingInput,
        InputStream in,
        PrintStream out,
        ReportViewer reportViewer
    ) {
        this.authorInput = authorInput;
        this.bookInput = bookInput;
        this.collectionInput = collectionInput;
        this.reportingInput = reportingInput;

        this.sharedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.sharedWriter = out;

        this.reportViewer = reportViewer;

        this.authorMenu = new AuthorMenu(this.sharedWriter, this.sharedReader, this.authorInput);
        this.collectionMenu = new CollectionMenu(this.sharedWriter, this.sharedReader, this.collectionInput);
        this.bookMenu = new BookMenu(this.sharedWriter, this.sharedReader, this.bookInput, this.authorInput, this.collectionInput);
    }

    public void run() {
        println("Welcome to the Library TUI");

        boolean isRunning = true;

        while (isRunning) {
            printMainMenu();

            final String option = this.readLine(SELECT_AN_OPTION).trim();

            switch (option) {
                case "1" -> authorMenu.run();
                case "2" -> collectionMenu.run();
                case "3" -> bookMenu.run();
                case "4" -> handleReports();
                case "0" -> isRunning = false;
                default -> println(UNKNOWN_OPTION);
            }

            println();
        }

        println(EXITING);
    }

    private void printMainMenu() {
        println(MAIN_MENU);
    }

    private void handleReports() {
        println(GENERATING_REPORTS);

        try {
            final List<CollectionReport> reports = reportingInput.generateCollectionReports();

            printReportSummary(reports);

            if (JCFUtils.isEmptyOrNull(reports)) {
                return;
            }

            this.promptAndOpenViewer(reports);
        } catch (Exception e) {
            println(String.format(GENERATION_FAILED, e.getMessage()));
        }
    }

    private void printReportSummary(final List<CollectionReport> reports) {
        final int count = Optional.ofNullable(reports).map(List::size).orElse(0);
        println(REPORT_GENERATED);
        println(String.format(COLLECTIONS_FOUND, count));
    }

    private void promptAndOpenViewer(final List<CollectionReport> reports) {
        final String answer = readLine(PROMPT_OPEN_REPORT).trim();

        if (!OPEN_OPTION.equalsIgnoreCase(answer)) {
            println(SKIP_GUI);

            return;
        }

        try {
            reportViewer.showReports(reports);
            println(REPORT_VIEWER_OPENED);
        } catch (ClassCastException ex) {
            println(UNABLE_TO_OPEN);
        } catch (Exception ex) {
            println(String.format(FAILED_TO_OPEN, ex.getMessage()));
        }
    }

    private String readLine(final String prompt) {
        this.sharedWriter.print(prompt);
        this.sharedWriter.flush();

        try {
            final String line = sharedReader.readLine();

            return StringUtils.orElse(() -> line, StringUtils.EMPTY_STRING);
        } catch (IOException e) {
            return StringUtils.EMPTY_STRING;
        }
    }

    private void println(final String s) {
        this.sharedWriter.println(s);
    }

    private void println() {
        this.sharedWriter.println();
    }
}
