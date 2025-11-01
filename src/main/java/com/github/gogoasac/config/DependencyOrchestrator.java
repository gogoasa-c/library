package com.github.gogoasac.config;

import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.input.ReportingInput;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.application.output.BookPersistence;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.application.service.AuthorManagementService;
import com.github.gogoasac.application.service.BookManagementService;
import com.github.gogoasac.application.service.CollectionManagementService;
import com.github.gogoasac.application.service.ReportingService;
import com.github.gogoasac.infra.input.CLIInputParser;
import com.github.gogoasac.infra.output.AuthorRepository;
import com.github.gogoasac.infra.output.BookRepository;
import com.github.gogoasac.infra.output.CollectionRepository;

public class DependencyOrchestrator {
    public static final DependencyOrchestrator INSTANCE = new DependencyOrchestrator();

    private final BookPersistence bookPersistence;
    private final AuthorPersistence authorPersistence;
    private final CollectionPersistence collectionPersistence;

    public final AuthorManagementInput authorManagementInput;
    public final BookManagementInput bookManagementInput;
    public final CollectionManagementInput collectionManagementInput;
    public final ReportingInput reportingInput;

    private final CLIInputParser cliInputParser;

    private DependencyOrchestrator() {
        this.bookPersistence = new BookRepository();
        this.authorPersistence = new AuthorRepository();
        this.collectionPersistence = new CollectionRepository();

        this.authorManagementInput = new AuthorManagementService(authorPersistence);
        this.collectionManagementInput = new CollectionManagementService(collectionPersistence);
        this.bookManagementInput = new BookManagementService(
            bookPersistence,
            authorPersistence,
            collectionPersistence
        );
        this.reportingInput = new ReportingService(collectionPersistence, bookPersistence, authorPersistence);

        this.cliInputParser = new CLIInputParser(
            this.authorManagementInput,
            this.bookManagementInput,
            this.collectionManagementInput,
            this.reportingInput,
            System.in,
            System.out
        );
    }

    public static void run() {
        INSTANCE.cliInputParser.run();
    }

}
