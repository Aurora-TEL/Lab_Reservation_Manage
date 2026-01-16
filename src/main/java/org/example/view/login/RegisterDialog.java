package org.example.view.login;

import org.example.dao.MetaDAO;
import org.example.dao.UserDAO;
import org.example.model.ClassEntity;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RegisterDialog extends JDialog {
    private JTextField userText;
    private JPasswordField passText;
    private JPasswordField confirmText;
    private JRadioButton rbStudent, rbTeacher;
    private JComboBox<ClassEntity> classCombo;
    private JPanel classPanel; // 用来包裹班级选择框，方便隐藏

    private UserDAO userDAO = new UserDAO();
    private MetaDAO metaDAO = new MetaDAO();

    public RegisterDialog(Frame owner) {
        super(owner, "新用户注册", true); // true 表示模态窗口(必须处理完才能点别的)
        setSize(400, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // --- 中间输入区 (GridBagLayout 布局更灵活) ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 间距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        userText = new JTextField(15);
        centerPanel.add(userText, gbc);

        // 2. 密码
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("密  码:"), gbc);
        gbc.gridx = 1;
        passText = new JPasswordField(15);
        centerPanel.add(passText, gbc);

        // 3. 确认密码
        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(new JLabel("确认密码:"), gbc);
        gbc.gridx = 1;
        confirmText = new JPasswordField(15);
        centerPanel.add(confirmText, gbc);

        // 4. 角色选择
        gbc.gridx = 0; gbc.gridy = 3;
        centerPanel.add(new JLabel("注册角色:"), gbc);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ButtonGroup bg = new ButtonGroup();
        rbStudent = new JRadioButton("学生", true); // 默认选学生
        rbTeacher = new JRadioButton("教师");
        bg.add(rbStudent);
        bg.add(rbTeacher);
        rolePanel.add(rbStudent);
        rolePanel.add(rbTeacher);

        gbc.gridx = 1;
        centerPanel.add(rolePanel, gbc);

        // 5. 班级选择 (默认显示，因为默认是学生)
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2; // 占两列

        classPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        classPanel.add(new JLabel("所属班级: "));
        classCombo = new JComboBox<>();
        loadClasses(); // 加载数据库班级
        classPanel.add(classCombo);

        centerPanel.add(classPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // --- 底部按钮区 ---
        JPanel btnPanel = new JPanel();
        JButton btnRegister = new JButton("确认注册");
        JButton btnCancel = new JButton("取消");
        btnPanel.add(btnRegister);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        // --- 事件监听 ---

        // 1. 角色切换事件：选老师时隐藏班级，选学生时显示
        rbTeacher.addActionListener(e -> classPanel.setVisible(false));
        rbStudent.addActionListener(e -> classPanel.setVisible(true));

        // 2. 注册按钮
        btnRegister.addActionListener(e -> doRegister());

        // 3. 取消按钮
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadClasses() {
        List<ClassEntity> list = metaDAO.getAllClasses();
        for (ClassEntity c : list) {
            classCombo.addItem(c);
        }
    }

    private void doRegister() {
        String username = userText.getText().trim();
        String pass = new String(passText.getPassword());
        String confirm = new String(confirmText.getPassword());

        // 基础校验
        if (username.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！");
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致！");
            return;
        }

        // 查重
        if (userDAO.isUsernameExists(username)) {
            JOptionPane.showMessageDialog(this, "该用户名已被注册，请换一个！");
            return;
        }

        // 准备数据
        String role = rbTeacher.isSelected() ? "TEACHER" : "STUDENT";
        Integer classId = null;

        if ("STUDENT".equals(role)) {
            ClassEntity selectedClass = (ClassEntity) classCombo.getSelectedItem();
            if (selectedClass == null) {
                JOptionPane.showMessageDialog(this, "学生必须选择一个班级！");
                return;
            }
            classId = selectedClass.getId();
        }

        // 写入数据库
        if (userDAO.register(username, pass, role, classId)) {
            JOptionPane.showMessageDialog(this, "注册成功！请使用新账号登录。");
            dispose(); // 关闭窗口
        } else {
            JOptionPane.showMessageDialog(this, "注册失败，系统错误。");
        }
    }
}