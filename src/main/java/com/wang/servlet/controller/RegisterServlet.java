package com.wang.servlet.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    // ================== 数据库连接信息（与 LoginServlet 保持一致） ==================
    private static final String DB_URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8";
    private static final String USER = "remote_user";
    private static final String PASS = "512179588";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;
        PrintWriter out = response.getWriter();

        try {
            // 1. 加载驱动并获取数据库连接
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 2. 检查用户名是否存在
            String checkSql = "SELECT user_id FROM sys_user WHERE username = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                out.print("{\"code\": 409, \"msg\": \"用户名已存在\"}");
                return;
            }

            // 3. 密码加密并插入新用户
            String encodedPwd = BCrypt.hashpw(password, BCrypt.gensalt());
            String insertSql = "INSERT INTO sys_user (username, password, user_role, create_time) VALUES (?, ?, 'user', NOW())";
            insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, encodedPwd);

            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                out.print("{\"code\": 200, \"msg\": \"注册成功\"}");
            } else {
                out.print("{\"code\": 500, \"msg\": \"注册失败\"}");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"数据库驱动加载失败\"}");
        } catch (SQLException e) {
            e.printStackTrace(); // 确保这行存在，并在控制台看日志
//            out.print("{\"code\": 500, \"msg\": \"数据库操作异常\"}");
            // 临时修改：将具体错误信息返回给前端，方便调试
            out.print("{\"code\": 500, \"msg\": \"数据库异常: " + e.getMessage() + "\"}");

        } finally {
            // 4. 手动关闭所有数据库资源
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (checkStmt != null) checkStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("register.html");
    }
}