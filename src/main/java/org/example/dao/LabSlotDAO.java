package org.example.dao;

import org.example.model.LabSlot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LabSlotDAO extends BaseDAO {
    // 1. 发布新的可用时间段
    public boolean addSlot(int labId, String dateStr, String period) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            // date_slot 对应数据库中的 DATE 类型
            String sql = "INSERT INTO sys_lab_slot (lab_id, date_slot, period, is_available) VALUES (?, CAST(? AS DATE), ?, TRUE)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, labId);
            ps.setString(2, dateStr); // 格式如 '2023-12-01'
            ps.setString(3, period);  // 例如 '上午' 或 '下午'
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }

    // 根据实验室ID查询所有已发布的时段
    public List<LabSlot> getSlotsByLab(int labId) {
        List<LabSlot> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // 按日期排序，方便查看
            String sql = "SELECT * FROM sys_lab_slot WHERE lab_id = ? ORDER BY date_slot ASC, period ASC";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, labId);
            rs = ps.executeQuery();
            while (rs.next()) {
                LabSlot slot = new LabSlot();
                slot.setId(rs.getInt("id"));
                slot.setLabId(rs.getInt("lab_id"));
                slot.setDateSlot(rs.getDate("date_slot"));
                slot.setPeriod(rs.getString("period"));
                // 注意：数据库里是 boolean，对应 Java 的 boolean
                slot.setAvailable(rs.getBoolean("is_available"));
                list.add(slot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }

    // 【可选功能】删除某个时段 (撤销发布)
    public boolean deleteSlot(int slotId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            // 注意：如果有外键约束（已被预约），这里可能会报错，需要先处理预约
            String sql = "DELETE FROM sys_lab_slot WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, slotId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // e.printStackTrace();
            // 如果报错，通常是因为该时段已经被预约了
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
}
