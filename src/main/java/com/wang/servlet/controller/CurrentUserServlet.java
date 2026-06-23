package com.wang.servlet.controller;

import com.google.gson.Gson;
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

@WebServlet("/currentUser")
public class CurrentUserServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8";
    private static final String USER = "remote_user";
    private static final String PASS = "512179588";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

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

            JsonObject result = new JsonObject();
            result.addProperty("userId", user.getUserId());
            result.addProperty("username", user.getUsername());
            result.addProperty("userRole", user.getUserRole());

            String profileSql = "SELECT avatar_url, bio FROM user_profile WHERE user_id = ?";
            pstmt = conn.prepareStatement(profileSql);
            pstmt.setInt(1, user.getUserId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                result.addProperty("avatarUrl", rs.getString("avatar_url"));
                result.addProperty("bio", rs.getString("bio"));
            } else {
                result.addProperty("avatarUrl", "");
                result.addProperty("bio", "");
            }
            rs.close();
            pstmt.close();

            String userTimeSql = "SELECT create_time FROM sys_user WHERE user_id = ?";
            pstmt = conn.prepareStatement(userTimeSql);
            pstmt.setInt(1, user.getUserId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                result.addProperty("createTime", rs.getString("create_time"));
            }
            rs.close();
            pstmt.close();

            String wishlistSql = "SELECT COUNT(*) FROM wishlist WHERE user_id = ?";
            pstmt = conn.prepareStatement(wishlistSql);
            pstmt.setInt(1, user.getUserId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                result.addProperty("wishlistCount", rs.getInt(1));
            }
            rs.close();
            pstmt.close();

            if ("admin".equals(user.getUserRole())) {
                String pendingSql = "SELECT COUNT(*) FROM product WHERE audit_status = 0";
                pstmt = conn.prepareStatement(pendingSql);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.addProperty("pendingCount", rs.getInt(1));
                }
                rs.close();
                pstmt.close();
            }

            if ("shop_owner".equals(user.getUserRole())) {
                String shopSql = "SELECT shop_id, shop_name FROM shop WHERE owner_id = ? LIMIT 1";
                pstmt = conn.prepareStatement(shopSql);
                pstmt.setInt(1, user.getUserId());
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.addProperty("shopId", rs.getInt("shop_id"));
                    result.addProperty("shopName", rs.getString("shop_name"));
                }
                rs.close();
                pstmt.close();
            }

            out.print("{\"code\": 200, \"msg\": \"成功\", \"data\": " + gson.toJson(result) + "}");

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
