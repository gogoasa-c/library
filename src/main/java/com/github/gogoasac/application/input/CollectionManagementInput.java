package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.domain.entity.Collection;

import java.util.List;

/**
 * Input port for collection-related operations exposed by the application layer.
 *
 * <p>Defines a minimal, testable API for creating and retrieving collection entities.
 */
public interface CollectionManagementInput {
    Collection addCollection(AddCollectionCommand addCollectionCommand);
    List<Collection> getAll();
    Collection getById(Long id);
}
