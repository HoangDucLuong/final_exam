package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import shop.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserRepository {
    @Autowired
    JdbcTemplate db;

    // Phương thức thêm người dùng
    public void addUser(User user) {
        String sql = "INSERT INTO tbl_user (name, email, pwd, usr_type, phone, address, created_at, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        db.update(sql, user.getName(), user.getEmail(), user.getPwd(),
                user.getUsrType(), user.getPhone(), user.getAddress(), user.getCreatedAt(), user.getStatus());
    }

    // Phương thức lấy tên người dùng theo email
    public String findNameByUid(String email) {
        String sql = "SELECT name FROM tbl_user WHERE email = ?";
        return db.queryForObject(sql, new Object[]{email}, String.class);
    }

    // Phương thức lấy thông tin người dùng theo email (có thể cần thiết trong tương lai)
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM tbl_user WHERE email = ?";
        return db.queryForObject(sql, new Object[]{email}, new UserRowMapper());
    }

    // Phương thức cập nhật thông tin người dùng
    public void updateUser(User user) {
        String sql = "UPDATE tbl_user SET name = ?, phone = ?, address = ?, pwd = ? WHERE email = ?";

        db.update(sql, user.getName(), user.getPhone(), user.getAddress(), user.getPwd(), user.getEmail());
    }

    // Phương thức tìm kiếm người dùng theo từ khóa và phân trang
    public List<User> findAllUsers(int page, String search) {
        int pageSize = 10; // Số lượng người dùng mỗi trang
        int offset = (page - 1) * pageSize;

        // Tạo câu lệnh SQL với điều kiện tìm kiếm
        String sql = "SELECT * FROM tbl_user WHERE name LIKE ? OR email LIKE ? ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        // Sử dụng JdbcTemplate để thực hiện truy vấn
        return db.query(sql, new Object[]{"%" + search + "%", "%" + search + "%", offset, pageSize},
                new UserRowMapper());
    }

    // RowMapper để ánh xạ kết quả truy vấn thành đối tượng User
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPwd(rs.getString("pwd"));
            user.setUsrType(rs.getInt("usr_type"));
            user.setPhone(rs.getString("phone"));
            user.setAddress(rs.getString("address"));
            user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            user.setStatus(rs.getInt("status"));
            return user;
        }
    }
}