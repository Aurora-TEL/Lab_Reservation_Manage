package org.example.view.admin;

import org.example.dao.ReservationDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AuditPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private ReservationDAO resDAO = new ReservationDAO();

    public AuditPanel() {
        setLayout(new BorderLayout());

        // 1. 顶部提示
        JLabel tipLabel = new JLabel(" 提示：以下是所有老师提交的待审核申请，请及时处理。");
        tipLabel.setForeground(Color.BLUE);
        add(tipLabel, BorderLayout.NORTH);

        // 2. 表格
        String[] cols = {"ID", "申请教师", "班级", "课程", "实验室", "预约时间"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override // 禁止编辑单元格
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. 底部操作按钮
        JPanel btnPanel = new JPanel();
        JButton btnApprove = new JButton("通过申请");
        JButton btnReject = new JButton("驳回申请");
        JButton btnRefresh = new JButton("刷新列表");

        btnApprove.setForeground(new Color(0, 100, 0)); // 绿色字
        btnReject.setForeground(Color.RED);             // 红色字

        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        // --- 事件监听 ---

        // 刷新
        btnRefresh.addActionListener(e -> loadData());

        // 通过
        btnApprove.addActionListener(e -> processReservation("APPROVED"));

        // 驳回
        btnReject.addActionListener(e -> processReservation("REJECTED"));

        // 初始化加载
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Map<String, Object>> list = resDAO.getPendingReservations();
        for (Map<String, Object> map : list) {
            tableModel.addRow(new Object[]{
                    map.get("id"),
                    map.get("teacher_name"),
                    map.get("class_name"),
                    map.get("course_name"),
                    map.get("room_number"),
                    map.get("time_info")
            });
        }
    }

    private void processReservation(String status) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一条记录！");
            return;
        }

        int resId = (int) tableModel.getValueAt(row, 0);
        String actionName = status.equals("APPROVED") ? "通过" : "驳回";

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要 " + actionName + " 这条申请吗？",
                "审核确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (resDAO.updateStatus(resId, status)) {
                JOptionPane.showMessageDialog(this, "操作成功！");
                loadData(); // 成功后刷新，该条记录应消失
            } else {
                JOptionPane.showMessageDialog(this, "操作失败，请重试。");
            }
        }
    }
}
