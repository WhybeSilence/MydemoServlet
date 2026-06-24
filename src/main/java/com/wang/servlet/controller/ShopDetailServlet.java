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

@WebServlet("/shopDetail")
public class ShopDetailServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8";
    private static final String USER = "remote_user";
    private static final String PASS = "512179588";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String shopIdStr = request.getParameter("shopId");
        if (shopIdStr == null || shopIdStr.isEmpty()) {
            out.print("{\"code\": 400, \"msg\": \"店铺ID不能为空\"}");
            return;
        }

        int shopId;
        try {
            shopId = Integer.parseInt(shopIdStr);
        } catch (NumberFormatException e) {
            out.print("{\"code\": 400, \"msg\": \"店铺ID格式错误\"}");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            JsonObject result = new JsonObject();

            String shopSql = "SELECT s.shop_id, s.shop_name, s.description, s.shop_img, s.status, s.owner_id, u.username as owner_name " +
                    "FROM shop s LEFT JOIN sys_user u ON s.owner_id = u.user_id WHERE s.shop_id = ?";
            pstmt = conn.prepareStatement(shopSql);
            pstmt.setInt(1, shopId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                JsonObject shop = new JsonObject();
                shop.addProperty("shopId", rs.getInt("shop_id"));
                shop.addProperty("shopName", rs.getString("shop_name"));
                shop.addProperty("description", rs.getString("description"));
                shop.addProperty("shopImg", rs.getString("shop_img"));
                shop.addProperty("status", rs.getInt("status"));
                shop.addProperty("ownerId", rs.getInt("owner_id"));
                shop.addProperty("ownerName", rs.getString("owner_name"));
                result.add("shop", shop);
            } else {
                out.print("{\"code\": 404, \"msg\": \"店铺不存在\"}");
                return;
            }
            rs.close();
            pstmt.close();

            boolean isOwner = false;
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                SysUser currentUser = (SysUser) session.getAttribute("currentUser");
                if ("shop_owner".equals(currentUser.getUserRole())) {
                    String ownerCheckSql = "SELECT shop_id FROM shop WHERE shop_id = ? AND owner_id = ?";
                    pstmt = conn.prepareStatement(ownerCheckSql);
                    pstmt.setInt(1, shopId);
                    pstmt.setInt(2, currentUser.getUserId());
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        isOwner = true;
                    }
                    rs.close();
                    pstmt.close();
                }
            }

            String productSql;
            if (isOwner) {
                productSql = "SELECT product_id, shop_id, name, price, stock, description, preview_url, audit_status, reject_reason, upload_time " +
                        "FROM product WHERE shop_id = ? ORDER BY audit_status ASC, upload_time DESC";
            } else {
                productSql = "SELECT product_id, shop_id, name, price, stock, description, preview_url, audit_status, upload_time " +
                        "FROM product WHERE shop_id = ? AND audit_status = 1 ORDER BY product_id";
            }
            pstmt = conn.prepareStatement(productSql);
            pstmt.setInt(1, shopId);
            rs = pstmt.executeQuery();

            JsonArray products = new JsonArray();
            while (rs.next()) {
                JsonObject product = new JsonObject();
                product.addProperty("productId", rs.getInt("product_id"));
                product.addProperty("shopId", rs.getInt("shop_id"));
                product.addProperty("name", rs.getString("name"));
                product.addProperty("price", rs.getBigDecimal("price"));
                product.addProperty("stock", rs.getInt("stock"));
                product.addProperty("description", rs.getString("description"));
                product.addProperty("previewUrl", rs.getString("preview_url"));
                product.addProperty("auditStatus", rs.getInt("audit_status"));
                product.addProperty("uploadTime", rs.getString("upload_time"));
                if (isOwner) {
                    product.addProperty("rejectReason", rs.getString("reject_reason"));
                }
                products.add(product);
            }
            result.add("products", products);

            out.print("{\"code\": 200, \"msg\": \"成功\", \"data\": " + new Gson().toJson(result) + "}");

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
}
