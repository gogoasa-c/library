package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.domain.entity.Collection;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

/**
 * CLI menu handling collection-related interactions (add/list/view).
 *
 * <p>Concrete {@link MenuHandler} that presents the collection flows and
 * delegates data operations to an injected {@link CollectionManagementInput}.
 *
 * <p>Behavior:
 * - Validates input (non-empty names) and prints user-friendly messages.
 * - Lists items using {@link Collection#toString()} for consistent display.
 */
public final class CollectionMenu extends MenuHandler {
    private static final String MENU_NAME = "Collections";

    private final CollectionManagementInput collectionInput;

    public CollectionMenu(final PrintStream printStream,
                          final BufferedReader sharedReader,
                          final CollectionManagementInput collectionManagementInput) {
        super(MENU_NAME, printStream, sharedReader);

        super.setMenuItemList(List.of(
            new MenuItem("Add collection", this::addCollection),
            new MenuItem("List all collections", this::listAllCollections),
            new MenuItem("View collection by id", this::viewCollectionById)
        ));

        this.collectionInput = Objects.requireNonNull(collectionManagementInput, "collectionManagementInput");
    }

    private void addCollection() {
        final String name = super.readLine("Collection name: ").trim();
        if (name.isEmpty()) {
            super.printLine("Name cannot be empty");
            return;
        }
        try {
            final Collection created = collectionInput.addCollection(new AddCollectionCommand(name));
            super.printLine("Collection created: " + created);
        } catch (Exception e) {
            super.printLine("Failed to create collection: " + e.getMessage());
        }
    }

    private void listAllCollections() {
        final java.util.List<Collection> collections = collectionInput.getAll();
        if (collections == null || collections.isEmpty()) {
            super.printLine("No collections found.");
            return;
        }
        super.printLine("Collections:");
        collections.stream()
                   .map(c -> "  " + c)
                   .forEach(super::printLine);
    }

    private void viewCollectionById() {
        final Long id = super.readLong("Collection id: ");
        if (id == null) return;

        final Collection collection = collectionInput.getById(id);
        if (collection == null) {
            super.printLine("Collection not found.");
            return;
        }
        super.printLine("Collection: " + collection);
    }
}

