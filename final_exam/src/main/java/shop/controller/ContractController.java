package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import shop.model.Contract;
import shop.model.ContractDetail;
import shop.model.Meal;
import shop.model.User;
import shop.repository.ContractRepository;
import shop.repository.MealRepository;
import shop.repository.UserRepository;
import shop.repository.ContractDetailRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

	@Autowired
	private ContractRepository contractRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MealRepository mealRepository;

	@Autowired
	private ContractDetailRepository contractDetailRepository;

	// Phần dành cho admin - Lấy danh sách tất cả hợp đồng
	@GetMapping("/admin")
	public String getAllContractsForAdmin(Model model) {
		List<Contract> contracts = contractRepository.getAllContracts();
		model.addAttribute("contracts", contracts);
		return "admin/contracts"; // Trả về trang danh sách hợp đồng cho admin
	}

	// Phần dành cho user - Lấy danh sách hợp đồng của user
	@GetMapping("/user/{usrId}")
	public String getAllContractsForUser(@PathVariable("usrId") int usrId, Model model) {
		List<Contract> userContracts = contractRepository.getContractsByUserId(usrId);
		model.addAttribute("contracts", userContracts);
		return "user/contracts"; // Trả về trang danh sách hợp đồng cho user
	}

	// Chi tiết hợp đồng - Dùng cho cả user và admin
	@GetMapping("/{id}")
	public String getContractById(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}
		List<ContractDetail> contractDetails = contractDetailRepository.getContractDetailsByContractId(id);
		model.addAttribute("contract", contract);
		model.addAttribute("contractDetails", contractDetails);
		return "contract-details"; // Trả về trang chi tiết hợp đồng (có thể dùng chung)
	}

	// Thêm mới hợp đồng - Phần này user có thể dùng khi tạo hợp đồng
//    @PostMapping("/add")
//    public String addContract(@ModelAttribute Contract contract, Model model) {
//        contractRepository.addContract(contract);
//        return "redirect:/contracts/user/" + contract.getUsrId(); // Quay lại trang danh sách hợp đồng của user
//    }

	// Cập nhật hợp đồng - Phần này admin có thể dùng để cập nhật thông tin hợp đồng
	@PostMapping("/admin/update/{id}")
	public String updateContract(@PathVariable("id") int id, @ModelAttribute Contract contract) {
		Contract existingContract = contractRepository.getContractById(id);
		if (existingContract == null) {
			return "redirect:/contracts/admin?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}
		contract.setId(id);
		contractRepository.updateContract(contract);
		return "redirect:/contracts/admin"; // Quay lại trang danh sách hợp đồng của admin
	}

	// Xóa hợp đồng - Admin có thể xóa
	@GetMapping("/admin/delete/{id}")
	public String deleteContract(@PathVariable("id") int id) {
		Contract existingContract = contractRepository.getContractById(id);
		if (existingContract != null) {
			contractRepository.deleteContract(id);
		}
		return "redirect:/contracts/admin"; // Quay lại trang danh sách hợp đồng của admin
	}

	// Hủy hợp đồng - User có thể hủy hợp đồng của mình
	@PostMapping("/user/cancel")
	public String cancelContract(@RequestParam("contractId") int contractId, @RequestParam("usrId") int usrId,
			Model model) {
		contractRepository.updateContractStatus(contractId, 4); // 4: Cancelled
		model.addAttribute("message", "Hợp đồng đã bị hủy.");
		return "redirect:/contracts/user/" + usrId; // Quay lại trang hợp đồng của user
	}

//    @PostMapping("/create-contract")
//    public String createContract(@RequestParam("usrId") int usrId, @RequestParam("mealId") int mealId) {
//        // Lấy thông tin người dùng và món ăn từ database
//        User user = userRepository.findById(usrId);
//        Meal meal = mealRepository.findById(mealId);
//
//        // Kiểm tra nếu user hoặc meal không tồn tại
//        if (user == null || meal == null) {
//            return "redirect:/menu?error=notfound"; // Xử lý khi không tìm thấy user hoặc meal
//        }
//
//        // Tạo hợp đồng mới
//        Contract contract = new Contract();
//        contract.setUsrId(user.getId());
//        contract.setStartDate(LocalDate.now()); // Ngày bắt đầu là ngày hiện tại
//        contract.setStatus(0); // 0: Pending (chưa hoàn tất)
//        contract.setTotalAmount(meal.getPrice()); // Tổng số tiền = giá món ăn
//
//        // Lưu hợp đồng
//        contractRepository.addContract(contract);
//
//        // Chuyển hướng đến trang hợp đồng của user
//        return "redirect:/contracts/user/" + usrId;
//    }
//    
	@PostMapping("/create-contract")
	public String createContract(
	        @RequestParam("startDate") LocalDate startDate, // Thay đổi đây
	        @RequestParam("contractDuration") int contractDuration,
	        @RequestParam("depositAmount") BigDecimal depositAmount, 
	        Model model) {
	    try {
	        // Tính toán ngày kết thúc
	        LocalDate endDate = startDate.plusMonths(contractDuration);

	        // Tạo đối tượng hợp đồng mới
	        Contract newContract = new Contract();
	        newContract.setStartDate(startDate);
	        newContract.setEndDate(endDate);
	        newContract.setDepositAmount(depositAmount);
	        newContract.setStatus(0); // Đặt trạng thái ban đầu là Pending
	        newContract.setPaymentStatus(0); // Đặt trạng thái thanh toán là Unpaid

	        // Lưu hợp đồng vào cơ sở dữ liệu
	        contractRepository.addContract(newContract);

	        return "redirect:/profile"; // Chuyển hướng về trang profile sau khi tạo hợp đồng
	    } catch (Exception e) {
	        // Xử lý lỗi (nếu có)
	        model.addAttribute("error", "Đã có lỗi xảy ra khi tạo hợp đồng: " + e.getMessage());
	        return "user/create-contract"; // Trả về trang tạo hợp đồng để người dùng có thể thử lại
	    }
	}

}
