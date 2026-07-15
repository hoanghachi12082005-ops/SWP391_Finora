package controller.product;

import controller.common.BaseController;
import dao.product.ProductDAO;
import model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

@WebServlet(name = "ProductController", urlPatterns = {"/products"})
@MultipartConfig(
        fileSizeThreshold = 3 * 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class ProductController extends BaseController {
    private ProductDAO productDAO;
    private static final int ITEMS_PER_PAGE = 5;
    private static final long MAX_IMAGE_SIZE = 3L * 1024 * 1024; // 3MB
    private static final String IMAGE_DIR = "/assets/images/product/";

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword  = request.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }
        String status   = request.getParameter("status");
        if (status != null) {
            status = status.trim().toUpperCase();
            if (status.isEmpty() || (!"ACTIVE".equals(status) && !"INACTIVE".equals(status))) {
                status = null;
            }
        }
        String categoryParam = request.getParameter("categoryID");
        String unitParam = request.getParameter("unitID");

        Integer categoryID = null;
        Integer unitID = null;
        try {
            if (categoryParam != null && !categoryParam.isBlank()) {
                categoryID = Integer.parseInt(categoryParam.trim());
            }
            if (unitParam != null && !unitParam.isBlank()) {
                unitID = Integer.parseInt(unitParam.trim());
            }
        } catch (NumberFormatException ignored) {}

        int page = 1;
        try {
            if (request.getParameter("page") != null)
                page = Integer.parseInt(request.getParameter("page").trim());
        } catch (NumberFormatException ignored) {}
        try {
            int totalCount = productDAO.getTotalCount(keyword, status, categoryID, unitID);
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

            List<Product> products = productDAO.findAll((page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE, keyword, status, categoryID, unitID);
            // ImageUrl đã được load từ DB trong ProductDAO (cột ImageUrl + bảng product_image)

            request.setAttribute("products",    products);
            request.setAttribute("categories",  productDAO.findAllCategories());
            request.setAttribute("units",       productDAO.findAllUnits());
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages",  totalPages);
            request.setAttribute("keyword",     keyword != null ? keyword : "");
            request.setAttribute("filterStatus",status != null ? status : "");
            request.setAttribute("filterCategoryID", categoryID);
            request.setAttribute("filterUnitID", unitID);

            forward(request, response, "products/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Database error retrieving products", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        try {
            String action = request.getParameter("action");
            if (action == null) action = "";
            if ("add".equals(action)) {
                Product p = buildProductFromRequest(request);
                Part imagePart = safeGetPart(request, "imageFile");

                // verify image
                String verifyError = (imagePart != null && imagePart.getSize() > 0)
                        ? verifyImage(imagePart) : null;
                if (verifyError != null) {
                    session.setAttribute("message", verifyError);
                    session.setAttribute("messageType", "danger");
                    response.sendRedirect(buildRedirectUrl(request));
                    return;
                }

                int newId = productDAO.insert(p);

                if (newId > 0 && imagePart != null && imagePart.getSize() > 0) {
                    try {
                        String savedPath = saveProductImageFile(request, imagePart, newId);
                        String imageUrl = request.getContextPath() + IMAGE_DIR + savedPath;
                        p.setProductID(newId);
                        // Lưu dạng JSON array: ["url"]
                        p.setImageUrl("[\"" + imageUrl + "\"]");
                        productDAO.update(p);
                    } catch (IOException ioe) {
                        session.setAttribute("message", "Thêm thành công, nhưng lưu ảnh thất bại: " + ioe.getMessage());
                        session.setAttribute("messageType", "warning");
                        response.sendRedirect(buildRedirectUrl(request));
                        return;
                    }
                }
                session.setAttribute("message", "Thêm sản phẩm thành công!");
                session.setAttribute("messageType", "success");
            } else if ("edit".equals(action)) {
                int productID = Integer.parseInt(request.getParameter("productID"));
                Product p = buildProductFromRequest(request);
                p.setProductID(productID);

                Part imagePart = safeGetPart(request, "imageFile");
                String verifyError = (imagePart != null && imagePart.getSize() > 0)
                        ? verifyImage(imagePart) : null;
                if (verifyError != null) {
                    session.setAttribute("message", verifyError);
                    session.setAttribute("messageType", "danger");
                    response.sendRedirect(buildRedirectUrl(request));
                    return;
                }

                if (imagePart != null && imagePart.getSize() > 0) {
                    try {
                        // Xoá ảnh cũ trên ổ cứng
                        deleteProductImageFiles(request, productID);
                        // Lưu file ảnh mới
                        String savedPath = saveProductImageFile(request, imagePart, productID);
                        String imageUrl = request.getContextPath() + IMAGE_DIR + savedPath;
                        // Lưu dạng JSON array: ["url"]
                        p.setImageUrl("[\"" + imageUrl + "\"]");
                    } catch (IOException ioe) {
                        session.setAttribute("message", "Cập nhật thành công, nhưng lưu ảnh thất bại: " + ioe.getMessage());
                        session.setAttribute("messageType", "warning");
                        response.sendRedirect(buildRedirectUrl(request));
                        return;
                    }
                } else {
                    // Giữ nguyên ảnh cũ — lấy từ DB
                    Product old = productDAO.findById(productID);
                    if (old != null) p.setImageUrl(old.getImageUrl());
                }

                productDAO.update(p);
                session.setAttribute("message", "Cập nhật sản phẩm thành công!");
                session.setAttribute("messageType", "success");
            } else if ("delete".equals(action)) {
                try {
                    int id = Integer.parseInt(request.getParameter("id"));
                    // Xoá file ảnh trên ổ cứng
                    deleteProductImageFiles(request, id);
                    // Xoá DB (product_image cascade qua FK + DAO delete đã xử lý)
                    productDAO.delete(id);
                    session.setAttribute("message", "Xóa sản phẩm thành công!");
                    session.setAttribute("messageType", "success");
                } catch (Exception e) {
                    e.printStackTrace();
                    session.setAttribute("message", "Không thể xóa sản phẩm này do đang có dữ liệu liên quan!");
                    session.setAttribute("messageType", "danger");
                }
            }


            response.sendRedirect(buildRedirectUrl(request));
        } catch (Exception e) {
            throw new ServletException("Error processing request", e);
        }
    }

    private Product buildProductFromRequest(HttpServletRequest request) {
        Product p = new Product();
        p.setCategoryID(Integer.parseInt(request.getParameter("categoryID")));
        p.setName(request.getParameter("name"));
        p.setUnitID(Integer.parseInt(request.getParameter("unitID")));
        p.setSellingPrice(new BigDecimal(request.getParameter("sellingPrice")));
        p.setStatus(request.getParameter("status"));
        return p;
    }

    private String buildRedirectUrl(HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }
        String status  = request.getParameter("filterStatus");
        String categoryID = request.getParameter("filterCategoryID");
        String unitID = request.getParameter("filterUnitID");
        String page    = request.getParameter("page");
        StringBuilder redirect = new StringBuilder(request.getContextPath() + "/products?");
        if (keyword != null && !keyword.isBlank()) redirect.append("keyword=").append(keyword).append("&");
        if (status  != null && !status.isBlank())  redirect.append("status=").append(status).append("&");
        if (categoryID  != null && !categoryID.isBlank())  redirect.append("categoryID=").append(categoryID).append("&");
        if (unitID  != null && !unitID.isBlank())  redirect.append("unitID=").append(unitID).append("&");
        if (page    != null && !page.isBlank())    redirect.append("page=").append(page);
        if (redirect.charAt(redirect.length() - 1) == '&' || redirect.charAt(redirect.length() - 1) == '?') {
            redirect.deleteCharAt(redirect.length() - 1);
        }
        return redirect.toString();
    }

    private Part safeGetPart(HttpServletRequest request, String name) {
        try {
            return request.getPart(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Xác thực ảnh thật bằng ImageIO + giới hạn 3MB.
     * Trả về null nếu hợp lệ; ngược lại trả về message lỗi.
     */
    private String verifyImage(Part imagePart) {
        if (imagePart.getSize() > MAX_IMAGE_SIZE) {
            return "Ảnh vượt quá dung lượng cho phép (tối đa 3MB).";
        }
        try (InputStream in = imagePart.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(in)) {
            if (iis == null) {
                return "Không thể đọc dữ liệu ảnh tải lên.";
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers == null || !readers.hasNext()) {
                return "File tải lên không phải ảnh hợp lệ.";
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0) {
                    return "Ảnh không hợp lệ (kích thước không xác định).";
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return "Lỗi khi xác thực ảnh: " + e.getMessage();
        }
        return null;
    }

    private String resolveExtension(Part imagePart) {
        String submitted = imagePart.getSubmittedFileName();
        if (submitted != null) {
            int dot = submitted.lastIndexOf('.');
            if (dot >= 0 && dot < submitted.length() - 1) {
                String ext = submitted.substring(dot + 1).toLowerCase();
                if (ext.matches("[a-z0-9]{1,5}")) return ext;
            }
        }
        String ct = imagePart.getContentType();
        if (ct != null) {
            ct = ct.toLowerCase();
            if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";
            if (ct.contains("png"))  return "png";
            if (ct.contains("webp")) return "webp";
            if (ct.contains("gif"))  return "gif";
            if (ct.contains("bmp"))  return "bmp";
        }
        return "img";
    }

    private File ensureImageDir(HttpServletRequest request) {
        String real = request.getServletContext().getRealPath(IMAGE_DIR);
        File dir = new File(real);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /**
     * Lưu file ảnh xuống ổ cứng.
     * @return tên file đã lưu (vd: "product_1_1712345678.jpg")
     */
    private String saveProductImageFile(HttpServletRequest request, Part imagePart, int productId) throws IOException {
        File dir = ensureImageDir(request);
        String ext = resolveExtension(imagePart);
        String uniqueName = "product_" + productId + "_" + System.currentTimeMillis() + "." + ext;
        File target = new File(dir, uniqueName);
        try (InputStream in = imagePart.getInputStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return uniqueName;
    }

    /** Xoá tất cả file ảnh của sản phẩm trong thư mục upload */
    private void deleteProductImageFiles(HttpServletRequest request, int productId) {
        File dir = ensureImageDir(request);
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles((d, name) -> {
            return name.toLowerCase().startsWith("product_" + productId + "_");
        });
        if (files != null) {
            for (File f : files) {
                try { f.delete(); } catch (Exception ignored) {}
            }
        }
    }
}
