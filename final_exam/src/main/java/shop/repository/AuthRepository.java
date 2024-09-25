package shop.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    private final JdbcTemplate db;

    public AuthRepository(JdbcTemplate db) {
        this.db = db;
    }

    // Tìm mật khẩu bằng UID (ở đây dùng email để làm ví dụ nếu UID là email)
    public String findPasswordByUid(String uid) {
        String sql = "SELECT pwd FROM tbl_user WHERE email = ?";
        try {
            return db.queryForObject(sql, new Object[]{uid}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // UID không tồn tại
        }
    }

    // Tìm userType bằng UID (email)
    public Integer findUserTypeByUid(String uid) {
        String sql = "SELECT usr_type FROM tbl_user WHERE email = ?";
        try {
            return db.queryForObject(sql, new Object[]{uid}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // UID không tồn tại
        }
    }
}
