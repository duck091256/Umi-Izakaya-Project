package database;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class InitDB {
    public static void main(String[] args) {
        try {
            // Đọc nội dung file Export.sql
            String sql = new String(Files.readAllBytes(Paths.get("resources/Export.sql")));

            // Tách các câu lệnh SQL bằng dấu chấm phẩy, loại bỏ các dòng comment
            String[] queries = sql
                .replaceAll("(?m)^\\s*--.*$", "")          // xóa dòng comment bắt đầu bằng --
                .replaceAll("(?m)^\\s*/\\*.*?\\*/", "")     // xóa dòng /*...*/ nếu có
                .split("(?<=[;])\\s*\\n");                  // tách bằng dấu ;

            // Kết nối DB (chỉ tới server, không chọn sẵn DB)
            Connection conn = JDBCUtil.getConnectionNoDB(); // cần chỉnh sửa JDBCUtil để hỗ trợ
            Statement stmt = conn.createStatement();

            // Thực thi từng câu lệnh
            for (String query : queries) {
                if (query.trim().isEmpty()) continue;
                try {
                    stmt.execute(query);
                } catch (Exception ex) {
                    System.out.println("Lỗi khi chạy câu: " + query.trim());
                    ex.printStackTrace();
                }
            }

            stmt.close();
            conn.close();
            System.out.println("Khởi tạo DB thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}