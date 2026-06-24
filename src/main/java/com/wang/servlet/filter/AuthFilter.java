package com.wang.servlet.filter;

import com.wang.servlet.entity.SysUser;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthFilter implements Filter {

    private static final int SESSION_TIMEOUT = 30 * 60 * 1000;

    private static final List<String> ALLOWED_PATHS = Arrays.asList(
            "/home.html",
            "/shopDetail.html",
            "/login",
            "/register",
            "/shopList",
            "/shopDetail",
            "/currentUser",
            "/initDb",
            "/logout"
    );

    private static final List<String> STATIC_EXTENSIONS = Arrays.asList(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico", ".webp"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = path.substring(contextPath.length());

        if (relativePath.equals("/") || relativePath.isEmpty()) {
            relativePath = "/home.html";
        }

        if (isStaticResource(relativePath) || isAllowedPath(relativePath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            sendUnauthorized(resp, req);
            return;
        }

        Long loginTime = (Long) session.getAttribute("loginTimestamp");
        if (loginTime != null) {
            long now = System.currentTimeMillis();
            if (now - loginTime > SESSION_TIMEOUT) {
                session.invalidate();
                sendUnauthorized(resp, req);
                return;
            }
        }
        session.setAttribute("lastActiveTime", System.currentTimeMillis());

        SysUser user = (SysUser) session.getAttribute("currentUser");
        if (user != null && user.getUserRole() != null) {
            if (relativePath.startsWith("/admin/")) {
                if (!"admin".equals(user.getUserRole())) {
                    sendForbidden(resp);
                    return;
                }
            } else if (relativePath.startsWith("/shopOwner/")) {
                if (!"shop_owner".equals(user.getUserRole())) {
                    sendForbidden(resp);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        for (String allowed : ALLOWED_PATHS) {
            if (path.equals(allowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticResource(String path) {
        for (String ext : STATIC_EXTENSIONS) {
            if (path.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private void sendUnauthorized(HttpServletResponse resp, HttpServletRequest req) throws IOException {
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
        String accept = req.getHeader("Accept");
        boolean isJsonRequest = (accept != null && accept.contains("application/json"));

        if (isJsonRequest || req.getMethod().equalsIgnoreCase("POST")
                || req.getMethod().equalsIgnoreCase("DELETE")
                || req.getMethod().equalsIgnoreCase("PUT")) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"code\": 401, \"msg\": \"未登录或登录已过期\"}");
        } else {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().print(
                    "<script>" +
                            "alert('请先登录');" +
                            "window.location.href = '" + req.getContextPath() + "/home.html';" +
                            "</script>"
            );
        }
    }

    private void sendForbidden(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().print("{\"code\": 403, \"msg\": \"无权限访问\"}");
    }

    @Override
    public void destroy() {
    }
}
