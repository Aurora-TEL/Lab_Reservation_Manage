package org.example.view.teacher;

import org.example.model.User;
import org.example.view.components.HistoryPanel;
import org.example.view.login.LoginFrame;
import org.example.view.teacher.BookingPanel;

import javax.swing.*;
import java.awt.*;

public class TeacherMainFrame extends JFrame {

    public TeacherMainFrame(User user) {
        setTitle("实验室预约系统 - 教师端");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. 顶部导航栏 ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(245, 245, 245));

        JLabel welcomeLabel = new JLabel("欢迎您，教师: " + user.getUsername());
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("退出登录");
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要退出登录吗？", "确认退出", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginFrame().setVisible(true);
            }
        });
        topPanel.add(btnLogout, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. 中间功能区 ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 预约面板 (传入 ID)
        tabs.addTab(" 实验室预约 ", new BookingPanel(user.getId()));

        // 历史记录面板 (传入 user)
        tabs.addTab(" 我的预约记录 ", new HistoryPanel(user));

        add(tabs, BorderLayout.CENTER);
    }
}