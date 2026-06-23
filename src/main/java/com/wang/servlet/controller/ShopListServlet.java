package com.wang.servlet.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/shopList")
public class ShopListServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8";
    private static final String USER = "remote_user";
    private static final String PASS = "512179588";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            String sql = "SELECT s.shop_id, s.shop_name, s.description, s.shop_img, s.status, s.owner_id, u.username as owner_name " +
                    "FROM shop s LEFT JOIN sys_user u ON s.owner_id = u.user_id ORDER BY s.shop_id";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            JsonArray shops = new JsonArray();
            while (rs.next()) {
                JsonObject shop = new JsonObject();
                shop.addProperty("shopId", rs.getInt("shop_id"));
                shop.addProperty("shopName", rs.getString("shop_name"));
                shop.addProperty("description", rs.getString("description"));
                shop.addProperty("shopImg", rs.getString("shop_img"));
                shop.addProperty("status", rs.getInt("status"));
                shop.addProperty("ownerId", rs.getInt("owner_id"));
                shop.addProperty("ownerName", rs.getString("owner_name"));
                shops.add(shop);
            }

            out.print("{\"code\": 200, \"msg\": \"成功\", \"data\": " + new Gson().toJson(shops) + "}");

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
