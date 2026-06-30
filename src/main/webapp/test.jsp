<%@ page import="java.sql.*, dal.DBContext, java.util.*" %>
<%
    List<Map<String, String>> records = new ArrayList<>();
    try (Connection conn = DBContext.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM inventory_ticket ORDER BY ticket_id DESC")) {
        ResultSetMetaData md = rs.getMetaData();
        while (rs.next()) {
            Map<String, String> row = new HashMap<>();
            for (int i=1; i<=md.getColumnCount(); i++) {
                row.put(md.getColumnName(i), rs.getString(i));
            }
            records.add(row);
        }
    } catch (Exception e) {
        out.print(e.getMessage());
    }
    for (Map<String, String> row : records) {
        out.println(row + "<br/>");
    }
%>
