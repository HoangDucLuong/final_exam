package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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
    @Autowired
    private JdbcTemplate jdbcTemplate;
    // Phương thức tìm kiếm người dùng theo từ khóa và phân trang
    public List<User> findAllUsers(int page, String search) {
        int pageSize = 10; // Số lượng người dùng mỗi trang
        int offset = (page - 1) * pageSize;

        // Sử dụng OFFSET và FETCH NEXT để phân trang trên SQL Server
        String sql = "SELECT * FROM tbl_user WHERE name LIKE ? OR email LIKE ? ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        return db.query(sql, new Object[]{"%" + search + "%", "%" + search + "%", offset, pageSize},
                new UserRowMapper());
    }

    // Phương thức thêm người dùng
    public void addUser(User user) {
        String sql = "INSERT INTO tbl_user (name, email, pwd, usr_type, phone, address, created_at, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        db.update(sql, user.getName(), user.getEmail(), user.getPwd(),
                user.getUsrType(), user.getPhone(), user.getAddress(), user.getCreatedAt(), user.getStatus());
    }

    // Phương thức tìm kiếm người dùng theo ID
 // Phương thức tìm kiếm người dùng theo ID
    public User findById(int id) {
        String sql = "SELECT * FROM tbl_user WHERE id = ?";
        try {
            return db.queryForObject(sql, new Object[]{id}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null; // Trả về null nếu không tìm thấy người dùng
        }
    }


    // Phương thức lấy tên người dùng theo email
    public String findNameByUid(String email) {
        String sql = "SELECT name FROM tbl_user WHERE email = ?";
        return db.queryForObject(sql, new Object[]{email}, String.class);
    }

    // Phương thức lấy thông tin người dùng theo email
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM tbl_user WHERE email = ?";
        return db.queryForObject(sql, new Object[]{email}, new UserRowMapper());
    }

    // Phương thức cập nhật thông tin người dùng
    public void updateUser(User user) {
        String sql = "UPDATE tbl_user SET name = ?, email = ?, phone = ?, address = ?, pwd = ?, status = ? WHERE id = ?";

        db.update(sql, user.getName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getPwd(), user.getStatus(), user.getId());
    }

    public void deleteUser(int id) {
        String sql = "DELETE FROM tbl_user WHERE id = ?";
        db.update(sql, id);
    }

    // Phương thức lấy toàn bộ danh sách người dùng
    public List<User> findAll() {
        String sql = "SELECT * FROM tbl_user";
        return db.query(sql, new UserRowMapper());
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
    
    public List<User> findUsersWithExpiringContracts() {
        String sql = "SELECT u.* FROM tbl_user u " +
                     "JOIN tbl_contract c ON u.id = c.usr_id " +
                     "WHERE DATEDIFF(day, GETDATE(), c.end_date) = 10";
        
        return db.query(sql, new UserRowMapper());
    }
    public Integer findUserIdByEmail(String email) {
        String sql = "SELECT id FROM tbl_user WHERE email = ?";
        try {
            return db.queryForObject(sql, new Object[]{email}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // Trả về null nếu không tìm thấy
        }
    }

    
 // Phương thức lấy email của người dùng dựa trên contract_id (danh sách)
    public String findEmailByContractId(int contractId) {
        String sql = "SELECT TOP 1 u.email " +
                     "FROM tbl_invoice i " +
                     "JOIN tbl_contract c ON i.contract_id = c.id " +
                     "JOIN tbl_user u ON c.usr_id = u.id " +
                     "WHERE i.contract_id = ? " +  // Loại bỏ điều kiện payment_status
                     "ORDER BY i.id"; // Thêm sắp xếp để đảm bảo kết quả nhất quán

        try {
            return db.queryForObject(sql, new Object[]{contractId}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            // Ghi log lỗi hoặc xử lý nhiều kết quả
            System.err.println("Tìm thấy nhiều email cho mã hợp đồng: " + contractId);
            
            // Hoặc lấy email đầu tiên
            List<String> emails = db.queryForList(
                "SELECT TOP 1 u.email " +
                "FROM tbl_invoice i " +
                "JOIN tbl_contract c ON i.contract_id = c.id " +
                "JOIN tbl_user u ON c.usr_id = u.id " +
                "WHERE i.contract_id = ? " +
                "ORDER BY i.id", 
                new Object[]{contractId}, 
                String.class
            );
            
            return emails.isEmpty() ? null : emails.get(0);
        }
    }

}