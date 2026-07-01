package       controller.inventory;

import       controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "InventoryController", urlPatterns = {"/inventory/dashboard", "/inventory/import", "/inventory/export", "/inventory/transfer", "/inventory/report", "/inventory/adjustment"})
public class InventoryController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
        case "/inventory/dashboard": forward(request, response, "inventory/dashboard"); break;
        case "/inventory/import":
            try {
                service.supplier.SupplierService supplierService = new service.supplier.SupplierService();
                java.util.List<model.Supplier> activeSuppliers = supplierService.getSuppliersPaging("", "active", 1, 1000);
                
                dao.product.ProductDAO productDAO = new dao.product.ProductDAO();
                java.util.List<model.Product> activeProducts = productDAO.findAll(0, 1000, "", "active", null, null);
                
                service.supplier.SupplierProductService spService = new service.supplier.SupplierProductService();
                java.util.Map<Integer, java.util.Map<Integer, Double>> mapping = spService.getSupplierProductMap();
                
                StringBuilder jsMap = new StringBuilder("{");
                boolean firstSup = true;
                for (java.util.Map.Entry<Integer, java.util.Map<Integer, Double>> entry : mapping.entrySet()) {
                    if (!firstSup) jsMap.append(",");
                    firstSup = false;
                    jsMap.append(entry.getKey()).append(":{");
                    boolean firstProd = true;
                    for (java.util.Map.Entry<Integer, Double> subEntry : entry.getValue().entrySet()) {
                        if (!firstProd) jsMap.append(",");
                        firstProd = false;
                        jsMap.append(subEntry.getKey()).append(":").append(subEntry.getValue());
                    }
                    jsMap.append("}");
                }
                jsMap.append("}");
                
                request.setAttribute("activeSuppliers", activeSuppliers);
                request.setAttribute("activeProducts", activeProducts);
                request.setAttribute("supplierProductsJson", jsMap.toString());
                
                forward(request, response, "inventory/import");
            } catch (Exception e) {
                e.printStackTrace();
                forward(request, response, "inventory/dashboard");
            }
            break;
        case "/inventory/export": forward(request, response, "inventory/export"); break;
        case "/inventory/transfer": forward(request, response, "inventory/transfer"); break;
        case "/inventory/report": forward(request, response, "inventory/report"); break;
        case "/inventory/adjustment": forward(request, response, "products/stock-adjustment"); break;
            default: forward(request, response, "inventory/dashboard"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }
}
