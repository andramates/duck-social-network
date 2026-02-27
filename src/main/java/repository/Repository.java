package repository;

import domain.Entity;

import java.util.List;

public interface Repository<ID, T extends Entity<ID>> {
    T save(T entity);
    T findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
    boolean existsById(ID id);
    T update(T entity);

    default List<T> findPage(int page, int size) {
        throw new UnsupportedOperationException("Pagination not implemented");
    }

    default int count() {
        throw new UnsupportedOperationException("Count not implemented");
    }

}
