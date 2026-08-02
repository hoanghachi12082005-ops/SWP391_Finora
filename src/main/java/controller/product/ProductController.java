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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "ProductController", urlPatterns = {"/products"})
@MultipartConfig(
        fileSizeThreshold = 3 * 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class ProductController extends BaseController {
    private ProductDAO productDAO;
    private static final int ITEMS_PER_PAGE = 5;
    private static final int MAX_IMAGE_SIZE = 3 * 1024 * 1024; // 3MB
    private static final int MAX_IMAGE_DIMENSION = 5000;        // 5000px tối đa mỗi chiều
    private static final String IMAGE_DIR = "/assets/images/product/";

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        if (keyword != null) keyword = keyword.trim().replaceAll("\\s+", " ");

        String status = request.getParameter("status");
        if (status != null) {
            status = status.trim().toUpperCase();
            if (status.isEmpty() || (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)))
                status = null;
        }

        Integer categoryID = parseIntParam(request.getParameter("categoryID"), null);
        Integer unitID = parseIntParam(request.getParameter("unitID"), null);
        int page = parseIntParam(request.getParameter("page"), 1);

        try {
            int totalCount = productDAO.getTotalCount(keyword, status, categoryID, unitID);
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

            List<Product> products = productDAO.findAll((page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE, keyword, status, categoryID, unitID);
            attachProductImageUrls(request, products);

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

    private void attachProductImageUrls(HttpServletRequest request, List<Product> products) {
        if (products == null || products.isEmpty()) return;
        
        String ctx = request.getContextPath();
        String real = request.getServletContext().getRealPath("/assets/images/product/");
        java.io.File dir = (real != null) ? new java.io.File(real) : null;
        java.io.File[] files = (dir != null && dir.exists()) ? dir.listFiles() : null;

        for (Product item : products) {
            List<String> list = item.getImageUrlList();
            boolean fileExists = false;
            if (!list.isEmpty()) {
                String firstUrl = list.get(0);
                if (firstUrl != null && !firstUrl.isBlank()) {
                    String cleanUrl = model.Product.normalizeUrl(firstUrl);
                    String physicalPath = request.getServletContext().getRealPath(cleanUrl);
                    if (physicalPath != null) {
                        java.io.File physicalFile = new java.io.File(physicalPath);
                        if (physicalFile.exists() && physicalFile.isFile()) {
                            fileExists = true;
                        }
                    }
                }
            }

            if (fileExists) {
                List<String> formatted = new java.util.ArrayList<>();
                for (String url : list) {
                    formatted.add(model.Product.formatDisplayUrl(url, ctx));
                }
                item.setImageUrlList(formatted);
            } else if (files != null) {
                String prefix = "product_" + item.getProductID() + "_";
                String fallbackPrefix = "product_" + item.getProductID() + ".";
                for (java.io.File f : files) {
                    String name = f.getName().toLowerCase();
                    if (f.isFile() && (name.startsWith(prefix.toLowerCase()) || name.startsWith(fallbackPrefix.toLowerCase()))) {
                        item.setImageUrlList(java.util.Collections.singletonList(ctx + "/assets/images/product/" + f.getName()));
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        model.Employee currentUser = (model.Employee) session.getAttribute("currentUser");
        String roleName = (currentUser != null && currentUser.getRoleName() != null) ? currentUser.getRoleName().trim() : "";
        boolean canManage = "Admin".equalsIgnoreCase(roleName) || "Owner".equalsIgnoreCase(roleName);
        if (!canManage) {
            session.setAttribute("message", "Bạn không có quyền thực hiện thao tác này.");
            session.setAttribute("messageType", "danger");
            response.sendRedirect(buildRedirectUrl(request));
            return;
        }
        try {
            String action = request.getParameter("action");
            if (action == null) action = "";
            if ("add".equals(action)) {
                Product p = buildProductFromRequest(request);
                List<Part> imageParts = getImageParts(request);

                String imgError = verifyImages(imageParts);
                if (imgError != null) {
                    session.setAttribute("message", imgError);
                    session.setAttribute("messageType", "danger");
                    response.sendRedirect(buildRedirectUrl(request));
                    return;
                }

                int newId = productDAO.insert(p);

                if (newId > 0 && !imageParts.isEmpty()) {
                    try {
                        List<String> savedUrls = new ArrayList<>();
                        for (Part part : imageParts) {
                            String savedPath = saveProductImageFile(request, part, newId);
                            savedUrls.add(request.getContextPath() + IMAGE_DIR + savedPath);
                        }
                        p.setProductID(newId);
                        p.setImageUrlList(savedUrls);
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

                List<Part> imageParts = getImageParts(request);

                String imgError = verifyImages(imageParts);
                if (imgError != null) {
                    session.setAttribute("message", imgError);
                    session.setAttribute("messageType", "danger");
                    response.sendRedirect(buildRedirectUrl(request));
                    return;
                }

                if (!imageParts.isEmpty() || 
                        (request.getParameter("deletedImages") != null && !request.getParameter("deletedImages").isBlank())) {
                     try {
                         // Lấy ảnh cũ từ DB
                         Product old = productDAO.findById(productID);
                         List<String> existingUrls = (old != null) ? old.getImageUrlList() : new ArrayList<>();

                         // Xoá các ảnh được đánh dấu
                         String deletedJson = request.getParameter("deletedImages");
                         if (deletedJson != null && !deletedJson.isBlank()) {
                             List<String> toDelete = Product.parseJsonArray(deletedJson);
                             for (String url : toDelete) {
                                 deleteImageFileByUrl(request, url);
                             }
                             existingUrls.removeAll(toDelete);
                         }

                         // Nếu có upload mới, thêm vào danh sách ảnh hiện tại (không xoá ảnh cũ)
                         if (!imageParts.isEmpty()) {
                             for (Part part : imageParts) {
                                 String savedPath = saveProductImageFile(request, part, productID);
                                 existingUrls.add(request.getContextPath() + IMAGE_DIR + savedPath);
                             }
                         }

                         p.setImageUrlList(existingUrls);
                     } catch (IOException ioe) {
                         session.setAttribute("message", "Cập nhật ảnh thất bại: " + ioe.getMessage());
                         session.setAttribute("messageType", "warning");
                         response.sendRedirect(buildRedirectUrl(request));
                         return;
                     }
                 } else {
                     // Giữ nguyên ảnh cũ
                     Product old = productDAO.findById(productID);
                     if (old != null) p.setImageUrl(old.getImageUrlRaw());
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

    private Integer parseIntParam(String param, Integer defaultVal) {
        if (param == null || param.isBlank()) return defaultVal;
        try { return Integer.parseInt(param.trim()); } catch (NumberFormatException e) { return defaultVal; }
    }

    private String verifyImages(List<Part> imageParts) {
        for (Part part : imageParts) {
            String error = verifyImage(part);
            if (error != null) return error;
        }
        return null;
    }

    private String buildRedirectUrl(HttpServletRequest request) {
        String[] params = {"keyword", "filterStatus", "filterCategoryID", "filterUnitID", "page"};
        StringBuilder sb = new StringBuilder(request.getContextPath() + "/products?");
        for (String name : params) {
            String value = request.getParameter(name);
            if (value != null && !value.isBlank()) {
                if ("keyword".equals(name)) value = value.trim().replaceAll("\\s+", " ");
                sb.append(name).append('=').append(value).append('&');
            }
        }
        if (sb.charAt(sb.length() - 1) == '?' || sb.charAt(sb.length() - 1) == '&')
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private Part safeGetPart(HttpServletRequest request, String name) {
        try {
            return request.getPart(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Xác thực ảnh thật bằng ImageIO + giới hạn 3MB + giới hạn kích thước.
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
                if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                    return "Kích thước ảnh quá lớn (tối đa " + MAX_IMAGE_DIMENSION + "px mỗi chiều).";
                }
                BufferedImage bi = reader.read(0);
                if (bi == null) {
                    return "Không thể giải mã ảnh.";
                }
                bi.flush();
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
     * @return tên file đã lưu (vd: "product_1_a1b2c3d4.jpg")
     */
    private String saveProductImageFile(HttpServletRequest request, Part imagePart, int productId) throws IOException {
        File dir = ensureImageDir(request);
        String ext = resolveExtension(imagePart);
        String uniqueName = "product_" + productId + "_" + UUID.randomUUID() + "." + ext;
        File target = new File(dir, uniqueName);
        try (InputStream in = imagePart.getInputStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("SAVE IMAGE: " + target.getAbsolutePath());
        return uniqueName;
    }

    /** Lấy tất cả Part có name="imageFile" và có nội dung */
    private List<Part> getImageParts(HttpServletRequest request) {
        List<Part> parts = new ArrayList<>();
        try {
            Collection<Part> allParts = request.getParts();
            for (Part part : allParts) {
                if ("imageFile".equals(part.getName()) && part.getSize() > 0) {
                    parts.add(part);
                }
            }
        } catch (Exception e) {
            // Không có file nào được upload
        }
        return parts;
    }

    /** Xoá tất cả file ảnh của sản phẩm theo path trong DB */
    private void deleteProductImageFiles(HttpServletRequest request, int productId) {
        try {
            Product product = productDAO.findById(productId);
            if (product == null) return;
            List<String> urls = product.getImageUrlList();
            for (String url : urls) {
                deleteImageFileByUrl(request, url);
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Không thể đọc ảnh từ DB để xoá: " + e.getMessage());
        }
    }

    /** Xoá 1 file ảnh cụ thể theo URL */
    private void deleteImageFileByUrl(HttpServletRequest request, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        // Bỏ query string ?v=... nếu có
        int qmark = filename.indexOf('?');
        if (qmark >= 0) filename = filename.substring(0, qmark);
        File dir = ensureImageDir(request);
        File file = new File(dir, filename);
        if (file.exists()) {
            boolean deleted = file.delete();
            System.out.println("DELETE IMAGE: " + file.getAbsolutePath() + " -> " + (deleted ? "OK" : "FAILED"));
        }
    }
}
