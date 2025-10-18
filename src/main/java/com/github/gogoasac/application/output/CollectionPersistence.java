package com.github.gogoasac.application.output;

import com.github.gogoasac.domain.entity.Collection;

import java.util.List;
import java.util.Optional;

public interface CollectionPersistence {
    Collection save(Collection collection);

    Optional<Collection> findById(Long id);

    List<Collection> findAll();
}
