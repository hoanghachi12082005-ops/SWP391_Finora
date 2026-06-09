package com.storemanagement.service.inventory;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.inventory.InventoryItemDAO;
import com.storemanagement.model.InventoryItem;

public class InventoryItemService extends GenericService<InventoryItem> {
    public InventoryItemService() {
        super(new InventoryItemDAO());
    }
}
