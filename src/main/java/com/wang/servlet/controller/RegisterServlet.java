package com.wang.servlet.controller;

import com.wang.servlet.util.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

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
            conn = DBUtil.getConnection();

            // 1. 检查用户名是否存在
            String checkSql = "SELECT user_id FROM sys_user WHERE username = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                out.print("{\"code\": 409, \"msg\": \"用户名已存在\"}");
                return;
            }

            // 2. 插入新用户
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

        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"数据库操作异常\"}");
        } finally {
            // 【修复】确保所有资源都被关闭
            DBUtil.close(conn, checkStmt, rs);
            try {
                if (insertStmt != null) insertStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("register.html");
    }
}