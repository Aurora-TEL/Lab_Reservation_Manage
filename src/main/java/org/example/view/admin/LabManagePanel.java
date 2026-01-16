package org.example.view.admin;

import org.example.dao.LabDAO;
import org.example.dao.LabSlotDAO;
import org.example.model.Lab;
import org.example.model.LabSlot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LabManagePanel extends JPanel {
    // UI 组件
    private JTextField txtRoomSearch, txtCapSearch;
    private JTable labTable;
    private DefaultTableModel tableModel;

    // 数据访问对象
    private LabDAO labDAO = new LabDAO();
    private LabSlotDAO slotDAO = new LabSlotDAO();

    public LabManagePanel() {
        setLayout(new BorderLayout());

        // ==========================================
        // 1. 顶部查询区域
        // ==========================================
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder(" 实验室筛选查询 "));

        txtRoomSearch = new JTextField(10);
        txtCapSearch = new JTextField(5);
        JButton btnSearch = new JButton("查询");

        searchPanel.add(new JLabel("房间号:"));
        searchPanel.add(txtRoomSearch);
        searchPanel.add(new JLabel("最小容量:"));
        searchPanel.add(txtCapSearch);
        searchPanel.add(btnSearch);

        add(searchPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. 中间表格区域 (展示实验室列表)
        // ==========================================
        String[] columnNames = {"ID", "实验室房号", "可容纳人数"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // 禁止编辑
        };
        labTable = new JTable(tableModel);
        labTable.setRowHeight(25);
        add(new JScrollPane(labTable), BorderLayout.CENTER);

        // ==========================================
        // 3. 底部操作按钮区域
        // ==========================================
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd = new JButton("新增实验室(含时段)");
        btnAdd.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnAdd.setForeground(new Color(0, 102, 0)); // 墨绿色

        JButton btnViewSlots = new JButton("管理已发布时段");
        btnViewSlots.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnViewSlots.setForeground(Color.BLUE);

        // --- 【新增】删除按钮 ---
        JButton btnDelete = new JButton("删除选中实验室");
        btnDelete.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnDelete.setForeground(Color.RED);

        // 添加到面板
        actionPanel.add(btnAdd);
        actionPanel.add(btnViewSlots);
        actionPanel.add(btnDelete); // 加入

        add(actionPanel, BorderLayout.SOUTH);

        // ==========================================
        // 4. 事件监听器绑定
        // ==========================================

        // 查询按钮
        btnSearch.addActionListener(e -> refreshTable());

        // 新增按钮 (合并了添加实验室+发布初始时段)
        btnAdd.addActionListener(e -> showAddLabWithSlotDialog());

        // 查看/管理时段按钮
        btnViewSlots.addActionListener(e -> {
            int row = labTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先在表格中选中一个实验室！");
                return;
            }
            // 获取选中行的实验室 ID 和 房号
            int labId = (int) tableModel.getValueAt(row, 0);
            String roomNum = (String) tableModel.getValueAt(row, 1);

            showSlotListDialog(labId, roomNum);
        });
        // --- 【新增】删除事件监听 ---
        btnDelete.addActionListener(e -> {
            int row = labTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的实验室！");
                return;
            }

            String roomNum = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除实验室 [" + roomNum + "] 吗？\n注意：如果该实验室已发布时间段，必须先清空时间段才能删除！",
                    "确认删除", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tableModel.getValueAt(row, 0);
                if (labDAO.deleteLab(id)) {
                    JOptionPane.showMessageDialog(this, "删除成功！");
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败！\n该实验室可能包含已发布的时间段或已有预约记录。\n请先去【管理已发布时段】清空数据。");
                }
            }
        });

        // 初始化加载数据
        refreshTable();
    }

    /**
     * 刷新实验室列表表格
     */
    private void refreshTable() {
        tableModel.setRowCount(0); // 清空旧数据
        // 获取查询条件
        String room = txtRoomSearch.getText();
        String capStr = txtCapSearch.getText();

        // 调用 DAO 查询
        List<Lab> labs = labDAO.searchLabs(room, capStr);

        // 填充表格
        for (Lab lab : labs) {
            tableModel.addRow(new Object[]{
                    lab.getId(),
                    lab.getRoomNumber(),
                    lab.getCapacity()
            });
        }
    }

    /**
     * 弹出对话框：新增实验室并直接发布一个初始时间段
     */
    private void showAddLabWithSlotDialog() {
        // 构建输入面板
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10)); // 4行2列，间距10

        JTextField txtRoom = new JTextField();
        JTextField txtCap = new JTextField();
        JTextField txtDate = new JTextField("2026-06-01"); // 给个默认值方便测试
        String[] periods = {"上午", "下午", "1-2节", "3-4节", "5-6节", "7-8节", "晚上"};
        JComboBox<String> comboPeriod = new JComboBox<>(periods);

        panel.add(new JLabel("实验室房号:"));
        panel.add(txtRoom);

        panel.add(new JLabel("容纳人数:"));
        panel.add(txtCap);

        panel.add(new JLabel("初始开放日期 (YYYY-MM-DD):"));
        panel.add(txtDate);

        panel.add(new JLabel("初始开放时段:"));
        panel.add(comboPeriod);

        int option = JOptionPane.showConfirmDialog(null, panel, "新增实验室并发布时段", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            // 基础校验
            if (txtRoom.getText().trim().isEmpty() || txtCap.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "房号和容量不能为空！");
                return;
            }

            try {
                // 1. 准备 Lab 对象
                Lab lab = new Lab();
                lab.setRoomNumber(txtRoom.getText().trim());
                lab.setCapacity(Integer.parseInt(txtCap.getText().trim()));

                // 2. 插入实验室并获取新 ID (需要 LabDAO 支持 addLabReturnId)
                int newLabId = labDAO.addLabReturnId(lab);

                if (newLabId != -1) {
                    // 3. 插入初始时间段
                    boolean slotSuccess = slotDAO.addSlot(
                            newLabId,
                            txtDate.getText().trim(),
                            (String) comboPeriod.getSelectedItem()
                    );

                    StringBuilder msg = new StringBuilder("实验室添加成功！\n");
                    if (slotSuccess) {
                        msg.append("初始时段已成功发布。");
                    } else {
                        msg.append("但初始时段发布失败（可能是日期格式错误），请稍后手动发布。");
                    }
                    JOptionPane.showMessageDialog(this, msg.toString());

                    // 刷新表格显示新数据
                    refreshTable();

                } else {
                    JOptionPane.showMessageDialog(this, "添加实验室失败，请检查数据库连接。");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "容量必须是有效的整数！");
            }
        }
    }

    /**
     * 弹出对话框：查看并管理某个实验室的所有时间段
     */
    private void showSlotListDialog(int labId, String roomNum) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "实验室时段详情: " + roomNum, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // 1. 时段表格模型
        String[] columns = {"时段ID", "日期", "时间段", "当前状态"};
        DefaultTableModel slotModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable slotTable = new JTable(slotModel);

        // 2. 加载该实验室的时段数据
        List<LabSlot> slots = slotDAO.getSlotsByLab(labId);
        for (LabSlot s : slots) {
            slotModel.addRow(new Object[]{
                    s.getId(),
                    s.getDateSlot(),
                    s.getPeriod(),
                    s.isAvailable() ? "可用" : "已被占用/禁用"
            });
        }

        dialog.add(new JScrollPane(slotTable), BorderLayout.CENTER);

        // 3. 底部操作栏
        JPanel btnPanel = new JPanel();
        JButton btnDeleteSlot = new JButton("撤销选中时段");
        JButton btnAddMore = new JButton("在该实验室发布新时段");

        btnDeleteSlot.setForeground(Color.RED);

        // --- 撤销时段逻辑 ---
        btnDeleteSlot.addActionListener(e -> {
            int row = slotTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "请先选择一个时段！");
                return;
            }
            int slotId = (int) slotModel.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "确定要删除该时段吗？\n如果该时段已有预约记录，数据库将阻止删除。",
                    "警告", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (slotDAO.deleteSlot(slotId)) {
                    JOptionPane.showMessageDialog(dialog, "删除成功！");
                    slotModel.removeRow(row); // 界面移除
                } else {
                    JOptionPane.showMessageDialog(dialog, "删除失败！\n该时段可能已被预约，请先处理预约记录。");
                }
            }
        });

        // --- 继续发布新时段逻辑 ---
        btnAddMore.addActionListener(e -> {
            // 简单弹窗输入日期和时段
            JTextField dateField = new JTextField("2026-06-02");
            String[] periods = {"上午", "下午", "1-2节", "3-4节", "5-6节", "晚上"};
            JComboBox<String> pBox = new JComboBox<>(periods);
            Object[] msg = {"日期:", dateField, "时段:", pBox};

            if (JOptionPane.showConfirmDialog(dialog, msg, "追加发布", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (slotDAO.addSlot(labId, dateField.getText(), (String) pBox.getSelectedItem())) {
                    JOptionPane.showMessageDialog(dialog, "发布成功！");
                    // 刷新当前弹窗里的表格
                    slotModel.setRowCount(0);
                    List<LabSlot> newSlots = slotDAO.getSlotsByLab(labId);
                    for (LabSlot s : newSlots) {
                        slotModel.addRow(new Object[]{ s.getId(), s.getDateSlot(), s.getPeriod(), s.isAvailable() ? "可用" : "不可用" });
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "发布失败，请检查日期格式。");
                }
            }
        });

        btnPanel.add(btnAddMore);
        btnPanel.add(btnDeleteSlot);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}