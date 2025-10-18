package com.github.gogoasac.infra.output;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Collection;
import com.github.gogoasac.infra.output.base.AbstractFileRepository;

import java.util.List;
import java.util.Optional;

public class CollectionRepository extends AbstractFileRepository<Collection> implements CollectionPersistence {
    private static final String FILE_PATH = "Collections.json";

    public CollectionRepository() {
        super(FILE_PATH, new TypeReference<>() {}, Collection::id);
    }

    @Override
    public Collection save(Collection collection) {
        return super.save(collection);
    }

    @Override
    public Optional<Collection> findById(Long id) {
        return super.findById(id);
    }

    @Override
    public List<Collection> findAll() {
        return super.findAll();
    }

    @Override
    protected Collection setId(Collection collection, Long id) {
        return new Collection(id, collection.name());
    }
}
