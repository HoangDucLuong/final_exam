package shop.service;

import shop.model.User;
import shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Tìm user theo ID
    public User findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    // Tìm user theo email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Tìm user theo uid
    public User findByUid(String uid) {
        return userRepository.findByUid(uid).orElse(null);
    }

    // Tìm user theo loại người dùng (userType)
    public List<User> findByUserType(int userType) {
        return userRepository.findByUserType(userType);
    }

    // Kiểm tra mật khẩu (so sánh mật khẩu đã mã hóa)
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            throw new IllegalArgumentException("Encoded password cannot be null");
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }



    // Lưu user mới
    public User saveUser(User user) {
        // Mã hóa mật khẩu trước khi lưu
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        // Set thời gian tạo nếu chưa có
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        // Set trạng thái tài khoản thành 1 (active) ngay lập tức
        user.setStatus(1);

        // Lưu user vào cơ sở dữ liệu
        userRepository.saveUser(user);

        return user;
    }


    // Cập nhật thông tin user
    public int updateUser(User user) {
        return jdbcTemplate.update(
            "UPDATE Users SET name=?, password=?, user_type=?, email=?, phone=?, address=?, created_at=?, status=? WHERE id=?",
            user.getName(),
            user.getPassword(),
            user.getUserType(),
            user.getEmail(),
            user.getPhone(),
            user.getAddress(),
            user.getCreatedAt(),
            user.getStatus(),
            user.getId()
        );
    }

    // Xóa user theo ID
    public boolean deleteById(int id) {
        return userRepository.deleteById(id) > 0;
    }
} 