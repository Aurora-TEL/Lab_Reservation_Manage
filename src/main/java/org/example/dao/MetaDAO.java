package org.example.dao;

import org.example.model.ClassEntity;
import org.example.model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MetaDAO extends BaseDAO {

    // --- 班级管理 (sys_class) ---

    public List<ClassEntity> getAllClasses() {
        List<ClassEntity> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "SELECT * FROM sys_class ORDER BY id ASC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                ClassEntity ce = new ClassEntity();
                ce.setId(rs.getInt("id"));
                ce.setClassName(rs.getString("class_name"));
                ce.setStudentCount(rs.getInt("student_count"));
                list.add(ce);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }

    public boolean addClass(String name, int count) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String sql = "INSERT INTO sys_class (class_name, student_count) VALUES (?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setInt(2, count);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }

    // --- 课程管理 (sys_course) ---

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "SELECT * FROM sys_course ORDER BY id ASC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setCourseName(rs.getString("course_name"));
                c.setDescription(rs.getString("description"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, ps, rs);
        }
        return list;
    }
    // 【新增】删除班级
    public boolean deleteClass(int classId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            // 如果该班级有学生(sys_user)或有预约(sys_reservation)，删除会失败
            String sql = "DELETE FROM sys_class WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        } finally {
            closeAll(conn, ps, null);
        }
    }
}
