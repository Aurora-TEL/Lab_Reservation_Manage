package org.example.view.admin;

import org.example.dao.MetaDAO;
import org.example.dao.UserDAO;
import org.example.model.ClassEntity;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class UserManagePanel extends JPanel {
    private UserDAO userDAO = new UserDAO();
    private MetaDAO metaDAO = new MetaDAO();

    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 中间表格 ---
        String[] columnNames = {"ID", "用户名", "角色身份", "所属班级"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // --- 底部按钮区 ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAddTeacher = new JButton("注册教师");
        JButton btnAddStudent = new JButton("注册学生");
        JButton btnEdit = new JButton("修改信息"); // 【新增】
        JButton btnResetPass = new JButton("重置密码"); // 【新增】
        JButton btnDelete = new JButton("删除用户");
        JButton btnRefresh = new JButton("刷新");

        // 设置颜色区分功能
        btnEdit.setForeground(Color.BLUE);
        btnResetPass.setForeground(new Color(255, 140, 0)); // 橙色
        btnDelete.setForeground(Color.RED);

        bottomPanel.add(btnAddTeacher);
        bottomPanel.add(btnAddStudent);
        bottomPanel.add(btnEdit);       // 加入修改
        bottomPanel.add(btnResetPass);  // 加入重置密码
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- 事件监听 ---
        btnRefresh.addActionListener(e -> refreshTable());
        btnAddTeacher.addActionListener(e -> showAddTeacherDialog());
        btnAddStudent.addActionListener(e -> showAddStudentDialog());

        // 删除事件
        btnDelete.addActionListener(e -> deleteUserAction());

        // 【新增】修改信息事件
        btnEdit.addActionListener(e -> showEditUserDialog());

        // 【新增】重置密码事件
        btnResetPass.addActionListener(e -> showResetPasswordDialog());

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Map<String, Object>> users = userDAO.getAllUsersForDisplay();
        for (Map<String, Object> u : users) {
            tableModel.addRow(new Object[]{
                    u.get("id"),
                    u.get("username"),
                    u.get("role"),
                    u.get("class_name")
            });
        }
    }

    // --- 1. 修改用户信息弹窗逻辑 ---
    private void showEditUserDialog() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的用户！");
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String currentName = (String) tableModel.getValueAt(row, 1);
        String roleStr = (String) tableModel.getValueAt(row, 2); // "教师" 或 "学生"
        boolean isStudent = "学生".equals(roleStr);

        // 构建输入面板
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField nameField = new JTextField(currentName);
        JComboBox<ClassEntity> classCombo = new JComboBox<>();

        panel.add(new JLabel("用户名:"));
        panel.add(nameField);

        // 只有学生才显示班级选择
        if (isStudent) {
            panel.add(new JLabel("所属班级:"));
            List<ClassEntity> classes = metaDAO.getAllClasses();
            for (ClassEntity c : classes) {
                classCombo.addItem(c);
                // 简单的回显逻辑：如果名字匹配则选中（这里做简化处理，默认选中第一个或不做回显）
            }
            panel.add(classCombo);
        } else {
            panel.add(new JLabel("角色:"));
            panel.add(new JLabel(roleStr + " (无需班级)"));
        }

        int option = JOptionPane.showConfirmDialog(null, panel, "修改用户信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名不能为空！");
                return;
            }

            Integer newClassId = null;
            if (isStudent) {
                ClassEntity selectedClass = (ClassEntity) classCombo.getSelectedItem();
                if (selectedClass != null) newClassId = selectedClass.getId();
            }

            if (userDAO.updateUser(userId, newName, newClassId)) {
                JOptionPane.showMessageDialog(this, "修改成功！");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "修改失败，请重试。");
            }
        }
    }

    // --- 2. 重置密码弹窗逻辑 ---
    private void showResetPasswordDialog() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择用户！");
            return;
        }
        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        String newPass = JOptionPane.showInputDialog(this, "请输入 [" + username + "] 的新密码:", "123456");

        if (newPass != null && !newPass.trim().isEmpty()) {
            if (userDAO.updatePassword(userId, newPass.trim())) {
                JOptionPane.showMessageDialog(this, "密码重置成功！");
            } else {
                JOptionPane.showMessageDialog(this, "操作失败。");
            }
        }
    }

    // --- 之前的辅助方法 (保持不变) ---
    private void deleteUserAction() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的用户！");
            return;
        }
        String username = (String) tableModel.getValueAt(row, 1);
        if ("admin".equalsIgnoreCase(username)) {
            JOptionPane.showMessageDialog(this, "管理员账号不可删除！");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除用户 [" + username + "] 吗？", "警告", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser((int) tableModel.getValueAt(row, 0))) {
                JOptionPane.showMessageDialog(this, "删除成功");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，该用户可能有预约记录。");
            }
        }
    }

    private void showAddTeacherDialog() {
        // ... (保持之前的代码)
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] message = {"教师用户名:", nameField, "初始密码:", passField};
        if (JOptionPane.showConfirmDialog(null, message, "新增教师", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (userDAO.addUser(nameField.getText(), new String(passField.getPassword()), "TEACHER")) {
                refreshTable();
            }
        }
    }

    private void showAddStudentDialog() {
        // ... (保持之前的代码)
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<ClassEntity> combo = new JComboBox<>();
        for(ClassEntity c : metaDAO.getAllClasses()) combo.addItem(c);
        Object[] msg = {"学生用户名:", nameField, "密码:", passField, "班级:", combo};
        if (JOptionPane.showConfirmDialog(null, msg, "新增学生", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ClassEntity c = (ClassEntity) combo.getSelectedItem();
            if(c!=null && userDAO.addStudent(nameField.getText(), new String(passField.getPassword()), c.getId())) {
                refreshTable();
            }
        }
    }
}