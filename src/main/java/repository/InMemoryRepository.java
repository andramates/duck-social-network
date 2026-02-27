package repository;

import domain.Entity;
import exceptions.RepositoryException;
import validation.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryRepository<ID, T extends Entity<ID>> implements Repository<ID, T> {
    protected final Map<ID, T> entities  = new HashMap<>();

    @Override
    public T save(T entity) {
        ID id = entity.getId();

        if (entities.containsKey(id)) throw new RepositoryException("Entity id already exists");
        entities.put(id, entity);
        return entity;
    }

    @Override
    public T findById(ID id) {
        if (!existsById(id)) throw new RepositoryException("Entity id not found");
        return entities.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(entities.values());
    }

    @Override
    public void deleteById(ID id) {
        if (!entities.containsKey(id)) throw new RepositoryException("Entity id does not exist");
        entities.remove(id);
    }

    @Override
    public boolean existsById(ID id) {
        return entities.containsKey(id);
    }

    @Override
    public T update(T entity) {
        ID id = entity.getId();
        if (!entities.containsKey(id)) throw new RepositoryException("Entity id does not exist");
        entities.put(id, entity);
        return entity;
    }

}
