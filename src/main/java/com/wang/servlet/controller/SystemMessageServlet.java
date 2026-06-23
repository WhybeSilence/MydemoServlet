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

@WebServlet("/messages")
public class SystemMessageServlet extends HttpServlet {

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

            String sql = "SELECT msg_id, content, is_read, create_time FROM system_message " +
                    "WHERE receiver_id = ? ORDER BY create_time DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user.getUserId());
            rs = pstmt.executeQuery();

            JsonArray messages = new JsonArray();
            while (rs.next()) {
                JsonObject msg = new JsonObject();
                msg.addProperty("msgId", rs.getInt("msg_id"));
                msg.addProperty("content", rs.getString("content"));
                msg.addProperty("isRead", rs.getInt("is_read"));
                msg.addProperty("createTime", rs.getString("create_time"));
                messages.add(msg);
            }

            out.print("{\"code\": 200, \"msg\": \"成功\", \"data\": " + new Gson().toJson(messages) + "}");

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
