<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Product, java.util.List, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    int currentPage = (Integer) request.getAttribute("currentPage");
    int totalPages  = (Integer) request.getAttribute("totalPages");
    String ctx = request.getContextPath();
    NumberFormat vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Product Management - Finora</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/styles.css">
    <style>
        .container { max-width:1200px; margin:2rem auto; padding:0 1rem; }
        .header-actions { display:flex; justify-content:space-between; align-items:center; margin-bottom:2rem; }
        .header-actions h1 { margin:0; font-size:1.875rem; font-weight:800; color: var(--text); text-shadow:0 1px 10px rgba(63,231,255,.28); }
        .btn { display:inline-flex; align-items:center; padding:.5rem 1rem; border-radius:999px; font-weight:800; cursor:pointer; border:none; transition:all .2s; font-size:.875rem; text-transform:uppercase; letter-spacing:0.05em; }
        .btn-primary { background: linear-gradient(135deg, var(--cyan), var(--violet)); color:#06101c; box-shadow:0 4px 15px rgba(63,231,255,.2); }
        .btn-primary:hover { transform: translateY(-2px); box-shadow:0 6px 20px rgba(63,231,255,.3); }
        .btn-danger  { background: rgba(255,107,138,.12); color: var(--danger); border: 1px solid rgba(255,107,138,.28); padding:.25rem .6rem !important; }
        .btn-danger:hover  { background: rgba(255,107,138,.25); }
        .card { background: var(--panel); border: 1px solid var(--line); border-radius: 26px; overflow:hidden; box-shadow: 0 24px 80px rgba(0,0,0,.28); backdrop-filter: blur(10px); }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:1rem; text-align:left; border-bottom:1px solid var(--line); color: var(--text); }
        th { background: rgba(255,255,255,0.02); font-weight:800; color: var(--cyan); text-transform:uppercase; font-size:.7rem; letter-spacing:.1em; }
        tr:last-child td { border-bottom:none; }
        tbody tr:hover td { background: var(--panel-strong); }
        .badge { padding:.3rem .7rem; border-radius:9999px; font-size:.72rem; font-weight:800; background:rgba(84,242,161,.12); color:var(--ok); border: 1px solid rgba(84,242,161,.24); text-transform:uppercase; letter-spacing:0.05em; }
        .badge.inactive { background:rgba(255,107,138,.12); color:var(--danger); border-color: rgba(255,107,138,.28); }
        
        /* Pagination */
        .pagination { display:flex; justify-content:center; align-items:center; padding:1.5rem; gap:.5rem; border-top:1px solid var(--line); background: rgba(255,255,255,0.01); }
        .pagination a, .pagination span { padding:.5rem .8rem; border-radius:.5rem; border:1px solid var(--line); color:var(--muted); text-decoration:none; transition:all .2s; font-weight: 600; }
        .pagination a:hover { background: var(--panel); color: var(--text); border-color: var(--cyan); }
        .pagination .active { background: linear-gradient(135deg, var(--cyan), var(--violet)); color:#06101c; border-color:transparent; font-weight: 800; }
        
        /* Modal */
        .modal { display:none; position:fixed; inset:0; background:rgba(7,17,31,.8); backdrop-filter:blur(8px); align-items:center; justify-content:center; z-index:50; }
        .modal-content { background: #07111f; border: 1px solid rgba(63,231,255,.3); border-radius: 28px; width:100%; max-width:520px; padding:2.5rem; box-shadow:0 24px 80px rgba(63,231,255,.15); }
        .modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem; }
        .modal-header h2 { margin:0; font-size:1.5rem; color: var(--text); font-weight: 800; text-shadow:0 1px 10px rgba(63,231,255,.28); }
        .close-btn { background:none; border:none; font-size:1.5rem; cursor:pointer; color:var(--muted); transition: color 0.2s; }
        .close-btn:hover { color: var(--danger); }
        .form-group { margin-bottom:1.2rem; }
        .form-group label { display:block; margin-bottom:.5rem; font-weight:600; font-size:.875rem; color: var(--muted); }
        .form-group input, .form-group select { width:100%; padding:.6rem .8rem; background: var(--panel); border:1px solid var(--line); border-radius:.5rem; font-size:.9rem; color: var(--text); transition: all 0.2s; }
        .form-group input:focus, .form-group select:focus { outline:none; border-color:var(--cyan); background: var(--panel-strong); box-shadow:0 0 0 3px rgba(63,231,255,.15); }
        .form-row { display:flex; gap:1rem; }
        .form-row .form-group { flex:1; }
        .form-actions { margin-top:2rem; display:flex; justify-content:flex-end; gap:.75rem; }
        .empty-state { text-align:center; padding:3rem; color:var(--muted); }
        
        ::placeholder { color: rgba(255,255,255,0.2); }
        option { background: #07111f; color: var(--text); }
        .btn-cancel { background: var(--panel); color: var(--muted); border: 1px solid var(--line); }
        .btn-cancel:hover { background: var(--panel-strong); color: var(--text); }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <div class="container">
        <div class="header-actions">
            <h1>Product Management</h1>
            <button class="btn btn-primary" onclick="openModal()">+ Add Product</button>
        </div>

        <div class="card">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>SKU</th>
                        <th>Name</th>
                        <th>Category ID</th>
                        <th>Price</th>
                        <th>Cost Price</th>
                        <th>Stock Alert</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
<%
    if (products == null || products.isEmpty()) {
%>
                    <tr>
                        <td colspan="9" class="empty-state">No products found. Add a new product to get started.</td>
                    </tr>
<%
    } else {
        for (Product p : products) {
            String badgeClass = "Active".equalsIgnoreCase(p.getStatus()) ? "badge" : "badge inactive";
%>
                    <tr>
                        <td><%= p.getProductID() %></td>
                        <td><strong><%= p.getSku() != null ? p.getSku() : "" %></strong></td>
                        <td><%= p.getName() != null ? p.getName() : "" %></td>
                        <td><%= p.getCategoryID() %></td>
                        <td><%= p.getPrice() != null ? vndFormat.format(p.getPrice()) : "0 ₫" %></td>
                        <td><%= p.getCostPrice() != null ? vndFormat.format(p.getCostPrice()) : "0 ₫" %></td>
                        <td><%= p.getStockAlertQty() %></td>
                        <td><span class="<%= badgeClass %>"><%= p.getStatus() != null ? p.getStatus() : "" %></span></td>
                        <td>
                            <form action="<%= ctx %>/product-management" method="post" style="display:inline;"
                                  onsubmit="return confirm('Delete this product?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="<%= p.getProductID() %>">
                                <button type="submit" class="btn btn-danger">Delete</button>
                            </form>
                        </td>
                    </tr>
<%
        }
    }
%>
                </tbody>
            </table>

<%  if (totalPages > 1) { %>
            <div class="pagination">
<%      if (currentPage > 1) { %>
                <a href="?page=<%= currentPage - 1 %>">&laquo; Prev</a>
<%      }
        for (int i = 1; i <= totalPages; i++) {
            if (i == currentPage) { %>
                <span class="active"><%= i %></span>
<%          } else { %>
                <a href="?page=<%= i %>"><%= i %></a>
<%          }
        }
        if (currentPage < totalPages) { %>
                <a href="?page=<%= currentPage + 1 %>">Next &raquo;</a>
<%      } %>
            </div>
<%  } %>
        </div>
    </div>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

    <!-- Add Product Modal -->
    <div id="addProductModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Add New Product</h2>
                <button class="close-btn" onclick="closeModal()">&times;</button>
            </div>
            <form action="<%= ctx %>/product-management" method="post">
                <input type="hidden" name="action" value="add">

                <div class="form-group">
                    <label for="name">Product Name</label>
                    <input type="text" id="name" name="name" required placeholder="e.g. Wireless Mouse">
                </div>

                <div class="form-group">
                    <label for="sku">SKU</label>
                    <input type="text" id="sku" name="sku" required placeholder="e.g. MOUSE-001">
                </div>

                <div class="form-group">
                    <label for="categoryID">Category ID</label>
                    <input type="number" id="categoryID" name="categoryID" required value="1" min="1">
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="price">Price (VND)</label>
                        <input type="number" id="price" name="price" required placeholder="0" min="0">
                    </div>
                    <div class="form-group">
                        <label for="costPrice">Cost Price (VND)</label>
                        <input type="number" id="costPrice" name="costPrice" required placeholder="0" min="0">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="stockAlertQty">Stock Alert Qty</label>
                        <input type="number" id="stockAlertQty" name="stockAlertQty" required value="10" min="0">
                    </div>
                    <div class="form-group">
                        <label for="status">Status</label>
                        <select id="status" name="status" required>
                            <option value="Active">Active</option>
                            <option value="Inactive">Inactive</option>
                        </select>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" class="btn btn-cancel" onclick="closeModal()">Cancel</button>
                    <button type="submit" class="btn btn-primary">Save Product</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        const modal = document.getElementById('addProductModal');
        function openModal()  { modal.style.display = 'flex'; }
        function closeModal() { modal.style.display = 'none'; }
        window.addEventListener('click', e => { if (e.target === modal) closeModal(); });
    </script>
</body>
</html>
