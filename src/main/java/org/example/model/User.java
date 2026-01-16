package org.example.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private Integer classId; // 新增：学生所属班级ID (Teacher/Admin 为 null)

    public User() {}

    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Generate Getters and Setters for all fields...
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public Integer getClassId() {return classId;}
    public void setClassId(Integer classId) {this.classId = classId;}
}