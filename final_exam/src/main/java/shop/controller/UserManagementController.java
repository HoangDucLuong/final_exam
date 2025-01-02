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

	@GetMapping("/admin/user-management")
	public String getAllUsers(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "search", required = false, defaultValue = "") String search) {
		
		int pageSize = 5; 
		int totalUsers = userRepository.countUsers(search); 
		int totalPages = totalUsers > 0 ? (int) Math.ceil((double) totalUsers / pageSize) : 1;
		
		List<User> users = userRepository.findAllUsers(page, search);
		model.addAttribute("users", users);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", search);
		return "admin/user-management";
	}

	@GetMapping("/admin/user/add")
	public String addUserPage() {
		return "admin/add_user";
	}

	@PostMapping("/admin/user/add")
	public String addUser(@RequestParam String email, @RequestParam String pwd, @RequestParam String name,
			@RequestParam String phone, @RequestParam String address) {
		User user = new User();
		user.setEmail(email);
		user.setPwd(SecurityUtility.encryptBcrypt(pwd));
		user.setName(name);
		user.setCreatedAt(LocalDateTime.now());
		user.setPhone(phone);
		user.setAddress(address);
		user.setStatus(1);
		user.setUsrType(0);

		userRepository.addUser(user);

		return "redirect:/admin/user-management";
	}

	@GetMapping("/admin/user/edit/{id}")
	public String editUserPage(@PathVariable("id") int id, Model model) {
		User user = userRepository.findById(id);
		model.addAttribute("user", user);
		return "admin/edit_user";
	}

	@PostMapping("/admin/user/edit")
	public String editUser(@RequestParam int id, @RequestParam String email, @RequestParam String name,
			@RequestParam String phone, @RequestParam String address, @RequestParam int status) {
		User user = userRepository.findById(id);
		if (user != null) {
			user.setEmail(email);
			user.setName(name);
			user.setPhone(phone);
			user.setAddress(address);
			user.setStatus(status);
			userRepository.updateUser(user);
		}
		return "redirect:/admin/user-management";
	}

	@GetMapping("/admin/user/delete/{id}")
	public String deleteUser(@PathVariable("id") int id) {
		userRepository.deleteUser(id);
		return "redirect:/admin/user-management";
	}
}
