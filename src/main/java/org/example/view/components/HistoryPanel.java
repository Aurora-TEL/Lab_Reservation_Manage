package org.example.view.components;

import org.example.dao.ReservationDAO;
import org.example.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class HistoryPanel extends JPanel {
    private JTextField txtLabSearch;
    private JComboBox<String> statusCombo;
    private JTable table;
    private DefaultTableModel tableModel;

    private ReservationDAO resDAO = new ReservationDAO();
    private User currentUser; // 当前登录用户

    public HistoryPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // --- 1. 顶部筛选区 ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("查询条件"));

        txtLabSearch = new JTextField(10);
        statusCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "REJECTED"});
        JButton btnSearch = new JButton("查询");

        searchPanel.add(new JLabel("实验室名称:"));
        searchPanel.add(txtLabSearch);
        searchPanel.add(new JLabel("状态:"));
        searchPanel.add(statusCombo);
        searchPanel.add(btnSearch);

        add(searchPanel, BorderLayout.NORTH);

        // --- 2. 表格显示区 ---
        // 根据角色决定列名
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        String[] columns;
        if (isAdmin) {
            columns = new String[]{"ID", "申请人", "实验室", "使用时间", "班级", "课程", "当前状态"};
        } else {
            columns = new String[]{"ID", "实验室", "使用时间", "班级", "课程", "当前状态"};
        }

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 3. 底部操作按钮 (修改这里) ---
        JPanel btnPanel = new JPanel();
        JButton btnCancel = new JButton("取消预约");
        btnCancel.setForeground(Color.RED);

        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        // --- 4. 事件监听 ---
        btnSearch.addActionListener(e -> loadData());

        // 【新增】5.取消预约逻辑
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一条记录！");
                return;
            }

            // 获取当前状态 (表格最后一列)
            String status = (String) tableModel.getValueAt(row, tableModel.getColumnCount() - 1);

            // 规则：只有 PENDING 状态可以取消
            if (!"PENDING".equals(status)) {
                JOptionPane.showMessageDialog(this, "只能取消【待审核】的预约！\n已通过或已驳回的记录无法取消。");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "确定要撤回这条预约申请吗？", "确认取消", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int resId = (int) tableModel.getValueAt(row, 0); // 获取第一列 ID

                // 复用 ReservationDAO 中的 updateStatus 方法
                // 我们将状态更新为 'CANCELLED'
                if (resDAO.updateStatus(resId, "CANCELLED")) {
                    JOptionPane.showMessageDialog(this, "预约已取消！");
                    loadData(); // 刷新表格
                } else {
                    JOptionPane.showMessageDialog(this, "操作失败，请重试。");
                }
            }
        });

        // 初始加载
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);

        String labName = txtLabSearch.getText();
        String status = (String) statusCombo.getSelectedItem();

        // 关键逻辑：如果是 ADMIN，传入 null (查所有人)；如果是 TEACHER，传入 ID (只查自己)
        Integer queryId = "ADMIN".equalsIgnoreCase(currentUser.getRole()) ? null : currentUser.getId();

        List<Map<String, Object>> list = resDAO.searchHistory(queryId, labName, status);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        for (Map<String, Object> map : list) {
            if (isAdmin) {
                tableModel.addRow(new Object[]{
                        map.get("id"),
                        map.get("teacher_name"), // 管理员多看这一列
                        map.get("room_number"),
                        map.get("date_info"),
                        map.get("class_name"),
                        map.get("course_name"),
                        map.get("status")
                });
            } else {
                tableModel.addRow(new Object[]{
                        map.get("id"),
                        map.get("room_number"),
                        map.get("date_info"),
                        map.get("class_name"),
                        map.get("course_name"),
                        map.get("status")
                });
            }
        }
    }
}
