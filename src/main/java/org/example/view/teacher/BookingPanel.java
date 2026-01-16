package org.example.view.teacher;

import org.example.dao.MetaDAO;
import org.example.model.*;
import org.example.service.BookingService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class BookingPanel extends JPanel {
    private JComboBox<ClassEntity> classCombo;
    private JComboBox<Course> courseCombo;
    private JTable slotTable;
    private DefaultTableModel tableModel;
    private BookingService bookingService = new BookingService();
    private MetaDAO metaDAO = new MetaDAO();

    public BookingPanel(int currentTeacherId) {
        setLayout(new BorderLayout());

        // 1. 顶部选择区
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        classCombo = new JComboBox<>();
        courseCombo = new JComboBox<>();

        // 加载班级和课程数据到下拉框
        loadCombos();

        topPanel.add(new JLabel("选择班级:"));
        topPanel.add(classCombo);
        topPanel.add(new JLabel("选择课程:"));
        topPanel.add(courseCombo);
        JButton btnSearch = new JButton("查询可用实验室");
        topPanel.add(btnSearch);

        // 2. 中间显示区 (显示符合条件的 slot)
        String[] cols = {"时段ID", "实验室", "日期", "时间段"};
        tableModel = new DefaultTableModel(cols, 0);
        slotTable = new JTable(tableModel);

        // 3. 底部提交区
        JButton btnSubmit = new JButton("提交预约申请");

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(slotTable), BorderLayout.CENTER);
        add(btnSubmit, BorderLayout.SOUTH);

        // --- 事件监听 ---
        // 点击查询：根据选中班级的人数，自动核查并筛选实验室
        btnSearch.addActionListener(e -> {
            ClassEntity selectedClass = (ClassEntity) classCombo.getSelectedItem();
            if (selectedClass != null) {
                refreshAvailableSlots(selectedClass.getId());
            }
        });

        // 点击提交：生成预约记录，初始状态为 PENDING
        btnSubmit.addActionListener(e -> {
            int row = slotTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请选择一个时间段！");
                return;
            }
            int slotId = (int) tableModel.getValueAt(row, 0);
            int classId = ((ClassEntity) classCombo.getSelectedItem()).getId();
            int courseId = ((Course) courseCombo.getSelectedItem()).getId();

            if (bookingService.applyReservation(slotId, currentTeacherId, classId, courseId)) {
                JOptionPane.showMessageDialog(this, "预约申请已提交，请等待管理员审核！");
                refreshAvailableSlots(classId); // 刷新列表，该时段将不再可见
            }
        });
    }

    private void loadCombos() {
        // 1. 清空现有选项，防止重复添加
        classCombo.removeAllItems();
        courseCombo.removeAllItems();

        // 2. 加载班级数据
        // metaDAO.getAllClasses() 返回 List<ClassEntity>
        List<ClassEntity> classes = metaDAO.getAllClasses();
        for (ClassEntity c : classes) {
            // 直接把对象加进去！因为重写了 toString()，界面会显示名字，但底层拿到的还是对象
            classCombo.addItem(c);
        }

        // 3. 加载课程数据
        // metaDAO.getAllCourses() 返回 List<Course>
        List<Course> courses = metaDAO.getAllCourses();
        for (Course c : courses) {
            courseCombo.addItem(c);
        }

        // 4. (可选) 如果列表为空，提示一下
        if (classCombo.getItemCount() == 0) {
            classCombo.addItem(new ClassEntity(0, "暂无班级数据", 0));
        }
    }

    private void refreshAvailableSlots(int classId) {
        tableModel.setRowCount(0);
        List<Map<String, Object>> slots = bookingService.getAvailableSlots(classId);
        for (Map<String, Object> s : slots) {
            tableModel.addRow(new Object[]{s.get("slot_id"), s.get("room_number"), s.get("date_slot"), s.get("period")});
        }
    }
}
