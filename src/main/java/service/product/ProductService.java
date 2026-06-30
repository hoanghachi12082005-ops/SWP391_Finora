package service.product;

import service.common.GenericService;

import dao.product.ProductDAO;
import model.Product;

public class ProductService extends GenericService<Product> {
    public ProductService() {
        super(new ProductDAO());
    }
}
