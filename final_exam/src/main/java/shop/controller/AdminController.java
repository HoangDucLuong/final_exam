package shop.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.Contract;
import shop.model.Meal;
import shop.model.MealRequest;
import shop.model.MealRequestDetail;
import shop.model.User;
import shop.repository.AuthRepository;
import shop.repository.ContractRepository;
import shop.repository.MealRepository;
import shop.repository.MealRequestDetailRepository;
import shop.repository.MealRequestRepository;
import shop.repository.UserRepository;
import shop.utils.SecurityUtility;

@Controller
@RequestMapping("/admin")
public class AdminController {
//controller
	@Autowired
	AuthRepository repAuth;
	@Autowired
	UserRepository repUser;
	@Autowired
	private MealRequestRepository mealRequestRepository;
	@Autowired
	private ContractRepository contractRepository;

	@Autowired
	private MealRequestDetailRepository mealRequestDetailRepository;
	@Autowired
	private MealRepository mealRepository;

	@GetMapping("/index")
	public String viewMealRequests(Model model) {
		List<MealRequest> mealRequests = mealRequestRepository.getAllMealRequests();

		for (MealRequest mealRequest : mealRequests) {
			List<MealRequestDetail> details = mealRequestDetailRepository
					.getDetailsByMealRequestId(mealRequest.getId());
			mealRequest.setMealRequestDetails(details);
			Integer userId = mealRequest.getUserId();
			if (userId != null) {
				User user = repUser.findById(userId);
				mealRequest.setUser(user);
			}
			Contract contract = contractRepository.getContractById(mealRequest.getContractId());

			if (contract != null) {
				mealRequest.setContract(contract);
			}

			switch (mealRequest.getStatus()) {
			case 0:
				mealRequest.setStatusText("Pending");
				break;
			case 1:
				mealRequest.setStatusText("Confirmed");
				break;
			case 2:
				mealRequest.setStatusText("Delivered");
				break;
			default:
				mealRequest.setStatusText("Unknown");
			}
		}

		model.addAttribute("mealRequests", mealRequests);
		return "admin/index";
	}

	@GetMapping("/meal-request/detail")
	public String getMealRequestDetail(@RequestParam("id") int id, @RequestParam("userId") int userId, Model model) {
		MealRequest mealRequest = mealRequestRepository.findById(id);

		if (mealRequest == null) {
			return "error/404";
		}

		if (userId != 0) {
			User user = repUser.findById(userId);
			mealRequest.setUser(user);
		}

		Contract contract = contractRepository.getContractById(mealRequest.getContractId());
		if (contract != null) {
			mealRequest.setContract(contract);
		}

		List<MealRequestDetail> mealRequestDetails = mealRequestDetailRepository.getDetailsByMealRequestId(id);
		for (MealRequestDetail detail : mealRequestDetails) {
			Optional<Meal> optionalMeal = mealRepository.findById(detail.getMealId());
			if (optionalMeal.isPresent()) {
				detail.setMealName(optionalMeal.get().getMealName());
			} else {
				detail.setMealName("Unknown Meal");
			}
		}

		model.addAttribute("mealRequest", mealRequest);
		model.addAttribute("mealRequestDetails", mealRequestDetails);

		return "admin/meal-request-detail";
	}

	@GetMapping("/meal-request/approve/{id}")
	public String approveMealRequest(@PathVariable("id") int id) {
		MealRequest mealRequest = mealRequestRepository.findById(id);
		if (mealRequest != null) {
			mealRequest.setStatus(1); 
			mealRequestRepository.updateMealRequest(mealRequest);
		}
		return "redirect:/admin/index"; 
	}

	@GetMapping("/meal-request/reject/{id}")
	public String rejectMealRequest(@PathVariable("id") int id) {
		MealRequest mealRequest = mealRequestRepository.findById(id);
		if (mealRequest != null) {
			mealRequest.setStatus(2); 
			mealRequestRepository.updateMealRequest(mealRequest);
		}
		return "redirect:/admin/index"; 
	}

	// Trang đăng nhập
	@GetMapping("/login")
	public String login() {
		return "admin/login";
	}

	@PostMapping("/chklogins")
	public String chkLogins(@RequestParam("email") String email, @RequestParam("pwd") String password,
			HttpServletRequest request) {
		Logger log = Logger.getGlobal();
		log.info("Attempted login by user: " + email);

		String encryptedPassword = repAuth.findPasswordByUid(email);
		Integer role = repAuth.findUserTypeByUid(email);

		if (encryptedPassword == null || !SecurityUtility.compareBcrypt(encryptedPassword, password)) {
			request.setAttribute("error", "Invalid email or password");
			return "admin/login"; 
		}

		if (role == 1) { 
			request.getSession().setAttribute("usr_type", role);
			return "redirect:/admin/index";
		} else {
			request.setAttribute("error", "You do not have permission to access the admin area.");
			return "admin/login"; 
		}
	}

	@GetMapping("/register")
	public String register() {
		return "admin/register";
	}

	@PostMapping("/add")
	public String addUser(@RequestParam String email, @RequestParam String pwd, @RequestParam String name,
			@RequestParam String phone, @RequestParam String address) {
		if (address == null || address.isEmpty()) {
			return "redirect:/admin/register?error=Address is required";
		}

		User user = new User();
		user.setEmail(email);

		// Hash mật khẩu
		String hashedPassword = SecurityUtility.encryptBcrypt(pwd);
		user.setPwd(hashedPassword); 

		user.setName(name);
		user.setCreatedAt(LocalDateTime.now());
		user.setPhone(phone); 
		user.setAddress(address); 
		user.setStatus(1); 
		user.setUsrType(0);

		repUser.addUser(user);

		return "redirect:/admin/index";
	}
}
