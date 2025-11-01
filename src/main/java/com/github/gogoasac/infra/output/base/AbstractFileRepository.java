package com.github.gogoasac.infra.output.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        this.objectMapper = new ObjectMapper();
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
