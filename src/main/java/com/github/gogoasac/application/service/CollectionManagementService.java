package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Collection;

import java.util.List;

public record CollectionManagementService(
    CollectionPersistence collectionPersistence) implements CollectionManagementInput {

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