package shop.controller;

import shop.model.User;
import shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // Hiển thị trang đăng ký
    @GetMapping("/register")
    public String registerForm() {
        return "user/register"; // Đường dẫn đến trang đăng ký
    }

    // Xử lý đăng ký người dùng
    @PostMapping("/register")
    public String registerUser(@RequestParam("name") String name,
    						   @RequestParam("uid") String uid,
                               @RequestParam("pwd") String password,
                               @RequestParam("email") String email,
                               @RequestParam("phone") String phone,
                               @RequestParam("address") String address) {

        // Tạo người dùng mới
        User newUser = new User();
        newUser.setName(name);
        newUser.setUid(uid);
        newUser.setPassword(password); // Mật khẩu sẽ được mã hóa trong service
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setUserType(0); // Mặc định là user
        newUser.setStatus(1); // Kích hoạt ngay lập tức

        // Lưu người dùng
        userService.saveUser(newUser);

        // Chuyển hướng đến trang đăng nhập
        return "redirect:/user/login?registered=true";
    }

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String loginForm() {
        return "user/login"; // Đường dẫn đến trang đăng nhập
    }

    // Xử lý đăng nhập
    @PostMapping("/login")
    public String login(@RequestParam("uid") String uid,
                        @RequestParam("pwd") String password,
                        HttpServletRequest request) {
        User user = userService.findByUid(uid); // Tìm user theo uid

        if (user == null) {
            return "redirect:/user/login?error=true"; // Quay lại trang đăng nhập nếu không tìm thấy user
        }

        String encodedPassword = user.getPassword();
        if (encodedPassword == null) {
            return "redirect:/user/login?error=true"; // Quay lại nếu mật khẩu không được lưu
        }

        if (userService.checkPassword(password, encodedPassword)) {
            // Lưu thông tin user vào session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);	
            return "redirect:/user/dashboard"; // Chuyển hướng đến trang dashboard
        }

        return "redirect:/user/login?error=true"; // Quay lại trang đăng nhập nếu thất bại
    }


}
