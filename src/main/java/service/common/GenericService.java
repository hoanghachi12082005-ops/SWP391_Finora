//package       service.common;
//
//import       dao.common.ICrudDAO;
//import java.util.List;
//
//public class GenericService<T> {
//    protected ICrudDAO<T> dao;
//
//    public GenericService(ICrudDAO<T> dao) {
//        this.dao = dao;
//    }
//
//    public List<T> findAll() { return dao.findAll(); }
//    public T findById(int id) { return dao.findById(id); }
//    public boolean insert(T item) { return dao.insert(item); }
//    public boolean update(T item) { return dao.update(item); }
//    public boolean delete(int id) { return dao.delete(id); }
//}
