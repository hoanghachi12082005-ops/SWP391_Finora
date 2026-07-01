package service.common;

import java.util.List;

public class GenericService<T> {
    protected Object dao;

    public GenericService(Object dao) {
        this.dao = dao;
    }

    public List<T> findAll() { throw new UnsupportedOperationException("Not implemented"); }
    public T findById(int id) { throw new UnsupportedOperationException("Not implemented"); }
    public boolean insert(T item) { throw new UnsupportedOperationException("Not implemented"); }
    public boolean update(T item) { throw new UnsupportedOperationException("Not implemented"); }
    public boolean delete(int id) { throw new UnsupportedOperationException("Not implemented"); }
}
