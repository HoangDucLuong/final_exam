package shop.controller;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.User;
import shop.repository.AuthRepository;
import shop.repository.UserRepository; 
import shop.utils.SecurityUtility;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    AuthRepository repAuth;
    @Autowired
    UserRepository repUser;

    // Trang đăng nhập
    @GetMapping("/login")
    public String login() {
        return "user/login"; // Trả về trang đăng nhập người dùng
    }

    // Kiểm tra đăng nhập
	    @PostMapping("/chklogins")
	    public String chklogins(@RequestParam("email") String email, @RequestParam("pwd") String password,
	                            HttpServletRequest request) {
	        Logger log = Logger.getGlobal();
	        log.info("Attempted login by user: " + email);
	
	        // Lấy mật khẩu đã mã hóa và quyền user từ repository
	        String encryptedPassword = repAuth.findPasswordByUid(email);
	        Integer role = repAuth.findUserTypeByUid(email);
	        String fullName = repUser.findNameByUid(email); // Lấy tên người dùng dựa trên email
	
	        // Kiểm tra mật khẩu
	        if (encryptedPassword != null && SecurityUtility.compareBcrypt(encryptedPassword, password)) {
	            request.getSession().setAttribute("user", email); // Lưu email vào phiên
	            request.getSession().setAttribute("name", fullName); // Lưu tên người dùng
	            request.getSession().setAttribute("usr_type", role); // Lưu quyền của người dùng
	
	            return "redirect:/"; // Điều hướng đến trang chính của người dùng
	        } else {
	            request.setAttribute("error", "Invalid email or password");
	            return "user/login"; // Trả về trang đăng nhập nếu thông tin sai
	        }
	    }

    // Trang đăng ký
    @GetMapping("/register")
    public String register() {
        return "user/register"; // Trả về trang đăng ký người dùng
    }

    // Xử lý đăng ký
    @PostMapping("/add")
    public String addUser(@RequestParam String email, 
                          @RequestParam String pwd, 
                          @RequestParam String name,
                          @RequestParam String phone,
                          @RequestParam String address) {
        // Tạo đối tượng User mới
        User user = new User();
        user.setEmail(email); // Đặt email là username

        // Hash mật khẩu
        String hashedPassword = SecurityUtility.encryptBcrypt(pwd);
        user.setPwd(hashedPassword); // Lưu mật khẩu đã hash

        user.setName(name); // Lưu tên đầy đủ
        user.setCreatedAt(LocalDateTime.now()); // Thời gian tạo tài khoản
        user.setPhone(phone); // Lưu số điện thoại
        user.setAddress(address); // Lưu địa chỉ
        user.setStatus(1); // Đặt trạng thái mặc định là active (1)
        user.setUsrType(0); // Đặt quyền mặc định là user (0)

        // Thêm người dùng vào cơ sở dữ liệu
        repUser.addUser(user);

        // Điều hướng lại trang người dùng sau khi thêm thành công
        return "redirect:/user/login"; // Có thể chuyển hướng đến trang đăng nhập
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate(); // Xóa thông tin phiên
        return "redirect:/user/login"; // Chuyển hướng đến trang đăng nhập
    }

}
