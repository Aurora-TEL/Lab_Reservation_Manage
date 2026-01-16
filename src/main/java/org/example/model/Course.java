package org.example.model;

public class Course {
    private int id;
    private String courseName;
    private String description;

    public Course() {
    }

    public Course(int id, String courseName, String description) {
        this.id = id;
        this.courseName = courseName;
        this.description = description;
    }

    // Generate Getters and Setters...
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getCourseName() {return courseName;}
    public void setCourseName(String courseName) {this.courseName = courseName;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    @Override
    public String toString() {
        return this.courseName; // 下拉框只显示课程名
    }

}
