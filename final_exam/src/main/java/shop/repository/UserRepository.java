package shop.repository;

import shop.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Kiểm tra xem email đã tồn tại chưa
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM tbl_user WHERE _email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{email}, Integer.class);
        return count != null && count > 0;
    }

    // Tìm user theo ID
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM tbl_user WHERE _id = ?";
        try {
            User user = jdbcTemplate.queryForObject(
                sql,
                new Object[]{id},
                new BeanPropertyRowMapper<>(User.class)
            );
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Tìm user theo uid
    public Optional<User> findByUid(String uid) {
        System.out.println("Finding user with uid: " + uid); // In ra uid
        String sql = "SELECT * FROM tbl_user WHERE _uid = ?";
        try {
            User user = jdbcTemplate.queryForObject(
                sql,
                new Object[]{uid},
                new BeanPropertyRowMapper<>(User.class)
            );
            return Optional.of(user);
        } catch (Exception e) {
            System.err.println("User not found or error occurred: " + e.getMessage());
            return Optional.empty();
        }
    }


    // Tìm user theo loại người dùng (userType)
    public List<User> findByUserType(int userType) {
        String sql = "SELECT * FROM tbl_user WHERE _usr_type = ?";
        return jdbcTemplate.query(
            sql,
            new Object[]{userType},
            new BeanPropertyRowMapper<>(User.class)
        );
    }

    // Tìm user theo tên đăng nhập (username)
    public Optional<User> findByUserName(String userName) {
        String sql = "SELECT * FROM tbl_user WHERE _name = ?";
        try {
            User user = jdbcTemplate.queryForObject(
                sql,
                new Object[]{userName},
                new BeanPropertyRowMapper<>(User.class)
            );
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Tìm user theo email
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM tbl_user WHERE _email = ?";
        try {
            User user = jdbcTemplate.queryForObject(
                sql,
                new Object[]{email},
                new BeanPropertyRowMapper<>(User.class)
            );
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Lưu user mới
    public int saveUser(User user) {
        String sql = "INSERT INTO tbl_user (_name, _uid, _email, _pwd, _usr_type, _phone, _address, _created_at, _status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(
            sql,
            user.getName(),
            user.getUid(),
            user.getEmail(),
            user.getPassword(),
            user.getUserType(),
            user.getPhone(),
            user.getAddress(),
            user.getCreatedAt(),
            user.getStatus()
        );
    }

    // Cập nhật thông tin user
    public int updateUser(User user) {
        String sql = "UPDATE tbl_user SET _name=?, _uid=?, _email=?, _pwd=?, _usr_type=?, _phone=?, _address=?, _created_at=?, _status=? WHERE _id=?";
        return jdbcTemplate.update(
            sql,
            user.getName(),
            user.getUid(),
            user.getEmail(),
            user.getPassword(),
            user.getUserType(),
            user.getPhone(),
            user.getAddress(),
            user.getCreatedAt(),
            user.getStatus(),
            user.getId()
        );
    }

    // Xóa user theo ID
    public int deleteById(int id) {
        String sql = "DELETE FROM tbl_user WHERE _id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
