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
import java.sql.*;

@WebServlet("/admin/audit")
public class AdminAuditServlet extends HttpServlet {

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
        if (!"admin".equals(user.getUserRole())) {
            out.print("{\"code\": 403, \"msg\": \"无权限访问\"}");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String sql = "SELECT p.product_id, p.shop_id, p.name, p.price, p.stock, p.description, " +
                    "p.preview_url, p.audit_status, p.upload_time, s.shop_name, s.owner_id, u.username as owner_name " +
                    "FROM product p LEFT JOIN shop s ON p.shop_id = s.shop_id " +
                    "LEFT JOIN sys_user u ON s.owner_id = u.user_id " +
                    "WHERE p.audit_status = 0 ORDER BY p.upload_time DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            JsonArray products = new JsonArray();
            while (rs.next()) {
                JsonObject product = new JsonObject();
                product.addProperty("productId", rs.getInt("product_id"));
                product.addProperty("shopId", rs.getInt("shop_id"));
                product.addProperty("shopName", rs.getString("shop_name"));
                product.addProperty("ownerId", rs.getInt("owner_id"));
                product.addProperty("ownerName", rs.getString("owner_name"));
                product.addProperty("name", rs.getString("name"));
                product.addProperty("price", rs.getBigDecimal("price"));
                product.addProperty("stock", rs.getInt("stock"));
                product.addProperty("description", rs.getString("description"));
                product.addProperty("previewUrl", rs.getString("preview_url"));
                product.addProperty("auditStatus", rs.getInt("audit_status"));
                product.addProperty("uploadTime", rs.getString("upload_time"));
                products.add(product);
            }

            out.print("{\"code\": 200, \"msg\": \"成功\", \"data\": " + new Gson().toJson(products) + "}");

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
        if (!"admin".equals(user.getUserRole())) {
            out.print("{\"code\": 403, \"msg\": \"无权限访问\"}");
            return;
        }

        String action = request.getParameter("action");
        String productIdStr = request.getParameter("productId");

        if (productIdStr == null || productIdStr.isEmpty()) {
            out.print("{\"code\": 400, \"msg\": \"商品ID不能为空\"}");
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            out.print("{\"code\": 400, \"msg\": \"商品ID格式错误\"}");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);

            String productSql = "SELECT p.name, p.shop_id, s.owner_id FROM product p " +
                    "LEFT JOIN shop s ON p.shop_id = s.shop_id WHERE p.product_id = ?";
            pstmt = conn.prepareStatement(productSql);
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                out.print("{\"code\": 404, \"msg\": \"商品不存在\"}");
                conn.rollback();
                return;
            }
            String productName = rs.getString("name");
            int ownerId = rs.getInt("owner_id");
            rs.close();
            pstmt.close();

            if ("approve".equals(action)) {
                String updateSql = "UPDATE product SET audit_status = 1 WHERE product_id = ? AND audit_status = 0";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, productId);
                int rows = pstmt.executeUpdate();
                pstmt.close();

                if (rows > 0) {
                    String msgSql = "INSERT INTO system_message (receiver_id, content) VALUES (?, ?)";
                    pstmt = conn.prepareStatement(msgSql);
                    pstmt.setInt(1, ownerId);
                    pstmt.setString(2, "您的商品【" + productName + "】已通过审核，批准上架。");
                    pstmt.executeUpdate();
                    pstmt.close();

                    conn.commit();
                    out.print("{\"code\": 200, \"msg\": \"审核通过\"}");
                } else {
                    conn.rollback();
                    out.print("{\"code\": 400, \"msg\": \"审核状态已变更\"}");
                }

            } else if ("reject".equals(action)) {
                String rejectReason = request.getParameter("rejectReason");
                if (rejectReason == null || rejectReason.isEmpty()) {
                    rejectReason = "不符合上架要求";
                }

                String updateSql = "UPDATE product SET audit_status = 2, reject_reason = ? WHERE product_id = ? AND audit_status = 0";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setString(1, rejectReason);
                pstmt.setInt(2, productId);
                int rows = pstmt.executeUpdate();
                pstmt.close();

                if (rows > 0) {
                    String msgSql = "INSERT INTO system_message (receiver_id, content) VALUES (?, ?)";
                    pstmt = conn.prepareStatement(msgSql);
                    pstmt.setInt(1, ownerId);
                    pstmt.setString(2, "您的商品【" + productName + "】未通过审核，拒绝原因：" + rejectReason);
                    pstmt.executeUpdate();
                    pstmt.close();

                    conn.commit();
                    out.print("{\"code\": 200, \"msg\": \"已拒绝\"}");
                } else {
                    conn.rollback();
                    out.print("{\"code\": 400, \"msg\": \"审核状态已变更\"}");
                }
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
