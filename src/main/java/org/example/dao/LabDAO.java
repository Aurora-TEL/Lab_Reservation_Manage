package org.example.dao;

import org.example.model.Lab;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LabDAO extends BaseDAO {

    // 1. 多条件组合查询
    public List<Lab> searchLabs(String roomNumber, String minCapacity) {
        List<Lab> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            StringBuilder sql = new StringBuilder("SELECT * FROM sys_lab WHERE 1=1");
            if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                sql.append(" AND room_number LIKE ?");
            }
            if (minCapacity != null && !minCapacity.trim().isEmpty()) {
                sql.append(" AND capacity >= ?");
            }
            sql.append(" ORDER BY id ASC");

            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                ps.setString(idx++, "%" + roomNumber + "%");
            }
            if (minCapacity != null && !minCapacity.trim().isEmpty()) {
                ps.setInt(idx++, Integer.parseInt(minCapacity));
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Lab(rs.getInt("id"), rs.getString("room_number"), rs.getInt("capacity")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }

    // 2. 添加实验室
    // 修改后的 addLab：插入成功后返回新生成的 ID，失败返回 -1
    public int addLabReturnId(Lab lab) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            conn = getConnection();
            String sql = "INSERT INTO sys_lab (room_number, capacity) VALUES (?, ?)";

            // 关键修正：添加 Statement.RETURN_GENERATED_KEYS 参数
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, lab.getRoomNumber());
            ps.setInt(2, lab.getCapacity());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                // 获取数据库自动生成的 ID
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1); // 拿到 ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return generatedId;
    }
    // 【新增】删除实验室
    public boolean deleteLab(int labId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            // 注意：如果该实验室有已发布的时间段(sys_lab_slot)，数据库会报错，这是保护机制
            String sql = "DELETE FROM sys_lab WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, labId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // e.printStackTrace(); // 可以打印出来调试
            // 如果报错，通常是因为违反外键约束（FK constraint）
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
}