package com.github.gogoasac.config;

import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.application.output.BookPersistence;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.application.service.BookManagementService;
import com.github.gogoasac.application.input.ReportingInput;
import com.github.gogoasac.application.service.ReportingService;
import com.github.gogoasac.infra.output.AuthorRepository;
import com.github.gogoasac.infra.output.BookRepository;
import com.github.gogoasac.infra.output.CollectionRepository;

public class DependencyOrchestrator {
    public static DependencyOrchestrator INSTANCE = new DependencyOrchestrator();

    private final BookPersistence bookPersistence;
    private final AuthorPersistence authorPersistence;
    private final CollectionPersistence collectionPersistence;

    private final BookManagementService bookManagementService;

    private final ReportingInput reportingInput;

    private DependencyOrchestrator() {
        this.bookPersistence = new BookRepository();
        this.authorPersistence = new AuthorRepository();
        this.collectionPersistence = new CollectionRepository();
        this.reportingInput = new ReportingService(collectionPersistence, bookPersistence, authorPersistence);

        this.bookManagementService = new BookManagementService(
            bookPersistence,
            authorPersistence,
            collectionPersistence
        );
    }

}
