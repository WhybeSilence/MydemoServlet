package com.wang.servlet.util; // 请确保包名与你项目结构一致

import java.sql.*;

public class DBUtil {

    // 1. 数据库连接配置
    // 注意：将 "shopdemo" 对应你的数据库名，password 修改为你本地的MySQL密码
    private static final String URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC";
    private static final String USER = "remote_user";
    private static final String PASSWORD = "512179588";

    // 2. 静态代码块：加载驱动（只需执行一次）
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("驱动加载失败，请检查是否引入了 mysql-connector-java jar包");
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接对象
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 关闭资源的通用方法
     * 按照 ResultSet -> Statement -> Connection 的顺序关闭
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}