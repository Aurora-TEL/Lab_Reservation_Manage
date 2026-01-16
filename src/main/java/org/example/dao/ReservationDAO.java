package org.example.dao;

import org.example.model.Reservation; // 确保你有这个实体类
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationDAO extends BaseDAO {

    // 1. 查询所有待审核的预约 (PENDING)
    // 返回 Map 是为了方便 UI 显示具体的中文名称，而不是 ID
    public List<Map<String, Object>> getPendingReservations() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // 核心难点：5表联合查询 (预约 + 老师 + 班级 + 课程 + 实验室)
            String sql = "SELECT r.id, u.username AS teacher_name, c.class_name, " +
                    "co.course_name, l.room_number, s.date_slot, s.period, r.create_time " +
                    "FROM sys_reservation r " +
                    "JOIN sys_user u ON r.teacher_id = u.id " +
                    "JOIN sys_class c ON r.class_id = c.id " +
                    "JOIN sys_course co ON r.course_id = co.id " +
                    "JOIN sys_lab_slot s ON r.slot_id = s.id " +
                    "JOIN sys_lab l ON s.lab_id = l.id " +
                    "WHERE r.status = 'PENDING' " +
                    "ORDER BY r.create_time DESC";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("teacher_name", rs.getString("teacher_name"));
                map.put("class_name", rs.getString("class_name"));
                map.put("course_name", rs.getString("course_name"));
                map.put("room_number", rs.getString("room_number"));
                map.put("time_info", rs.getDate("date_slot") + " " + rs.getString("period"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }

    // 2. 更新预约状态 (通过 APPROVED / 驳回 REJECTED)
    public boolean updateStatus(int reservationId, String newStatus) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String sql = "UPDATE sys_reservation SET status = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }

    /**
     * 多条件组合查询预约历史
     * @param teacherId 如果传入非null，则只查该老师的记录(教师端模式)；如果null，查所有(管理员模式)
     * @param labName 实验室名称关键字 (模糊查询)
     * @param status 状态 (ALL, PENDING, APPROVED, REJECTED)
     */
    public List<Map<String, Object>> searchHistory(Integer teacherId, String labName, String status) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            StringBuilder sql = new StringBuilder(
                    "SELECT r.id, u.username AS teacher_name, l.room_number, s.date_slot, s.period, " +
                            "c.class_name, co.course_name, r.status, r.create_time " +
                            "FROM sys_reservation r " +
                            "JOIN sys_user u ON r.teacher_id = u.id " +
                            "JOIN sys_lab_slot s ON r.slot_id = s.id " +
                            "JOIN sys_lab l ON s.lab_id = l.id " +
                            "JOIN sys_class c ON r.class_id = c.id " +
                            "JOIN sys_course co ON r.course_id = co.id " +
                            "WHERE 1=1 "
            );

            // --- 动态拼接 SQL ---
            if (teacherId != null) {
                sql.append(" AND r.teacher_id = ? ");
            }
            if (labName != null && !labName.trim().isEmpty()) {
                sql.append(" AND l.room_number LIKE ? ");
            }
            if (status != null && !"ALL".equals(status)) {
                sql.append(" AND r.status = ? ");
            }

            sql.append(" ORDER BY r.create_time DESC");

            ps = conn.prepareStatement(sql.toString());

            // --- 动态设置参数 ---
            int idx = 1;
            if (teacherId != null) {
                ps.setInt(idx++, teacherId);
            }
            if (labName != null && !labName.trim().isEmpty()) {
                ps.setString(idx++, "%" + labName + "%");
            }
            if (status != null && !"ALL".equals(status)) {
                ps.setString(idx++, status);
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("teacher_name", rs.getString("teacher_name"));
                map.put("room_number", rs.getString("room_number"));
                map.put("date_info", rs.getDate("date_slot") + " " + rs.getString("period"));
                map.put("class_name", rs.getString("class_name"));
                map.put("course_name", rs.getString("course_name"));

                // 状态翻译 (可选，界面显示更友好)
                String st = rs.getString("status");
                map.put("status", st);

                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }
    // 查询指定班级的预约记录 (学生视图)
    public List<Map<String, Object>> getReservationsByClassId(int classId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // 只需要查询该班级ID相关的记录
            // 关联 sys_reservation 和 sys_user 获取老师名字
            String sql = "SELECT r.id, u.username AS teacher_name, l.room_number, s.date_slot, s.period, " +
                    "co.course_name, r.status " +
                    "FROM sys_reservation r " +
                    "JOIN sys_user u ON r.teacher_id = u.id " +
                    "JOIN sys_lab_slot s ON r.slot_id = s.id " +
                    "JOIN sys_lab l ON s.lab_id = l.id " +
                    "JOIN sys_course co ON r.course_id = co.id " +
                    "WHERE r.class_id = ? " +  // 核心过滤条件
                    "ORDER BY s.date_slot DESC";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, classId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("teacher_name", rs.getString("teacher_name"));
                map.put("room_number", rs.getString("room_number"));
                map.put("time_info", rs.getDate("date_slot") + " " + rs.getString("period"));
                map.put("course_name", rs.getString("course_name"));
                map.put("status", rs.getString("status"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }
}