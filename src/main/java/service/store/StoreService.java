package com.storemanagement.service.store;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.store.StoreDAO;
import com.storemanagement.model.Store;

public class StoreService extends GenericService<Store> {
    public StoreService() {
        super(new StoreDAO());
    }
}
