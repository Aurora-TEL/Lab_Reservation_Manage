package org.example.model;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private int slotId;
    private int teacherId; // 对应 teacher_id
    private int classId;   // 对应 class_id
    private int courseId;  // 对应 course_id
    private String status;
    private Timestamp createTime;

    // 建议增加额外的字段用于显示（数据库里只存ID，但界面要显示名字）
    // 这些字段在数据库表中不存在，但查询时会联表查出来塞进去
    private String teacherName;
    private String labName;
    private String className;

    public Reservation() {
    }

    public Reservation(int id, int slotId, int teacherId, int classId, int courseId, String status, Timestamp createTime, String teacherName, String labName, String className) {
        this.id = id;
        this.slotId = slotId;
        this.teacherId = teacherId;
        this.classId = classId;
        this.courseId = courseId;
        this.status = status;
        this.createTime = createTime;
        this.teacherName = teacherName;
        this.labName = labName;
        this.className = className;
    }

    // Generate Getters and Setters...
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public int getSlotId() {return slotId;}
    public void setSlotId(int slotId) {this.slotId = slotId;}
    public int getTeacherId() {return teacherId;}
    public void setTeacherId(int teacherId) {this.teacherId = teacherId;}
    public int getClassId() {return classId;}
    public void setClassId(int classId) {this.classId = classId;}
    public int getCourseId() {return courseId;}
    public void setCourseId(int courseId) {this.courseId = courseId;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public Timestamp getCreateTime() {return createTime;}
    public void setCreateTime(Timestamp createTime) {this.createTime = createTime;}
    public String getTeacherName() {return teacherName;}
    public void setTeacherName(String teacherName) {this.teacherName = teacherName;}
    public String getLabName() {return labName;}
    public void setLabName(String labName) {this.labName = labName;}
    public String getClassName() {return className;}
    public void setClassName(String className) {this.className = className;}

}
