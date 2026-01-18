package org.example.view.login;

import org.example.dao.UserDAO;
import org.example.model.User;
import org.example.view.admin.AdminMainFrame;
import org.example.view.student.StudentMainFrame;
import org.example.view.teacher.TeacherMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField userText;
    private JPasswordField passText;
    private UserDAO userDAO = new UserDAO(); // 准备好DAO

    public LoginFrame() {
        setTitle("系统登录");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中
        setLayout(null); // 简单起见，使用绝对布局

        initUI();
    }

    private void initUI() {
        // 1. 用户名标签和输入框
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setBounds(50, 50, 80, 25);
        add(userLabel);

        userText = new JTextField(20);
        userText.setBounds(130, 50, 160, 25);
        add(userText);

        // 2. 密码标签和输入框
        JLabel passLabel = new JLabel("密  码:");
        passLabel.setBounds(50, 90, 80, 25);
        add(passLabel);

        passText = new JPasswordField(20);
        passText.setBounds(130, 90, 160, 25);
        add(passText);

        // 3. 登录按钮 (调整一下位置)
        JButton loginBtn = new JButton("登录");
        loginBtn.setBounds(80, 150, 80, 30); // 向左移一点
        add(loginBtn);

        // 4. 【新增】注册按钮
        JButton regBtn = new JButton("注册");
        regBtn.setBounds(180, 150, 80, 30); // 放在登录按钮右边
        add(regBtn);

        // 4. 登录事件监听
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        // 5. 注册事件监听
        regBtn.addActionListener(e -> {
            // 弹出注册对话框
            new RegisterDialog(this).setVisible(true);
        });
    }

    private void doLogin() {
        String username = userText.getText();
        String password = new String(passText.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名或密码不能为空！");
            return;
        }

        // 调用后台查库
        User user = userDAO.login(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "登录失败，用户名或密码错误！");
        } else {
            // 登录成功，关闭当前窗口
            this.dispose();

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                new AdminMainFrame(user).setVisible(true);
            } else if ("TEACHER".equalsIgnoreCase(user.getRole())) {
                new TeacherMainFrame(user).setVisible(true);
            } else if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                // 【新增】跳转到学生界面
                new StudentMainFrame(user).setVisible(true);
            }
        }
    }
}
