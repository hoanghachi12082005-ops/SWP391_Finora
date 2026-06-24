//package com.storemanagement.dao.common;
//
//import com.storemanagement.util.database.DBContext;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public abstract class BaseDAO<T> implements ICrudDAO<T> {
//    protected Connection getConnection() throws SQLException {
//        return DBContext.getConnection();
//    }
//
//    @Override
//    public List<T> findAll() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public T findById(int id) {
//        return null;
//    }
//
//    @Override
//    public boolean insert(T item) {
//        return true;
//    }
//
//    @Override
//    public boolean update(T item) {
//        return true;
//    }
//
//    @Override
//    public boolean delete(int id) {
//        return true;
//    }
//}
