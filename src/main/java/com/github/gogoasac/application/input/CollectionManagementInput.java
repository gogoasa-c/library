package com.github.gogoasac.application.input;

import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.domain.entity.Collection;

import java.util.List;

public interface CollectionManagementInput {
    Collection addCollection(AddCollectionCommand addCollectionCommand);
    List<Collection> getAll();
    Collection getById(Long id);
}
