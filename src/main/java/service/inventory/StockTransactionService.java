package com.storemanagement.service.inventory;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.inventory.StockTransactionDAO;
import com.storemanagement.model.StockTransaction;

public class StockTransactionService extends GenericService<StockTransaction> {
    public StockTransactionService() {
        super(new StockTransactionDAO());
    }
}
