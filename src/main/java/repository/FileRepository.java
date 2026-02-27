package repository;

import domain.Entity;
import exceptions.RepositoryException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class FileRepository<ID, T extends Entity<ID>> extends InMemoryRepository<ID, T> {
    protected final String fileName;

    public FileRepository(String fileName) {
        this.fileName = fileName;
        loadFromFile();
    }

    private void loadFromFile() {
        entities.clear();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                T entity = lineToEntity(line);
                if (entity != null) super.save(entity);
            }
        } catch (IOException e)  {
            throw new RepositoryException(e.getMessage());
        }
    }

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileName, false), StandardCharsets.UTF_8))) {
            for (T entity : findAll()) {
                bw.write(entityToLine(entity));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    protected abstract T lineToEntity(String line);
    protected abstract String entityToLine(T entity);

    @Override
    public T save(T entity) {
//        loadFromFile();
        T result = super.save(entity);
        saveToFile();
        return result;
    }

    @Override
    public void deleteById(ID id) {
//        loadFromFile();
        super.deleteById(id);
        saveToFile();
    }

    @Override
    public T update(T entity) {
//        loadFromFile();
        T result = super.update(entity);
        saveToFile();
        return result;
    }
}
