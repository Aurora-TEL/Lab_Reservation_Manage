package org.example.view.admin;

import org.example.model.User;
import org.example.view.components.HistoryPanel; // 确保你有这个类
import org.example.view.login.LoginFrame;
/*import org.example.view.admin.AuditPanel;
import org.example.view.admin.ClassManagePanel;
import org.example.view.admin.LabManagePanel;
import org.example.view.admin.UserManagePanel;*/

import javax.swing.*;
import java.awt.*;

public class AdminMainFrame extends JFrame {

    public AdminMainFrame(User user) {
        setTitle("实验室预约管理系统 - 管理员控制台");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. 顶部导航栏 (欢迎语 + 退出按钮) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 增加内边距
        topPanel.setBackground(new Color(240, 240, 240)); // 浅灰背景

        JLabel welcomeLabel = new JLabel("欢迎您，管理员: " + user.getUsername());
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(50, 50, 50));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("退出登录");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(new Color(220, 50, 50)); // 红色按钮
        btnLogout.setForeground(Color.WHITE);

        btnLogout.addActionListener(e -> {
            // 关闭当前窗口
            this.dispose();
            // 打开登录窗口
            new LoginFrame().setVisible(true);
        });
        topPanel.add(btnLogout, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. 中间功能区 (Tab选项卡) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 添加各个功能面板
        tabbedPane.addTab(" 实验室管理 ", new LabManagePanel());
        tabbedPane.addTab(" 班级管理 ", new ClassManagePanel());
        tabbedPane.addTab(" 用户/教师管理 ", new UserManagePanel());
        tabbedPane.addTab(" 预约信息审核 ", new AuditPanel());

        // 全校历史记录 (传入 user 对象，HistoryPanel 会自动识别是管理员)
        tabbedPane.addTab(" 全校预约历史 ", new HistoryPanel(user));

        add(tabbedPane, BorderLayout.CENTER);

        // --- 3. 底部状态栏 ---
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(new JLabel(" 系统状态: 正常运行 | 当前时间: " + new java.util.Date()));
        add(statusBar, BorderLayout.SOUTH);
    }
}