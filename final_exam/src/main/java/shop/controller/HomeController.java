package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.User;
import shop.repository.UserRepository;
import shop.utils.SecurityUtility;

@Controller
public class HomeController {

    @Autowired
    UserRepository repUser;

    @GetMapping("/")
    public ModelAndView showHomePage() {
        return new ModelAndView("home/index");
    }

    @GetMapping("/about")
    public ModelAndView showAboutPage() {
        return new ModelAndView("home/about");
    }

    @GetMapping("/contact")
    public ModelAndView showContactPage() {
        return new ModelAndView("home/contact");
    }

    @GetMapping("/service")
    public ModelAndView showServicePage() {
        return new ModelAndView("home/service");
    }

    @GetMapping("/team")
    public ModelAndView showTeamPage() {
        return new ModelAndView("home/team");
    }

    @GetMapping("/testimonial")
    public ModelAndView showTestimonialPage() {
        return new ModelAndView("home/testimonial");
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        String email = (String) request.getSession().getAttribute("user");
        if (email != null) {
            User user = repUser.findUserByEmail(email); // Lấy thông tin người dùng từ CSDL
            model.addAttribute("user", user); // Đưa thông tin người dùng vào Model

            // Thêm thông báo nếu có
            if (request.getAttribute("success") != null) {
                model.addAttribute("success", request.getAttribute("success"));
            }
            if (request.getAttribute("error") != null) {
                model.addAttribute("error", request.getAttribute("error"));
            }

            return "home/profile"; // Trả về trang profile
        }
        return "redirect:/user/login"; // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
    }

    // Xử lý việc cập nhật thông tin người dùng
    @PostMapping("/update")
    public String updateProfile(@RequestParam String name, @RequestParam String phone, @RequestParam String address,
            HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = repUser.findUserByEmail(email); // Lấy người dùng theo email
            user.setName(name); // Cập nhật tên mới
            user.setPhone(phone); // Cập nhật số điện thoại mới
            user.setAddress(address); // Cập nhật địa chỉ mới

            repUser.updateUser(user); // Gọi phương thức update để lưu thay đổi vào cơ sở dữ liệu

            request.setAttribute("success", "Cập nhật thông tin thành công!"); // Thông báo thành công
            return "redirect:/profile"; // Quay lại trang profile sau khi cập nhật
        }
        return "redirect:/user/login"; // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
            @RequestParam String confirmPassword, HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = repUser.findUserByEmail(email); // Lấy người dùng theo email

            // Kiểm tra mật khẩu cũ
            if (user != null && SecurityUtility.compareBcrypt(user.getPwd(), oldPassword)) {
                if (newPassword.equals(confirmPassword)) {
                    user.setPwd(SecurityUtility.encryptBcrypt(newPassword)); // Cập nhật mật khẩu mới
                    repUser.updateUser(user); // Gọi phương thức update để lưu thay đổi vào cơ sở dữ liệu
                    return "redirect:/profile";
                } else {
                    return "redirect:/profile";// Thông báo lỗi
                }
            } else {
                return "redirect:/profile";
            }
        } else {
            return "redirect:/profile"; // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
        }
    }
}
