package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import shop.model.User;
import shop.repository.UserRepository;
import shop.utils.SecurityUtility;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class UserManagementController {

    @Autowired
    private UserRepository userRepository;

    // Trang quản lý user
    @GetMapping("/admin/user-management")
    public String getAllUsers(Model model,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        List<User> users = userRepository.findAllUsers(page, search);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search); // Truyền từ khóa tìm kiếm vào model
        return "admin/user-management"; // Tên của giao diện HTML
    }
    
    // Trang tạo user mới
    @GetMapping("/admin/user/add")
    public String addUserPage() {
        return "admin/add_user"; // Trả về trang thêm user
    }

    // Xử lý thêm user mới
    @PostMapping("/admin/user/add")
    public String addUser(@RequestParam String email, 
                          @RequestParam String pwd, 
                          @RequestParam String name,
                          @RequestParam String phone,
                          @RequestParam String address) {
        // Tạo đối tượng User mới
        User user = new User();
        user.setEmail(email);
        user.setPwd(SecurityUtility.encryptBcrypt(pwd)); // Hash mật khẩu
        user.setName(name);
        user.setCreatedAt(LocalDateTime.now());
        user.setPhone(phone);
        user.setAddress(address);
        user.setStatus(1); // Mặc định active
        user.setUsrType(0); // Mặc định là user

        // Lưu user vào database
        userRepository.addUser(user); // Đúng repository được sử dụng ở đây

        return "redirect:/admin/user-management"; // Sau khi thêm thành công, điều hướng về trang quản lý user
    }

    // Trang chỉnh sửa user
    @GetMapping("/admin/user/edit/{id}")
    public String editUserPage(@PathVariable("id") int id, Model model) {
        User user = userRepository.findById(id);
        model.addAttribute("user", user);
        return "admin/edit_user"; // Trả về trang chỉnh sửa user
    }

 // Xử lý việc chỉnh sửa user
    @PostMapping("/admin/user/edit")
    public String editUser(@RequestParam int id,
                           @RequestParam String email, 
                           @RequestParam String name,
                           @RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam int status) {
        User user = userRepository.findById(id);
        if (user != null) {
            user.setEmail(email);
            user.setName(name);
            user.setPhone(phone);
            user.setAddress(address);
            user.setStatus(status); // Cập nhật trạng thái
            userRepository.updateUser(user); // Cập nhật thông tin user
        }
        return "redirect:/admin/user-management"; // Sau khi chỉnh sửa thành công, điều hướng về trang quản lý user
    }
    // Xử lý việc xóa user
    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        userRepository.deleteUser(id); // Xóa user theo ID
        return "redirect:/admin/user-management"; // Sau khi xóa thành công, điều hướng về trang quản lý user
    }
}
