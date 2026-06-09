package com.storemanagement.service.product;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.product.CategoryDAO;
import com.storemanagement.model.Category;

public class CategoryService extends GenericService<Category> {
    public CategoryService() {
        super(new CategoryDAO());
    }
}
