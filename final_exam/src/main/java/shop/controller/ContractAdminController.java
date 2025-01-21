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
@RequestMapping("/admin/contracts") 
public class ContractAdminController {

	@Autowired
	private ContractRepository contractRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContractDetailRepository contractDetailRepository;

	@Autowired
	private MailService mailService;

	@GetMapping("")
	public String getAllContractsForAdmin(@RequestParam(value = "page", defaultValue = "1") int page,
	        @RequestParam(value = "size", defaultValue = "10") int size,
	        @RequestParam(value = "search", required = false) String search, Model model) {
		
		String keyword = search != null ? search : "";
	    int offset = (page - 1) * size;
	    
		List<Contract> contracts = contractRepository.searchContracts(keyword, offset, size);
		List<Map<String, Object>> contractsWithUser = new ArrayList<>();
		
		int totalContracts = contractRepository.countContracts(keyword);

	    int totalPages = (int) Math.ceil((double) totalContracts / size);
		
		LocalDate today = LocalDate.now();

		for (Contract contract : contracts) {
			User user = userRepository.findById(contract.getUsrId());
			Map<String, Object> contractWithUser = new HashMap<>();
			contractWithUser.put("contract", contract);
			contractWithUser.put("userName", user != null ? user.getName() : "N/A");
			long daysToExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, contract.getEndDate());
			contractWithUser.put("isExpiringSoon", daysToExpiry <= 10 && daysToExpiry >= 0);

			contractsWithUser.add(contractWithUser);
		}
		
		model.addAttribute("contracts", contracts);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", totalPages);
	    model.addAttribute("search", keyword);
		
		model.addAttribute("contracts", contractsWithUser);
		return "admin/contract/contracts"; 
	}

	@GetMapping("/{id}")
	public String getContractByIdForAdmin(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/admin/contracts?error=notfound"; 
		}
		model.addAttribute("contract", contract);
		return "admin/contract/contract-details"; 
	}

	@GetMapping("/edit/{id}")
	public String showEditContractForm(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/admin/contracts?error=notfound"; 
		}
		model.addAttribute("contract", contract);
		return "admin/contract/edit_contract"; 
	}
	@PostMapping("/update/{id}")
public String updateContract(@PathVariable("id") int id, @ModelAttribute Contract contract, Model model) {
	    Contract existingContract = contractRepository.getContractById(id);

	    if (existingContract == null) {
	        return "redirect:/admin/contract/contracts?error=notfound"; 
	    }

	    existingContract.setUsrId(contract.getUsrId());
	    existingContract.setStartDate(contract.getStartDate());
	    existingContract.setEndDate(contract.getEndDate());
	    existingContract.setTotalAmount(contract.getTotalAmount());
	    existingContract.setDepositAmount(contract.getDepositAmount());
	    existingContract.setStatus(contract.getStatus());
	    existingContract.setPaymentStatus(contract.getPaymentStatus());

	    contractRepository.updateContract(existingContract);

	    User user = userRepository.findById(contract.getUsrId());
	    if (user != null) {
	        mailService.sendContractUpdateMail(user.getEmail(), existingContract);
	    }

	    return "redirect:/admin/contracts"; 
	}

	@RequestMapping(value = "/confirm/{id}", method = {RequestMethod.GET, RequestMethod.POST})
	public String confirmContract(@PathVariable("id") int id, Model model) {
	    Contract contract = contractRepository.getContractById(id);
	    if (contract == null) {
	        return "redirect:/admin/contract/contracts?error=notfound"; 
	    }
	    contract.setStatus(1);
	    contractRepository.updateContract(contract); 

	    return "redirect:/user/contracts"; 
	}

	@GetMapping("/delete/{id}")
	public String deleteContract(@PathVariable("id") int id) {
		Contract existingContract = contractRepository.getContractById(id);
		if (existingContract != null) {
			contractRepository.deleteContract(id);
		}
		return "redirect:/admin/contracts"; 
	}

	@GetMapping("/create")
	public String showCreateContractFormForAdmin(@RequestParam("usrId") int usrId, Model model) {
		User user = userRepository.findById(usrId);

		if (user == null) {
			return "redirect:/admin/contract/contracts?error=usernotfound";
		}

		Contract contract = new Contract();
		contract.setUsrId(user.getId());

		model.addAttribute("user", user);
		model.addAttribute("contract", contract);

		return "admin/contract/create-contract"; 
	}

	@PostMapping("/create")
	public String createContractForAdmin(@RequestParam("usrId") int usrId,
			@RequestParam("startDate") LocalDate startDate, @RequestParam("contractDuration") int contractDuration,
			@RequestParam("depositAmount") BigDecimal depositAmount, Model model) {
		try {
			LocalDate endDate = startDate.plusMonths(contractDuration);

			Contract newContract = new Contract();
			newContract.setUsrId(usrId); 
			newContract.setStartDate(startDate);
			newContract.setEndDate(endDate);
			newContract.setDepositAmount(depositAmount);
			newContract.setStatus(0);
			newContract.setPaymentStatus(0);
			contractRepository.addContract(newContract);

			return "redirect:/admin/contracts"; 												
		} catch (Exception e) {
			model.addAttribute("error", "Đã có lỗi xảy ra khi tạo hợp đồng: " + e.getMessage());
			return "admin/contract/create-contract"; 
		}
	}

	@PostMapping("/sendMail/{id}")
	public String sendExpiryNotification(@PathVariable("id") int contractId, Model model) {
	    System.out.println("Gửi thông báo cho hợp đồng ID: " + contractId); 

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
	    return "redirect:/admin/contracts"; 
	}

}