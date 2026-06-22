package com.wang.servlet.controller;

import com.wang.servlet.entity.SysUser;
import com.wang.servlet.util.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet({"/login", "/checkLogin", "/logout"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        
        if ("/logout".equals(servletPath)) {
            handleLogout(request, response);
            return;
        }
        
        // 处理登录逻辑
        handleLogin(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        
        if ("/checkLogin".equals(servletPath)) {
            handleCheckLogin(request, response);
            return;
        }
        
        // 默认跳转到登录页面
        response.sendRedirect("login.html");
    }

    private void handleCheckLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            SysUser user = (SysUser) session.getAttribute("currentUser");
            if (user != null) {
                out.print("{\"code\": 200, \"username\": \"" + user.getUsername() + "\", \"role\": \"" + user.getUserRole() + "\"}");
                return;
            }
        }
        
        out.print("{\"code\": 401, \"msg\": \"未登录\"}");
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // 清除 cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        
        out.print("{\"code\": 200, \"msg\": \"退出成功\"}");
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // 【关键】设置响应类型为 JSON
        response.setContentType("application/json;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        PrintWriter out = response.getWriter();

        try {
            conn = DBUtil.getConnection();
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

                // 校验密码
                if (BCrypt.checkpw(password, storedPassword)) {
                    // --- 登录成功 ---

                    // 1. 写入 Session
                    HttpSession session = request.getSession();
                    session.setAttribute("currentUser", user);
                    session.setAttribute("loginTimestamp", System.currentTimeMillis());

                    // 2. 写入 Cookie
                    addCookie(response, "username", username, 3600);
                    addCookie(response, "loginIp", request.getRemoteAddr(), 3600);
                    addCookie(response, "loginTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 3600);

                    // 3. 返回 JSON 成功信息 (不再直接重定向)
                    out.print("{\"code\": 200, \"msg\": \"登录成功\", \"role\": \"" + user.getUserRole() + "\"}");
                    return;
                }
            }

            // --- 登录失败 ---
            out.print("{\"code\": 401, \"msg\": \"用户名或密码错误\"}");

        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"code\": 500, \"msg\": \"系统繁忙，请稍后再试\"}");
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    private void addCookie(HttpServletResponse resp, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }
}