package org.example.dao;

import org.example.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO extends BaseDAO {

    /**
     * 登录验证
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回User对象(包含id, role等信息)，失败返回null
     */
    public User login(String username, String password) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = getConnection();
            String sql = "SELECT * FROM sys_user WHERE username = ? AND password = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role")); // 'ADMIN' or 'TEACHER'
                // 在 rs.next() 代码块中增加：
                user.setClassId(rs.getInt("class_id"));
                if (rs.wasNull()) user.setClassId(null); // 处理 null 值
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return user;
    }

    public boolean addUser(String username, String password, String role) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            // 对应 sys_user 表结构 [cite: 1]
            String sql = "INSERT INTO sys_user (username, password, role) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role); // 这里传入 'TEACHER'
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
    // 专门用于添加学生账号
    public boolean addStudent(String username, String password, int classId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            // role 固定为 'STUDENT'
            String sql = "INSERT INTO sys_user (username, password, role, class_id) VALUES (?, ?, 'STUDENT', ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
    // 1. 检查用户名是否存在
    public boolean isUsernameExists(String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "SELECT count(*) FROM sys_user WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // 大于0说明已存在
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return false;
    }

    // 2. 注册新用户 (支持学生带班级ID，老师班级ID为null)
    public boolean register(String username, String password, String role, Integer classId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String sql = "INSERT INTO sys_user (username, password, role, class_id) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            // 处理 classId (如果是 null，就设为 SQL NULL)
            if (classId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, classId);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
    /**
     * 获取所有教师和学生列表，用于管理界面展示
     * 排序规则：先展示 TEACHER，后展示 STUDENT (利用 ORDER BY role DESC，因为 T 在 S 后面)
     */
    public List<Map<String, Object>> getAllUsersForDisplay() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // 关联 sys_user 和 sys_class
            // 使用 LEFT JOIN 是因为老师没有班级，如果不加 LEFT，老师会被过滤掉
            String sql = "SELECT u.id, u.username, u.role, c.class_name " +
                    "FROM sys_user u " +
                    "LEFT JOIN sys_class c ON u.class_id = c.id " +
                    "WHERE u.role IN ('TEACHER', 'STUDENT') " +
                    "ORDER BY u.role DESC, u.id ASC";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("username", rs.getString("username"));

                String role = rs.getString("role");
                map.put("role", "TEACHER".equals(role) ? "教师" : "学生");

                // 如果班级名为空，显示 "-"
                String className = rs.getString("class_name");
                map.put("class_name", className == null ? "-" : className);

                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }
    // 【新增】删除用户
    public boolean deleteUser(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            // 如果该用户有预约记录，删除可能会失败
            String sql = "DELETE FROM sys_user WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
    // 1. 修改基础信息 (用户名、班级)
    public boolean updateUser(int userId, String newUsername, Integer newClassId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String sql = "UPDATE sys_user SET username = ?, class_id = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newUsername);

            // 处理 classId 为 null 的情况 (老师/管理员)
            if (newClassId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, newClassId);
            }

            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }

    // 2. 修改/重置密码
    public boolean updatePassword(int userId, String newPassword) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String sql = "UPDATE sys_user SET password = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
}
