package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import shop.model.User;
import shop.repository.UserRepository;
import java.util.List;

@Controller
public class UserManagementController {

    @Autowired
    private UserRepository userRepository;

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
}
