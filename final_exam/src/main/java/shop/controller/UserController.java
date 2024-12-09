package shop.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.User;
import shop.repository.AuthRepository;
import shop.repository.UserRepository;
import shop.service.MailService;
import shop.utils.SecurityUtility;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    AuthRepository repAuth;
    @Autowired
    UserRepository repUser;
    @Autowired
    private MailService mailService;

    // Trang đăng nhập
    @GetMapping("/login")
    public String login() {
        return "user/login"; // Trả về trang đăng nhập người dùng
    }

    @PostMapping("/chklogins")
    public String chklogins(@RequestParam("email") String email, @RequestParam("pwd") String password,
                            HttpServletRequest request) {
        Logger log = Logger.getGlobal();
        log.info("Attempted login by user: " + email);

        // Lấy thông tin từ repository
        String encryptedPassword = repAuth.findPasswordByUid(email);
        Integer role = repAuth.findUserTypeByUid(email);
        String fullName = repUser.findNameByUid(email); // Lấy tên người dùng
        Integer userId = repUser.findUserIdByEmail(email); // Lấy userId

        if (encryptedPassword != null && SecurityUtility.compareBcrypt(encryptedPassword, password)) {
            if (role == 0) { // Chỉ cho phép người dùng với quyền "user"
                request.getSession().setAttribute("user", email);
                request.getSession().setAttribute("name", fullName);
                request.getSession().setAttribute("usr_type", role);
                request.getSession().setAttribute("userId", userId); // Lưu userId vào session

                return "redirect:/"; // Chuyển hướng về trang chính
            } else {
                request.setAttribute("error", "You do not have permission to log in as a user.");
                return "user/login"; // Không đủ quyền, quay lại đăng nhập
            }
        } else {
            request.setAttribute("error", "Invalid email or password");
            return "user/login"; // Thông tin đăng nhập không hợp lệ
        }
    }


    // Trang đăng ký
    @GetMapping("/register")
    public String register() {
        return "user/register";
    }

    @PostMapping("/send-otp")
    @ResponseBody
    public Map<String, Object> sendOtp(@RequestParam String email, HttpServletRequest request) {
        String otpCode = SecurityUtility.generateOtp();
        request.getSession().setAttribute("otpCode", otpCode);
        request.getSession().setAttribute("otpExpiry", LocalDateTime.now().plusMinutes(5));

        Map<String, Object> response = new HashMap<>();
        if (mailService.sendOtpMail(email, otpCode)) {
            response.put("success", true);
            response.put("message", "OTP sent to your email.");
        } else {
            response.put("success", false);
            response.put("message", "Failed to send OTP. Please try again.");
        }
        return response;
    }

    @PostMapping("/add")
    public String addUser(@RequestParam String email, 
                          @RequestParam String pwd, 
                          @RequestParam String confirmPwd,
                          @RequestParam String name,
                          @RequestParam String phone,
                          @RequestParam String address,
                          @RequestParam String otp,
                          HttpServletRequest request,
                          Model model) {

        String sessionOtp = (String) request.getSession().getAttribute("otpCode");
        LocalDateTime otpExpiry = (LocalDateTime) request.getSession().getAttribute("otpExpiry");

        if (!pwd.equals(confirmPwd)) {
            model.addAttribute("error", "Passwords do not match.");
            return "user/register";
        }
        
        if (sessionOtp == null || otpExpiry == null || LocalDateTime.now().isAfter(otpExpiry)) {
            model.addAttribute("error", "OTP expired. Please request a new OTP.");
            return "user/register";
        }

        if (!otp.equals(sessionOtp)) {
            model.addAttribute("error", "Invalid OTP.");
            return "user/register";
        }

        User user = new User();
        user.setEmail(email);
        user.setPwd(SecurityUtility.encryptBcrypt(pwd));
        user.setName(name);
        user.setCreatedAt(LocalDateTime.now());
        user.setPhone(phone);
        user.setAddress(address);
        user.setStatus(1);
        user.setUsrType(0);

        repUser.addUser(user);

        return "redirect:/user/login";
    }
    
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate(); // Xóa thông tin phiên
        return "redirect:/user/login"; // Chuyển hướng đến trang đăng nhập
    }
    @GetMapping("/forgot-password")
    public String forgotPasswordEmail() {
        return "user/forgot-password-email"; // Trả về giao diện nhập email
    }

    // Trang xác minh OTP
    @GetMapping("/forgot-password/verify-otp")
    public String forgotPasswordVerifyOtp() {
        return "user/forgot-password-verify-otp"; // Trả về giao diện xác minh OTP
    }

    // Trang đặt lại mật khẩu
    @GetMapping("/forgot-password/reset-password")
    public String forgotPasswordReset() {
        return "user/forgot-password-reset"; // Trả về giao diện đặt lại mật khẩu
    }

    // POST - Gửi OTP cho "Quên mật khẩu"
    @PostMapping("/forgot-password/send-otp")
    @ResponseBody
    public Map<String, Object> sendForgotPasswordOtp(@RequestParam String email, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra email tồn tại
        User user = repUser.findUserByEmail(email);
        if (user == null) {
            response.put("success", false);
            response.put("message", "Email not registered.");
            return response;
        }

        // Tạo OTP và lưu vào session
        String otpCode = SecurityUtility.generateOtp();
        request.getSession().setAttribute("forgotPasswordOtp", otpCode);
        request.getSession().setAttribute("forgotPasswordOtpExpiry", LocalDateTime.now().plusMinutes(5));
        request.getSession().setAttribute("email", email); // Lưu email vào session

        // Gửi OTP qua email
        if (mailService.sendOtpMail(email, otpCode)) {
            response.put("success", true);
            response.put("message", "OTP sent to your email.");
        } else {
            response.put("success", false);
            response.put("message", "Failed to send OTP. Please try again.");
        }
        return response;
    }

    // POST - Xác minh OTP
    @PostMapping("/forgot-password/verify-otp")
    @ResponseBody
    public Map<String, Object> verifyForgotPasswordOtp(@RequestParam String otp, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        String sessionOtp = (String) request.getSession().getAttribute("forgotPasswordOtp");
        LocalDateTime otpExpiry = (LocalDateTime) request.getSession().getAttribute("forgotPasswordOtpExpiry");

        if (sessionOtp == null || otpExpiry == null || LocalDateTime.now().isAfter(otpExpiry)) {
            response.put("success", false);
            response.put("message", "OTP expired. Please request a new OTP.");
            return response;
        }

        if (!otp.equals(sessionOtp)) {
            response.put("success", false);
            response.put("message", "Invalid OTP.");
            return response;
        }

        // OTP hợp lệ
        request.getSession().setAttribute("otpVerified", true); // Đánh dấu OTP đã được xác minh
        response.put("success", true);
        response.put("message", "OTP verified. You can reset your password.");
        return response;
    }

    @PostMapping("/forgot-password/reset-password")
    public String resetPassword(@RequestParam String newPwd,
                                @RequestParam String confirmPwd,
                                HttpServletRequest request,
                                Model model) {

        // Kiểm tra xem OTP đã được xác minh hay chưa
        Boolean otpVerified = (Boolean) request.getSession().getAttribute("otpVerified");
        String email = (String) request.getSession().getAttribute("email"); // Lấy email từ session

        if (otpVerified == null || !otpVerified) {
            model.addAttribute("error", "Please verify OTP before resetting password.");
            return "user/forgot-password-verify-otp"; // Chuyển hướng đến giao diện xác minh OTP
        }

        if (!newPwd.equals(confirmPwd)) {
            model.addAttribute("error", "Passwords do not match.");
            return "user/forgot-password-reset"; // Quay lại trang đặt lại mật khẩu nếu mật khẩu không trùng khớp
        }

        // Kiểm tra email hợp lệ và cập nhật mật khẩu mới
        if (email == null) {
            model.addAttribute("error", "Invalid request. Email not found.");
            return "user/forgot-password-email"; // Quay lại trang nhập email nếu email không tồn tại
        }

        // Cập nhật mật khẩu mới
        User user = repUser.findUserByEmail(email);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "user/forgot-password-reset"; // Nếu không tìm thấy người dùng
        }

        // Mã hóa mật khẩu mới và cập nhật vào cơ sở dữ liệu
        user.setPwd(SecurityUtility.encryptBcrypt(newPwd));
        repUser.updateUser(user);

        // Xóa session liên quan đến OTP và email
        request.getSession().removeAttribute("forgotPasswordOtp");
        request.getSession().removeAttribute("forgotPasswordOtpExpiry");
        request.getSession().removeAttribute("otpVerified");
        request.getSession().removeAttribute("email");

        return "redirect:/user/login"; // Điều hướng đến trang đăng nhập sau khi cập nhật mật khẩu
    }


}
