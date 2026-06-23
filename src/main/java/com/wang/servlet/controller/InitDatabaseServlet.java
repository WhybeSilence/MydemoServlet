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

@WebServlet("/initDb")
public class InitDatabaseServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://192.168.56.1:3306/shopdemo?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8";
    private static final String USER = "remote_user";
    private static final String PASS = "512179588";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html><body><h2>数据库初始化中...</h2>");

        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            // 1. 创建 sys_user 表
            out.println("<p>创建 sys_user 表...</p>");
            String createSysUser = "CREATE TABLE IF NOT EXISTS sys_user (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "user_role VARCHAR(20) NOT NULL DEFAULT 'user'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(createSysUser);
            out.println("<p style='color:green'>sys_user 表创建成功</p>");

            // 2. 创建 user_profile 表
            out.println("<p>创建 user_profile 表...</p>");
            String createUserProfile = "CREATE TABLE IF NOT EXISTS user_profile (" +
                    "profile_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "avatar_url VARCHAR(500) DEFAULT ''," +
                    "bio VARCHAR(500) DEFAULT ''," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(createUserProfile);
            out.println("<p style='color:green'>user_profile 表创建成功</p>");

            // 3. 创建 shop 表
            out.println("<p>创建 shop 表...</p>");
            String createShop = "CREATE TABLE IF NOT EXISTS shop (" +
                    "shop_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "owner_id INT NOT NULL," +
                    "shop_name VARCHAR(100) NOT NULL," +
                    "description VARCHAR(500) DEFAULT ''," +
                    "shop_img VARCHAR(500) DEFAULT ''," +
                    "status INT DEFAULT 1," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (owner_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(createShop);
            out.println("<p style='color:green'>shop 表创建成功</p>");

            // 4. 创建 product 表
            out.println("<p>创建 product 表...</p>");
            String createProduct = "CREATE TABLE IF NOT EXISTS product (" +
                    "product_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "shop_id INT NOT NULL," +
                    "name VARCHAR(100) NOT NULL," +
                    "price DECIMAL(10,2) DEFAULT 0.00," +
                    "stock INT DEFAULT 0," +
                    "description VARCHAR(500) DEFAULT ''," +
                    "preview_url VARCHAR(500) DEFAULT ''," +
                    "audit_status INT DEFAULT 0," +
                    "reject_reason VARCHAR(500) DEFAULT ''," +
                    "upload_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (shop_id) REFERENCES shop(shop_id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(createProduct);
            out.println("<p style='color:green'>product 表创建成功</p>");

            // 5. 创建 wishlist 表
            out.println("<p>创建 wishlist 表...</p>");
            String createWishlist = "CREATE TABLE IF NOT EXISTS wishlist (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "add_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_user_product (user_id, product_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(createWishlist);
            out.println("<p style='color:green'>wishlist 表创建成功</p>");

            // 6. 创建 system_message 表
            out.println("<p>创建 system_message 表...</p>");
            String createSystemMessage = "CREATE TABLE IF NOT EXISTS system_message (" +
                    "msg_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "receiver_id INT NOT NULL," +
                    "content VARCHAR(500) NOT NULL," +
                    "is_read INT DEFAULT 0," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (receiver_id) REFERENCES sys_user(user_id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(createSystemMessage);
            out.println("<p style='color:green'>system_message 表创建成功</p>");

            // ==================== 插入测试数据 ====================
            out.println("<hr><h3>插入测试数据...</h3>");

            // 检查是否已有用户数据
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sys_user");
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();

            if (userCount == 0) {
                out.println("<p>插入测试用户...</p>");

                // 插入管理员
                String adminPwd = BCrypt.hashpw("admin123", BCrypt.gensalt());
                String insertAdmin = "INSERT INTO sys_user (username, password, user_role, create_time) VALUES " +
                        "('admin', '" + adminPwd + "', 'admin', NOW())";
                stmt.execute(insertAdmin, Statement.RETURN_GENERATED_KEYS);
                rs = stmt.getGeneratedKeys();
                int adminId = 0;
                if (rs.next()) adminId = rs.getInt(1);
                rs.close();

                // 插入店主1
                String owner1Pwd = BCrypt.hashpw("owner123", BCrypt.gensalt());
                String insertOwner1 = "INSERT INTO sys_user (username, password, user_role, create_time) VALUES " +
                        "('shopowner1', '" + owner1Pwd + "', 'shop_owner', NOW())";
                stmt.execute(insertOwner1, Statement.RETURN_GENERATED_KEYS);
                rs = stmt.getGeneratedKeys();
                int owner1Id = 0;
                if (rs.next()) owner1Id = rs.getInt(1);
                rs.close();

                // 插入店主2
                String owner2Pwd = BCrypt.hashpw("owner123", BCrypt.gensalt());
                String insertOwner2 = "INSERT INTO sys_user (username, password, user_role, create_time) VALUES " +
                        "('shopowner2', '" + owner2Pwd + "', 'shop_owner', NOW())";
                stmt.execute(insertOwner2, Statement.RETURN_GENERATED_KEYS);
                rs = stmt.getGeneratedKeys();
                int owner2Id = 0;
                if (rs.next()) owner2Id = rs.getInt(1);
                rs.close();

                // 插入普通用户
                String userPwd = BCrypt.hashpw("user123", BCrypt.gensalt());
                String insertUser = "INSERT INTO sys_user (username, password, user_role, create_time) VALUES " +
                        "('testuser', '" + userPwd + "', 'user', NOW())";
                stmt.execute(insertUser, Statement.RETURN_GENERATED_KEYS);
                rs = stmt.getGeneratedKeys();
                int userId = 0;
                if (rs.next()) userId = rs.getInt(1);
                rs.close();

                out.println("<p style='color:green'>测试用户插入成功</p>");
                out.println("<p>管理员账号: admin / admin123</p>");
                out.println("<p>店主账号: shopowner1 / owner123</p>");
                out.println("<p>店主账号: shopowner2 / owner123</p>");
                out.println("<p>普通用户: testuser / user123</p>");

                // 插入用户资料
                String insertProfiles = "INSERT INTO user_profile (user_id, avatar_url, bio) VALUES " +
                        "(" + adminId + ", 'https://picsum.photos/200/200?random=admin', '系统管理员，负责商品审核与平台管理')," +
                        "(" + owner1Id + ", 'https://picsum.photos/200/200?random=owner1', '清风解忧小店店主，用心经营每一件商品')," +
                        "(" + owner2Id + ", 'https://picsum.photos/200/200?random=owner2', '晚风杂货小铺店主，收集人间温柔')," +
                        "(" + userId + ", 'https://picsum.photos/200/200?random=user', '热爱生活的普通用户')";
                stmt.execute(insertProfiles);
                out.println("<p style='color:green'>用户资料插入成功</p>");

                // 插入店铺
                String insertShops = "INSERT INTO shop (owner_id, shop_name, description, shop_img, status) VALUES " +
                        "(" + owner1Id + ", '清风解忧小店', '主营情绪疏导、暖心好物，陪伴每一位疲惫的旅人。本店坚持用心服务，售卖治愈系小物件、情绪语录、减压周边，愿每一位到访者都能卸下烦恼，拥抱美好。', 'https://picsum.photos/400/300?random=shop1', 1)," +
                        "(" + owner2Id + ", '晚风杂货小铺', '收集人间温柔，贩卖片刻安宁，在这里放下所有烦恼。精选治愈系商品，为你的生活增添一抹温暖。', 'https://picsum.photos/400/300?random=shop2', 1)";
                stmt.execute(insertShops);
                out.println("<p style='color:green'>店铺数据插入成功</p>");

                // 插入商品（已审核通过的）
                String insertProducts = "INSERT INTO product (shop_id, name, price, stock, description, preview_url, audit_status, upload_time) VALUES " +
                        "(1, '暖心手账本', 29.90, 50, '精致治愈手账本，记录每日心情与感悟，纸质顺滑，装帧精美。', 'https://picsum.photos/200/200?random=p1', 1, NOW())," +
                        "(1, '解压毛绒玩偶', 45.00, 30, '柔软毛绒小玩偶，不开心时抱一抱，缓解压力，陪伴独处时光。', 'https://picsum.photos/200/200?random=p2', 1, NOW())," +
                        "(1, '治愈系香薰', 68.00, 0, '淡香舒缓香薰，放松身心，助眠减压，营造温馨氛围。', 'https://picsum.photos/200/200?random=p3', 1, NOW())," +
                        "(2, '复古明信片套装', 19.90, 100, '精美的复古明信片，写下你的心情，寄给远方的朋友。', 'https://picsum.photos/200/200?random=p4', 1, NOW())," +
                        "(2, '云朵夜灯', 55.00, 25, '温柔的云朵造型夜灯，暖黄光芒伴你入眠。', 'https://picsum.photos/200/200?random=p5', 1, NOW())," +
                        "(2, '星空投影灯', 89.00, 15, '把星空搬进房间，浪漫氛围一键开启。', 'https://picsum.photos/200/200?random=p6', 1, NOW())";
                stmt.execute(insertProducts);
                out.println("<p style='color:green'>商品数据插入成功</p>");

                // 插入待审核商品
                String insertPending = "INSERT INTO product (shop_id, name, price, stock, description, preview_url, audit_status, upload_time) VALUES " +
                        "(1, '新品尝鲜-花茶礼盒', 39.90, 20, '精选花茶组合，养生又美味，送礼自用两相宜。', 'https://picsum.photos/200/200?random=p7', 0, NOW())," +
                        "(2, '神秘盲盒惊喜', 49.90, 50, '随机惊喜好物，每次都有新期待！', 'https://picsum.photos/200/200?random=p8', 0, NOW())";
                stmt.execute(insertPending);
                out.println("<p style='color:green'>待审核商品插入成功</p>");

                // 插入愿望单数据
                String insertWishlist = "INSERT INTO wishlist (user_id, product_id) VALUES " +
                        "(" + userId + ", 1)," +
                        "(" + userId + ", 4)";
                try {
                    stmt.execute(insertWishlist);
                    out.println("<p style='color:green'>愿望单数据插入成功</p>");
                } catch (SQLException e) {
                    out.println("<p style='color:orange'>愿望单可能已存在: " + e.getMessage() + "</p>");
                }

            } else {
                out.println("<p style='color:orange'>数据库中已存在用户数据，跳过初始化</p>");
            }

            out.println("<hr><h2 style='color:green'>数据库初始化完成！</h2>");
            out.println("<p><a href='home.html'>返回首页</a></p>");

        } catch (ClassNotFoundException e) {
            out.println("<p style='color:red'>驱动加载失败: " + e.getMessage() + "</p>");
            e.printStackTrace();
        } catch (SQLException e) {
            out.println("<p style='color:red'>数据库操作异常: " + e.getMessage() + "</p>");
            e.printStackTrace();
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        out.println("</body></html>");
    }
}
