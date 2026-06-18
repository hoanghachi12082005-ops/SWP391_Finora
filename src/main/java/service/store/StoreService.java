package service.store;

import service.common.GenericService;

import dao.store.StoreDAO;
import model.Store;

public class StoreService extends GenericService<Store> {
    public StoreService() {
        super(new StoreDAO());
    }
}
