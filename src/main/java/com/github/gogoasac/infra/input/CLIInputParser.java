package com.github.gogoasac.infra.input;

import com.github.gogoasac.application.dto.CollectionReport;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.input.ReportingInput;
import com.github.gogoasac.infra.input.menu.AuthorMenu;
import com.github.gogoasac.infra.input.menu.BookMenu;
import com.github.gogoasac.infra.input.menu.CollectionMenu;
import com.github.gogoasac.infra.input.reporting.ReportViewer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Simple terminal UI for the application. Keeps logic thin and delegates to application services.
 * The parser is IO-agnostic: input and output are injected so it can be tested easily.
 * Provides both a full constructor (recommended) and a no-arg convenience constructor
 * that delegates to the existing DependencyOrchestrator and System streams.
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
        while (true) {
            printMainMenu();
            String option = readLine("Select an option: ").trim();
            switch (option) {
                case "1" -> authorMenu.run();
                case "2" -> collectionMenu.run();
                case "3" -> bookMenu.run();
                case "4" -> handleReports();
                case "0" -> {
                    println("Exiting. Goodbye!");
                    return;
                }
                default -> println("Unknown option. Please choose a valid menu number.");
            }
            println("");
        }
    }

    private void printMainMenu() {
        println("=== Main Menu ===");
        println("1) Authors");
        println("2) Collections");
        println("3) Books");
        println("4) Generate Collection Reports (writes report_YYYY-MM-DD.txt)");
        println("0) Exit");
    }

    private void handleReports() {
        println("Generating collection reports...");
        try {
            final List<CollectionReport> reports = reportingInput.generateCollectionReports();
            println("Report generated. A file named report_YYYY-MM-DD.txt was written to the working directory.");
            println("Collections found: " + reports.size());

            if (!reports.isEmpty()) {
                final String open = readLine("Open report in GUI? (y/N): ").trim();
                if ("y".equalsIgnoreCase(open)) {
                    try {
                        this.reportViewer.showReports(reports);
                        println("Report viewer opened.");
                    } catch (ClassCastException ex) {
                        println("Unable to open GUI viewer: report format not recognized.");
                    } catch (Exception ex) {
                        println("Failed to open report viewer: " + ex.getMessage());
                    }
                } else {
                    println("Skipping GUI view.");
                }
            }
        } catch (Exception e) {
            println("Failed to generate reports: " + e.getMessage());
        }
    }

    private String readLine(String prompt) {
        sharedWriter.print(prompt);
        sharedWriter.flush();
        try {
            String line = sharedReader.readLine();
            return line == null ? "" : line;
        } catch (IOException e) {
            return "";
        }
    }

    private void println(String s) {
        sharedWriter.println(s);
    }
}

