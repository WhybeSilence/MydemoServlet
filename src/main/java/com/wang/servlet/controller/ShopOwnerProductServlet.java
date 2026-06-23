package com.wang.servlet.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wang.servlet.entity.SysUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;

@WebServlet("/shopOwner/product")
public class ShopOwnerProductServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8";
    private static final String USER = "remote_user";
    private static final String PASS = "512179588";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            out.print("{\"code\": 401, \"msg\": \"未登录\"}");
            return;
        }

        SysUser user = (SysUser) session.getAttribute("currentUser");
        if (!"shop_owner".equals(user.getUserRole())) {
            out.print("{\"code\": 403, \"msg\": \"无权限访问\"}");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String shopSql = "SELECT shop_id, shop_name FROM shop WHERE owner_id = ? LIMIT 1";
            pstmt = conn.prepareStatement(shopSql);
            pstmt.setInt(1, user.getUserId());
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                out.print("{\"code\": 404, \"msg\": \"未找到您的店铺\"}");
                return;
            }
            int shopId = rs.getInt("shop_id");
            String shopName = rs.getString("shop_name");
            rs.close();
            pstmt.close();

            String action = request.getParameter("action");
            if ("all".equals(action)) {
                String sql = "SELECT product_id, name, price, stock, description, preview_url, audit_status, reject_reason, upload_time " +
                        "FROM product WHERE shop_id = ? ORDER BY audit_status ASC, upload_time DESC";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, shopId);
                rs = pstmt.executeQuery();

                JsonArray products = new JsonArray();
                while (rs.next()) {
                    JsonObject product = new JsonObject();
                    product.addProperty("productId", rs.getInt("product_id"));
                    product.addProperty("name", rs.getString("name"));
                    product.addProperty("price", rs.getBigDecimal("price"));
                    product.addProperty("stock", rs.getInt("stock"));
                    product.addProperty("description", rs.getString("description"));
                    product.addProperty("previewUrl", rs.getString("preview_url"));
                    product.addProperty("auditStatus", rs.getInt("audit_status"));
                    product.addProperty("rejectReason", rs.getString("reject_reason"));
                    product.addProperty("uploadTime", rs.getString("upload_time"));
                    products.add(product);
                }

                out.print("{\"code\": 200, \"msg\": \"成功\", \"data\": " + new Gson().toJson(products) + "}");
            } else {
                out.print("{\"code\": 200, \"msg\": \"成功\", \"shopId\": " + shopId + ", \"shopName\": \"" + shopName + "\"}");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"数据库驱动加载失败\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"系统繁忙\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            out.print("{\"code\": 401, \"msg\": \"未登录\"}");
            return;
        }

        SysUser user = (SysUser) session.getAttribute("currentUser");
        if (!"shop_owner".equals(user.getUserRole())) {
            out.print("{\"code\": 403, \"msg\": \"无权限访问\"}");
            return;
        }

        String action = request.getParameter("action");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);

            String shopSql = "SELECT shop_id FROM shop WHERE owner_id = ? LIMIT 1";
            pstmt = conn.prepareStatement(shopSql);
            pstmt.setInt(1, user.getUserId());
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                out.print("{\"code\": 404, \"msg\": \"未找到您的店铺\"}");
                conn.rollback();
                return;
            }
            int shopId = rs.getInt("shop_id");
            rs.close();
            pstmt.close();

            if ("add".equals(action)) {
                String name = request.getParameter("name");
                String priceStr = request.getParameter("price");
                String stockStr = request.getParameter("stock");
                String description = request.getParameter("description");
                String previewUrl = request.getParameter("previewUrl");

                if (name == null || name.isEmpty()) {
                    out.print("{\"code\": 400, \"msg\": \"商品名称不能为空\"}");
                    conn.rollback();
                    return;
                }

                BigDecimal price = new BigDecimal(priceStr != null && !priceStr.isEmpty() ? priceStr : "0");
                int stock = Integer.parseInt(stockStr != null && !stockStr.isEmpty() ? stockStr : "0");
                if (description == null) description = "";
                if (previewUrl == null) previewUrl = "";

                String insertSql = "INSERT INTO product (shop_id, name, price, stock, description, preview_url, audit_status, upload_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 0, NOW())";
                pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, shopId);
                pstmt.setString(2, name);
                pstmt.setBigDecimal(3, price);
                pstmt.setInt(4, stock);
                pstmt.setString(5, description);
                pstmt.setString(6, previewUrl);
                pstmt.executeUpdate();

                String msgSql = "INSERT INTO system_message (receiver_id, content) VALUES (?, ?)";
                pstmt.close();
                pstmt = conn.prepareStatement(msgSql);
                pstmt.setInt(1, user.getUserId());
                pstmt.setString(2, "您的商品【" + name + "】已发送审核申请，正等待审核");
                pstmt.executeUpdate();

                conn.commit();
                out.print("{\"code\": 200, \"msg\": \"商品已提交审核\"}");

            } else if ("update".equals(action)) {
                String productIdStr = request.getParameter("productId");
                if (productIdStr == null || productIdStr.isEmpty()) {
                    out.print("{\"code\": 400, \"msg\": \"商品ID不能为空\"}");
                    conn.rollback();
                    return;
                }
                int productId = Integer.parseInt(productIdStr);

                String checkSql = "SELECT product_id FROM product WHERE product_id = ? AND shop_id = ?";
                pstmt = conn.prepareStatement(checkSql);
                pstmt.setInt(1, productId);
                pstmt.setInt(2, shopId);
                rs = pstmt.executeQuery();
                if (!rs.next()) {
                    out.print("{\"code\": 404, \"msg\": \"商品不存在或不属于您的店铺\"}");
                    conn.rollback();
                    return;
                }
                rs.close();
                pstmt.close();

                String name = request.getParameter("name");
                String priceStr = request.getParameter("price");
                String stockStr = request.getParameter("stock");
                String description = request.getParameter("description");
                String previewUrl = request.getParameter("previewUrl");

                if (name == null || name.isEmpty()) {
                    out.print("{\"code\": 400, \"msg\": \"商品名称不能为空\"}");
                    conn.rollback();
                    return;
                }

                BigDecimal price = new BigDecimal(priceStr != null && !priceStr.isEmpty() ? priceStr : "0");
                int stock = Integer.parseInt(stockStr != null && !stockStr.isEmpty() ? stockStr : "0");
                if (description == null) description = "";
                if (previewUrl == null) previewUrl = "";

                String updateSql = "UPDATE product SET name = ?, price = ?, stock = ?, description = ?, preview_url = ? WHERE product_id = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setString(1, name);
                pstmt.setBigDecimal(2, price);
                pstmt.setInt(3, stock);
                pstmt.setString(4, description);
                pstmt.setString(5, previewUrl);
                pstmt.setInt(6, productId);
                pstmt.executeUpdate();

                conn.commit();
                out.print("{\"code\": 200, \"msg\": \"商品已更新\"}");

            } else if ("remove".equals(action)) {
                String productIdStr = request.getParameter("productId");
                if (productIdStr == null || productIdStr.isEmpty()) {
                    out.print("{\"code\": 400, \"msg\": \"商品ID不能为空\"}");
                    conn.rollback();
                    return;
                }
                int productId = Integer.parseInt(productIdStr);

                String checkSql = "SELECT product_id FROM product WHERE product_id = ? AND shop_id = ?";
                pstmt = conn.prepareStatement(checkSql);
                pstmt.setInt(1, productId);
                pstmt.setInt(2, shopId);
                rs = pstmt.executeQuery();
                if (!rs.next()) {
                    out.print("{\"code\": 404, \"msg\": \"商品不存在或不属于您的店铺\"}");
                    conn.rollback();
                    return;
                }
                rs.close();
                pstmt.close();

                String deleteSql = "DELETE FROM product WHERE product_id = ?";
                pstmt = conn.prepareStatement(deleteSql);
                pstmt.setInt(1, productId);
                pstmt.executeUpdate();

                conn.commit();
                out.print("{\"code\": 200, \"msg\": \"商品已下架\"}");

            } else {
                conn.rollback();
                out.print("{\"code\": 400, \"msg\": \"无效操作\"}");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"数据库驱动加载失败\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            out.print("{\"code\": 500, \"msg\": \"系统繁忙\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
