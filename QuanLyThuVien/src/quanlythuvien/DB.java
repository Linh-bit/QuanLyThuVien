package quanlythuvien;

import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DB {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LeMaiLinh_QuanLyThuVien;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "151107";

    private static Connection conn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void setParams(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof String) {
                p = ((String) p).trim();
                if (((String) p).isEmpty()) {
                    p = null;
                }
            }
            if (p == null) {
                ps.setNull(i + 1, Types.VARCHAR);
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }

    public static void load(JTable table, String sql, Object... params) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 0; i < cols; i++) {
                        Object v = rs.getObject(i + 1);
                        row[i] = (v instanceof String) ? ((String) v).trim() : v;
                    }
                    model.addRow(row);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static Object[] row(String sql, Object... params) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Object[] row = new Object[rs.getMetaData().getColumnCount()];
                for (int i = 0; i < row.length; i++) {
                    Object v = rs.getObject(i + 1);
                    row[i] = (v instanceof String) ? ((String) v).trim() : v;
                }
                return row;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static boolean run(String sql, String thanhCong, Object... params) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, params);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, thanhCong, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            JOptionPane.showMessageDialog(null, "Không có dòng nào được thay đổi!", "Thất bại", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            String msg;
            if (ex.getErrorCode() == 2627 || ex.getErrorCode() == 2601) {
                msg = "Mã đã tồn tại (trùng khóa chính)!";
            } else if (ex.getErrorCode() == 547) {
                msg = "Vi phạm ràng buộc: dữ liệu đang được bảng khác sử dụng hoặc giá trị không hợp lệ!";
            } else {
                msg = ex.getMessage();
            }
            JOptionPane.showMessageDialog(null, msg, "Thất bại", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
