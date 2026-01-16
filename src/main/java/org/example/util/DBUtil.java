/**
 * 核心：管理数据库连接、释放资源
 */

package org.example.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;

public class DBUtil {
    private static Properties properties = new Properties();

    // 1. 静态代码块：类加载时读取配置文件
    static {
        try {
            // 从 resources 目录下加载 db.properties
            InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            properties.load(in);
            // 加载驱动
            Class.forName(properties.getProperty("driver"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("加载数据库配置失败");
        }
    }

    // 2. 获取数据库连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
        );
    }

//    // 简单的测试方法
//    public static void main(String[] args) {
//        try {
//            Connection conn = getConnection();
//            System.out.println("数据库连接成功！" + conn);
//            conn.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println("连接失败，请检查URL、用户名或密码。");
//        }
//    }
}

