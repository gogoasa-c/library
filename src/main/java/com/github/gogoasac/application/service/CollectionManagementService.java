package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Collection;

import java.util.List;

/**
 * Application service responsible for collection management.
 *
 * <p>Coordinates creation and retrieval of collections, delegating persistence
 * concerns to {@link CollectionPersistence} and keeping business rules in one place.
 */
public final class CollectionManagementService implements CollectionManagementInput {
    private final CollectionPersistence collectionPersistence;

    public CollectionManagementService(CollectionPersistence collectionPersistence) {
        this.collectionPersistence = collectionPersistence;
    }

    @Override
    public Collection addCollection(AddCollectionCommand addCollectionCommand) {
        Collection collection = new Collection(null, addCollectionCommand.name());
        return collectionPersistence.addCollection(collection);
    }

    @Override
    public List<Collection> getAll() {
        return collectionPersistence.findAll();
    }

    @Override
    public Collection getById(Long id) {
        return collectionPersistence.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Collection with ID " + id + " does not exist."));
    }
}