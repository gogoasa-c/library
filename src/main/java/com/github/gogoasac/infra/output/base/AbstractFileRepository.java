package com.github.gogoasac.infra.output.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

public abstract class AbstractFileRepository<T> {
    private final String filePath;
    private final ObjectMapper objectMapper;
    private final TypeReference<List<T>> typeReference;
    private Long idGenerator;
    private final Function<T, Long> idExtractor;
    private final Logger logger;

    protected AbstractFileRepository(
        String filePath,
        TypeReference<List<T>> typeReference,
        Function<T, Long> idExtractor
    ) {
        this.filePath = filePath;
        this.objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .findAndAddModules()
            .build();
        this.typeReference = typeReference;
        this.idExtractor = idExtractor;
        createFileIfNotExists();
        this.idGenerator = getMaxId();
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    protected T save(T entity) {
        List<T> entities = readFromFile();
        T savedEntity = setId(entity, ++idGenerator);
        entities.add(savedEntity);
        writeToFile(entities);
        return savedEntity;
    }

    protected Optional<T> findById(Long id) {
        return readFromFile().stream()
            .filter(entity -> idExtractor.apply(entity).equals(id))
            .findFirst();
    }

    protected List<T> findAll() {
        return readFromFile();
    }

    /**
     * Update an existing entity identified by id by applying the updater function.
     * The updater may return a new instance; setId(...) will be used to ensure the persisted entity has the expected id.
     * Returns Optional.empty() when no entity with the given id exists.
     */
    protected Optional<T> updateById(final Long id, final Function<T, T> updater) {
        if (id == null) {
            return Optional.empty();
        }

        final List<T> entities = readFromFile();
        for (int idx = 0; idx < entities.size(); idx++) {
            final T current = entities.get(idx);
            final Long currentId = idExtractor.apply(current);
            if (currentId != null && currentId.equals(id)) {
                final T updatedCandidate = updater.apply(current);
                final T updatedWithId = setId(updatedCandidate, id);
                entities.set(idx, updatedWithId);
                writeToFile(entities);
                return Optional.of(updatedWithId);
            }
        }
        return Optional.empty();
    }

    private List<T> readFromFile() {
        try {
            return objectMapper.readValue(new File(filePath), typeReference);
        } catch (IOException e) {
            this.logger.severe(e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeToFile(List<T> entities) {
        try {
            objectMapper.writeValue(new File(filePath), entities);
        } catch (IOException e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException("Failed to write to file", e);
        }
    }

    private void createFileIfNotExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            writeToFile(new ArrayList<>());
        }
    }

    private long getMaxId() {
        return readFromFile().stream()
            .mapToLong(idExtractor::apply)
            .max()
            .orElse(0);
    }

    protected abstract T setId(T entity, Long id);
}
