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
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AuthRepository repAuth;
    @Autowired
    UserRepository repUser; 

    // Trang chính của admin
    @GetMapping("/index")
    public String index() {
        return "admin/index"; // Trả về trang admin index
    }

    // Trang đăng nhập
    @GetMapping("/login")
    public String login() {
        return "admin/login"; // Trả về trang đăng nhập admin
    }

    // Kiểm tra đăng nhập
 // Kiểm tra đăng nhập
    @PostMapping("/chklogins")
    public String chklogins(@RequestParam("email") String email, @RequestParam("pwd") String password,
                            HttpServletRequest request) {
        Logger log = Logger.getGlobal();
        log.info("Attempted login by user: " + email);

        // Lấy mật khẩu đã mã hóa và quyền user từ repository
        String encryptedPassword = repAuth.findPasswordByUid(email);
        Integer role = repAuth.findUserTypeByUid(email);

        // Kiểm tra mật khẩu
        if (encryptedPassword == null) {
            // Nếu email không tồn tại
            request.setAttribute("error", "Invalid email or password");
            return "admin/login"; // Trả về trang đăng nhập với thông báo lỗi
        }

        if (!SecurityUtility.compareBcrypt(encryptedPassword, password)) {
            // Nếu mật khẩu không đúng
            request.setAttribute("error", "Invalid email or password");
            return "admin/login"; // Trả về trang đăng nhập với thông báo lỗi
        }

        // Kiểm tra quyền người dùng
        if (role == 1) { // Nếu là admin
            request.getSession().setAttribute("usr_type", role);
            return "redirect:/admin/index"; // Điều hướng đến trang chính của admin
        } else {
            request.setAttribute("error", "You do not have permission to access the admin area.");
            return "admin/login"; // Trả về trang đăng nhập với thông báo lỗi
        }
    }



    // Trang đăng ký
    @GetMapping("/register")
    public String register() {
        return "admin/register"; // Trả về trang đăng ký admin
    }

    // Xử lý đăng ký
    @PostMapping("/add")
    public String addUser(@RequestParam String email, 
                          @RequestParam String pwd, 
                          @RequestParam String name,
                          @RequestParam String phone,
                          @RequestParam String address) {
        // Kiểm tra giá trị address
        if (address == null || address.isEmpty()) {
            // Có thể ném ra một thông báo lỗi hoặc xử lý khác nếu cần
            return "redirect:/admin/addUser?error=Address is required";
        }

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

        // Điều hướng lại trang admin sau khi thêm user thành công
        return "redirect:/admin/index";
    }

}
