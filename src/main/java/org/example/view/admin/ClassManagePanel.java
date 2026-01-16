package org.example.view.admin;

import org.example.dao.MetaDAO; // 假设你在MetaDAO中处理班级和课程
import org.example.model.ClassEntity;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClassManagePanel extends JPanel {
    private JTable classTable;
    private DefaultTableModel tableModel;
    private MetaDAO metaDAO = new MetaDAO();

    public ClassManagePanel() {
        setLayout(new BorderLayout());

        // --- 1. 顶部 ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("班级管理模块 (用于核查人数)"));
        add(topPanel, BorderLayout.NORTH);

        // --- 2. 表格 (sys_class) ---
        String[] columns = {"ID", "班级名称", "学生人数"};
        tableModel = new DefaultTableModel(columns, 0);
        classTable = new JTable(tableModel);
        add(new JScrollPane(classTable), BorderLayout.CENTER);

        // --- 3. 底部操作 ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("添加班级");
        JButton btnRefresh = new JButton("刷新列表");
        // --- 【新增】删除按钮 ---
        JButton btnDelete = new JButton("删除选中班级");
        btnDelete.setForeground(Color.RED);

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnDelete); // 加入
        add(bottomPanel, BorderLayout.SOUTH);

        // --- 4. 事件 ---
        btnRefresh.addActionListener(e -> loadData());

        btnAdd.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField countField = new JTextField();
            Object[] msg = {"班级名称:", nameField, "学生人数:", countField};

            if (JOptionPane.showConfirmDialog(null, msg, "新增班级", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText();
                    int count = Integer.parseInt(countField.getText());
                    if (metaDAO.addClass(name, count)) loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "输入格式错误");
                }
            }
        });
        // --- 【新增】删除事件监听 ---
        btnDelete.addActionListener(e -> {
            int row = classTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的班级！");
                return;
            }

            String className = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除 [" + className + "] 吗？\n如果该班级还有学生或预约记录，删除将失败。",
                    "确认删除", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tableModel.getValueAt(row, 0);
                // 调用 MetaDAO 的 deleteClass
                if (metaDAO.deleteClass(id)) {
                    JOptionPane.showMessageDialog(this, "删除成功！");
                    loadData(); // 刷新
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败！\n该班级下可能还有学生账号或预约记录。");
                }
            }
        });

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<ClassEntity> classes = metaDAO.getAllClasses();
        for (ClassEntity c : classes) {
            tableModel.addRow(new Object[]{c.getId(), c.getClassName(), c.getStudentCount()});
        }
    }
}
