package dao.common;

import java.util.List;

public interface ICrudDAO<T> {
    List<T> findAll();
    T findById(int id);
    boolean insert(T item);
    boolean update(T item);
    boolean delete(int id);
}
