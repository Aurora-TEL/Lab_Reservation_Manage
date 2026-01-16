package org.example.dao;

import org.example.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseDAO {

    // 只有子类能调用连接
    protected Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    // 通用的关闭资源方法
    protected void closeAll(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}