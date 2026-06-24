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

@WebServlet("/wishlist")
public class WishlistServlet extends HttpServlet {

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

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String sql = "SELECT w.id, w.product_id, w.add_time, p.name, p.preview_url, p.price, p.stock " +
                    "FROM wishlist w LEFT JOIN product p ON w.product_id = p.product_id " +
                    "WHERE w.user_id = ? ORDER BY w.add_time DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user.getUserId());
            rs = pstmt.executeQuery();

            JsonArray wishlist = new JsonArray();
            while (rs.next()) {
                JsonObject item = new JsonObject();
                item.addProperty("id", rs.getInt("id"));
                item.addProperty("productId", rs.getInt("product_id"));
                item.addProperty("name", rs.getString("name"));
                item.addProperty("previewUrl", rs.getString("preview_url"));
                item.addProperty("price", rs.getBigDecimal("price"));
                item.addProperty("stock", rs.getInt("stock"));
                item.addProperty("addTime", rs.getString("add_time"));
                wishlist.add(item);
            }

            out.print("{\"code\": 200, \"msg\": \"成功\", \"data\": " + new Gson().toJson(wishlist) + "}");

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

            String checkSql = "SELECT stock FROM product WHERE product_id = ? AND audit_status = 1";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                out.print("{\"code\": 404, \"msg\": \"商品不存在或未上架\"}");
                return;
            }
            int stock = rs.getInt("stock");
            if (stock <= 0) {
                out.print("{\"code\": 400, \"msg\": \"商品已售罄\"}");
                return;
            }
            rs.close();
            pstmt.close();

            String insertSql = "INSERT IGNORE INTO wishlist (user_id, product_id) VALUES (?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, user.getUserId());
            pstmt.setInt(2, productId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                String updateStockSql = "UPDATE product SET stock = stock - 1 WHERE product_id = ? AND stock > 0";
                pstmt.close();
                pstmt = conn.prepareStatement(updateStockSql);
                pstmt.setInt(1, productId);
                pstmt.executeUpdate();
                out.print("{\"code\": 200, \"msg\": \"已加入愿望单\"}");
            } else {
                out.print("{\"code\": 400, \"msg\": \"该商品已在愿望单中\"}");
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            out.print("{\"code\": 401, \"msg\": \"未登录\"}");
            return;
        }

        SysUser user = (SysUser) session.getAttribute("currentUser");
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

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String deleteSql = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setInt(1, user.getUserId());
            pstmt.setInt(2, productId);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                pstmt.close();
                String updateStockSql = "UPDATE product SET stock = stock + 1 WHERE product_id = ?";
                pstmt = conn.prepareStatement(updateStockSql);
                pstmt.setInt(1, productId);
                pstmt.executeUpdate();
                out.print("{\"code\": 200, \"msg\": \"已从愿望单移除\"}");
            } else {
                out.print("{\"code\": 400, \"msg\": \"该商品不在愿望单中\"}");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"数据库驱动加载失败\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"系统繁忙\"}");
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
