package org.summer.boot.repository;

import java.sql.SQLException;
import java.util.List;

public interface GenericRepository<T, R> {
    T findById(R id) throws SQLException;
    List<T> findAll() throws SQLException;
    T save(T entity) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(T entity) throws SQLException;
    void deleteById(R id) throws SQLException;
}
