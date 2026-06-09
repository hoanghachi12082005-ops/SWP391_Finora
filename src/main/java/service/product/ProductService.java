package com.storemanagement.service.product;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.product.ProductDAO;
import com.storemanagement.model.Product;

public class ProductService extends GenericService<Product> {
    public ProductService() {
        super(new ProductDAO());
    }
}
