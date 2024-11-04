package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import shop.model.Contract;
import shop.model.User;
import shop.repository.ContractRepository;
import shop.repository.UserRepository;
import shop.service.MailService;
import shop.repository.ContractDetailRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/contracts") // Đường dẫn dành riêng cho admin
public class ContractAdminController {

	@Autowired
	private ContractRepository contractRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContractDetailRepository contractDetailRepository;

	@Autowired
	private MailService mailService;

	@GetMapping("/list")
	public String getAllUsers(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "search", required = false, defaultValue = "") String search) {
		// Lấy toàn bộ danh sách người dùng từ repository (không phân trang)
		List<User> allUsers = userRepository.findAll();

		// Lọc người dùng dựa trên từ khóa tìm kiếm
		List<User> filteredUsers = allUsers.stream()
				.filter(user -> user.getName().toLowerCase().contains(search.toLowerCase())
						|| user.getEmail().toLowerCase().contains(search.toLowerCase()))
				.toList();

		// Tính tổng số trang dựa trên số lượng người dùng đã lọc
		int pageSize = 10;
		int totalUsers = filteredUsers.size();
		int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

		// Đảm bảo giá trị page không vượt quá số trang thực tế
		if (page < 1)
			page = 1;
		if (page > totalPages)
			page = totalPages;

		// Lấy danh sách người dùng cho trang hiện tại
		int fromIndex = (page - 1) * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, totalUsers);
		List<User> pagedUsers = filteredUsers.subList(fromIndex, toIndex);

		// Thêm các thông tin cần thiết vào model
		model.addAttribute("users", pagedUsers);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", search); // Truyền từ khóa tìm kiếm vào model

		return "admin/contracts"; // Trả về giao diện HTML
	}

	// Lấy danh sách tất cả hợp đồng cho admin
	@GetMapping("")
	public String getAllContractsForAdmin(Model model) {
		List<Contract> contracts = contractRepository.getAllContracts();
		List<Map<String, Object>> contractsWithUser = new ArrayList<>();

		LocalDate today = LocalDate.now();

		for (Contract contract : contracts) {
			User user = userRepository.findById(contract.getUsrId());
			Map<String, Object> contractWithUser = new HashMap<>();
			contractWithUser.put("contract", contract);
			contractWithUser.put("userName", user != null ? user.getName() : "N/A"); // Lấy tên người dùng
			long daysToExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, contract.getEndDate());
			contractWithUser.put("isExpiringSoon", daysToExpiry <= 10 && daysToExpiry >= 0);
			contractsWithUser.add(contractWithUser);
		}

		model.addAttribute("contracts", contractsWithUser);
		return "admin/contracts"; // Trả về trang danh sách hợp đồng cho admin
	}

	// Chi tiết hợp đồng cho admin
	@GetMapping("/{id}")
	public String getContractByIdForAdmin(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/admin/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}
		model.addAttribute("contract", contract);
		return "admin/contract-details"; // Trả về trang chi tiết hợp đồng cho admin
	}

	// Hiển thị trang chỉnh sửa hợp đồng
	@GetMapping("/edit/{id}")
	public String showEditContractForm(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/admin/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}
		model.addAttribute("contract", contract);
		return "admin/edit_contract"; // Trả về trang chỉnh sửa hợp đồng
	}

	// Admin cập nhật hợp đồng
	@PostMapping("/update/{id}")
	public String updateContract(@PathVariable("id") int id, @ModelAttribute Contract contract) {
		Contract existingContract = contractRepository.getContractById(id);

		if (existingContract == null) {
			return "redirect:/admin/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}


		// Cập nhật các thuộc tính của hợp đồng
		existingContract.setUsrId(contract.getUsrId());
		existingContract.setStartDate(contract.getStartDate());
		existingContract.setEndDate(contract.getEndDate());
		existingContract.setTotalAmount(contract.getTotalAmount());
		existingContract.setDepositAmount(contract.getDepositAmount());
		existingContract.setStatus(contract.getStatus());
		
		// Cập nhật trạng thái thanh toán
        existingContract.setPaymentStatus(contract.getPaymentStatus());

        // Lưu hợp đồng đã cập nhật vào cơ sở dữ liệu
        contractRepository.updateContract(existingContract);
        
        return "redirect:/admin/contracts"; // Quay lại trang danh sách hợp đồng của admin
    }

    // Xác nhận hợp đồng (POST) - đổi tên thành confirmContractPost
    @PostMapping("/confirm/{id}")
    public String confirmContractPost(@PathVariable("id") int id, Model model) {
        Contract contract = contractRepository.getContractById(id);
        if (contract == null) {
            return "redirect:/admin/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
        }
        contract.setStatus(1); // Đặt trạng thái là đã xác nhận
        contractRepository.updateContract(contract); // Cập nhật trạng thái trong cơ sở dữ liệu

        return "redirect:/admin/contracts"; // Chuyển hướng về trang danh sách hợp đồng của admin
    }

	// Admin xóa hợp đồng
	@GetMapping("/delete/{id}")
	public String deleteContract(@PathVariable("id") int id) {
		Contract existingContract = contractRepository.getContractById(id);
		if (existingContract != null) {
			contractRepository.deleteContract(id);
		}
		return "redirect:/admin/contracts"; // Quay lại trang danh sách hợp đồng của admin
	}

	// Admin tạo hợp đồng mới
	@GetMapping("/create")
	public String showCreateContractFormForAdmin(@RequestParam("usrId") int usrId, Model model) {
		// Lấy người dùng từ cơ sở dữ liệu bằng usrId
		User user = userRepository.findById(usrId);

		// Kiểm tra xem người dùng có tồn tại không
		if (user == null) {
			return "redirect:/admin/contracts?error=usernotfound"; // Nếu không tìm thấy người dùng
		}

		// Tạo đối tượng hợp đồng và gán thông tin người dùng
		Contract contract = new Contract();
		contract.setUsrId(user.getId());

		// Thêm người dùng và hợp đồng vào model để hiển thị trong form
		model.addAttribute("user", user);
		model.addAttribute("contract", contract);

		return "admin/create-contract"; // Trả về trang tạo hợp đồng cho admin
	}

	@PostMapping("/create")
	public String createContractForAdmin(@RequestParam("usrId") int usrId,
			@RequestParam("startDate") LocalDate startDate, @RequestParam("contractDuration") int contractDuration,
			@RequestParam("depositAmount") BigDecimal depositAmount, Model model) {
		try {
			// Tính toán ngày kết thúc
			LocalDate endDate = startDate.plusMonths(contractDuration);

			// Tạo đối tượng hợp đồng mới
			Contract newContract = new Contract();
			newContract.setUsrId(usrId); // Gán ID người dùng
			newContract.setStartDate(startDate);
			newContract.setEndDate(endDate);
			newContract.setDepositAmount(depositAmount);
			newContract.setStatus(0); // Trạng thái ban đầu là Pending
			newContract.setPaymentStatus(0); // Trạng thái thanh toán là Unpaid

			// Lưu hợp đồng vào cơ sở dữ liệu
			contractRepository.addContract(newContract);

			return "redirect:/admin/contracts"; // Chuyển hướng về trang danh sách hợp đồng của admin sau khi tạo hợp
												// đồng
		} catch (Exception e) {
			// Xử lý lỗi (nếu có)
			model.addAttribute("error", "Đã có lỗi xảy ra khi tạo hợp đồng: " + e.getMessage());
			return "admin/create-contract"; // Trả về trang tạo hợp đồng để admin có thể thử lại
		}
	}

	@PostMapping("/sendMail/{id}")
	public String sendExpiryNotification(@PathVariable("id") int contractId, Model model) {
	    System.out.println("Gửi thông báo cho hợp đồng ID: " + contractId); // log thông tin hợp đồng

	    Contract contract = contractRepository.getContractById(contractId);
	    if (contract != null) {
	        User user = userRepository.findById(contract.getUsrId());
	        if (user != null) {
	            // Gửi email
	            mailService.sendContractExpiryMail(user.getEmail(), contract);
	            model.addAttribute("mailStatus", "success");
	            model.addAttribute("successMessage", "Email đã được gửi thành công!");
	        } else {
	            model.addAttribute("mailStatus", "failure");
	            model.addAttribute("errorMessage", "Người dùng không tồn tại.");
	        }
	    } else {
	        model.addAttribute("mailStatus", "failure");
	        model.addAttribute("errorMessage", "Hợp đồng không tồn tại.");
	    }
	    return "redirect:/admin/contracts"; // Quay lại trang danh sách hợp đồng
	}

}
