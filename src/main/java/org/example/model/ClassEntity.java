package org.example.model;

public class ClassEntity {
    private int id;
    private String className;
    private int studentCount;

    public ClassEntity() {
    }

    public ClassEntity(int id, String className, int studentCount) {
        this.id = id;
        this.className = className;
        this.studentCount = studentCount;
    }

    // Generate Getters and Setters...
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getClassName() {return className;}
    public void setClassName(String className) {this.className = className;}
    public int getStudentCount() {return studentCount;}
    public void setStudentCount(int studentCount) {this.studentCount = studentCount;}

    @Override
    public String toString() {
        // 下拉框会显示这个字符串，比如 "软件2101班 (45人)"
        return this.className + " (" + this.studentCount + "人)";
    }

}
