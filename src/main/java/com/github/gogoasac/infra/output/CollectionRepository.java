package com.github.gogoasac.infra.output;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Collection;
import com.github.gogoasac.infra.output.base.AbstractFileRepository;

import java.util.List;
import java.util.Optional;

/**
 * File-backed repository for Collection entities.
 *
 * <p>Uses {@link AbstractFileRepository} for file-based JSON persistence. This
 * class provides the minimal surface required by {@link CollectionPersistence}
 * and is intended to be used by application services to create, list and look
 * up collection data used in listings and reports.
 *
 * <p>Notes:
 * - Persistence concerns such as file paths and id generation are handled by
 *   the base class; business validation belongs in the service layer.
 */
public class CollectionRepository extends AbstractFileRepository<Collection> implements CollectionPersistence {
    private static final String FILE_PATH = "Collections.json";

    public CollectionRepository() {
        super(FILE_PATH, new TypeReference<>() {}, Collection::id);
    }

    public CollectionRepository(final String filePath) {
        super(filePath, new TypeReference<>() {}, Collection::id);
    }

    @Override
    public Collection addCollection(Collection collection) {
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
