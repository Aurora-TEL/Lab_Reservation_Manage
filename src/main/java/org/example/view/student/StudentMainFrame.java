package org.example.view.student;

import org.example.dao.ReservationDAO;
import org.example.model.User;
import org.example.view.login.LoginFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StudentMainFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private ReservationDAO resDAO = new ReservationDAO();
    private User studentUser;

    public StudentMainFrame(User user) {
        this.studentUser = user;
        setTitle("实验室课程查询 - 学生端");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. 顶部导航栏 ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("欢迎 " + user.getUsername() + " (学生)");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("返回登录");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        topPanel.add(btnLogout, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. 提示信息 ---
        JLabel tipLabel = new JLabel("  提示：以下为您所属班级的实验课安排表");
        tipLabel.setForeground(Color.BLUE);
        add(tipLabel, BorderLayout.SOUTH);

        // --- 3. 表格区域 ---
        String[] columns = {"任课老师", "课程名称", "实验室", "上课时间", "当前状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25); // 稍微调高行高好看点
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 加载数据
        loadData();
    }

    private void loadData() {
        if (studentUser.getClassId() == null || studentUser.getClassId() == 0) {
            JOptionPane.showMessageDialog(this, "当前账号数据异常（未绑定班级），无法查询！");
            return;
        }

        // 调用 ReservationDAO 查询本班级的课表
        List<Map<String, Object>> list = resDAO.getReservationsByClassId(studentUser.getClassId());

        for (Map<String, Object> map : list) {
            tableModel.addRow(new Object[]{
                    map.get("teacher_name"),
                    map.get("course_name"),
                    map.get("room_number"),
                    map.get("time_info"),
                    map.get("status")
            });
        }
    }
}