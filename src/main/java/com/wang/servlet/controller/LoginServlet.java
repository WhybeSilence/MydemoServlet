package com.wang.servlet.controller;

import com.wang.servlet.entity.SysUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // ================== 数据库连接信息（参考 StudentQuery.java 格式） ==================
    private static final String DB_URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8";
    private static final String USER = "remote_user";
    private static final String PASS = "512179588";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // 设置响应类型为 JSON
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PrintWriter out = response.getWriter();

        try {
            // 1. 加载驱动并获取数据库连接（参考 StudentQuery.java 的写法）
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 2. 执行查询
            String sql = "SELECT user_id, username, password, user_role FROM sys_user WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                SysUser user = new SysUser();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                String storedPassword = rs.getString("password");
                user.setUserRole(rs.getString("user_role"));

                // 3. 使用 BCrypt 校验密码
                if (BCrypt.checkpw(password, storedPassword)) {
                    // --- 登录成功 ---

                    // A. 写入 Session
                    HttpSession session = request.getSession();
                    session.setAttribute("currentUser", user);
                    session.setAttribute("loginTimestamp", System.currentTimeMillis());

                    // B. 写入 Cookie
                    addCookie(response, "username", username, 3600);
                    addCookie(response, "loginIp", request.getRemoteAddr(), 3600);
                    addCookie(response, "loginTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 3600);

                    // C. 返回 JSON 成功信息
                    out.print("{\"code\": 200, \"msg\": \"登录成功\", \"role\": \"" + user.getUserRole() + "\"}");
                    return;
                }
            }

            // --- 登录失败（用户不存在或密码错误） ---
            out.print("{\"code\": 401, \"msg\": \"用户名或密码错误\"}");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"数据库驱动加载失败\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"系统繁忙，请稍后再试\"}");
        } finally {
            // 4. 关闭资源（参考 StudentQuery.java 的写法）
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("login.html");
    }

    /**
     * 辅助方法：添加 Cookie 并自动进行 URL 编码
     */
    private void addCookie(HttpServletResponse resp, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }
}