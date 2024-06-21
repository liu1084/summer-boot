package org.summer.boot.repository.impl;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import org.summer.boot.repository.GenericRepository;

import java.sql.SQLException;
import java.util.List;

public class ORMLiteRepository<T, R> implements GenericRepository<T, R> {
    private final Dao<T, R> dao;

    public ORMLiteRepository(ConnectionSource connectionSource, Class<T> type) throws SQLException {
        this.dao = DaoManager.createDao(connectionSource, type);
    }

    @Override
    public T findById(R id) throws SQLException {
        return dao.queryForId(id);
    }

    @Override
    public List<T> findAll() throws SQLException {
        return dao.queryForAll();
    }

    @Override
    public T save(T entity) throws SQLException {
        dao.create(entity);
        return entity;
    }

    @Override
    public void update(T entity) throws SQLException {
        dao.update(entity);
    }

    @Override
    public void delete(T entity) throws SQLException {
        dao.delete(entity);
    }

    @Override
    public void deleteById(R id) throws SQLException {
        dao.deleteById(id);
    }
}

