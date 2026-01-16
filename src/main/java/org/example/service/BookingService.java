package org.example.service;

import org.example.dao.BaseDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BookingService extends BaseDAO {

    /**
     * 核心逻辑：查询符合条件的可用实验室时段
     * 1. 实验室已由管理员发布 (sys_lab_slot) [cite: 4, 5]
     * 2. 实验室容量 >= 班级人数 (sys_lab.capacity >= sys_class.student_count) [cite: 3, 7]
     * 3. 该时段未被他人预约或预约被驳回 (不在 sys_reservation 中或状态为 REJECTED)
     */
    public List<Map<String, Object>> getAvailableSlots(int classId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            String sql = "SELECT s.id AS slot_id, l.room_number, l.capacity, s.date_slot, s.period " +
                    "FROM sys_lab_slot s " +
                    "JOIN sys_lab l ON s.lab_id = l.id " +
                    "WHERE l.capacity >= (SELECT student_count FROM sys_class WHERE id = ?) " + // 核查人数
                    "AND s.id NOT IN (SELECT slot_id FROM sys_reservation WHERE status IN ('PENDING', 'APPROVED')) " + // 核查占用
                    "ORDER BY s.date_slot ASC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("slot_id", rs.getInt("slot_id"));
                map.put("room_number", rs.getString("room_number"));
                map.put("date_slot", rs.getDate("date_slot"));
                map.put("period", rs.getString("period"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 修正后的提交预约方法：真正执行数据库插入
     */
    public boolean applyReservation(int slotId, int teacherId, int classId, int courseId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();

            // 1. 再次做一个防卫性检查：确保该 slot 真的还没被预约
            // (防止两人同时点提交的并发情况，虽然 swing 单机版概率小，但习惯要好)
            String checkSql = "SELECT count(*) FROM sys_reservation WHERE slot_id = ? AND status IN ('PENDING', 'APPROVED')";
            ps = conn.prepareStatement(checkSql);
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("预约失败：该时段刚刚已被抢占");
                return false;
            }
            // 关闭上一个 ResultSet 和 PreparedStatement
            rs.close();
            ps.close();

            // 2. 执行真正的插入
            String sql = "INSERT INTO sys_reservation (slot_id, teacher_id, class_id, course_id, status) VALUES (?, ?, ?, ?, 'PENDING')";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, slotId);
            ps.setInt(2, teacherId);
            ps.setInt(3, classId);
            ps.setInt(4, courseId);

            int rows = ps.executeUpdate();

            // 只有当受影响行数 > 0 时，才返回 true
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace(); // 打印报错信息，方便排查
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
}
