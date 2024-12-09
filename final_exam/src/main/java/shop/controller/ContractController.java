package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.Contract;
import shop.model.ContractDetail;
import shop.model.MenuDetails;
import shop.model.User;
import shop.model.Menu;

import shop.repository.ContractRepository;
import shop.repository.ContractDetailRepository;
import shop.repository.MenuDetailsRepository;
import shop.repository.MenuRepository;
import shop.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/user")
public class ContractController {

	@Autowired
	private ContractRepository contractRepository;

	@Autowired
	private ContractDetailRepository contractDetailRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MenuDetailsRepository menuDetailsRepository;

	@Autowired
	private MenuRepository menuRepository;

	@GetMapping("/contracts")
	public String getAllContractsForUser(HttpServletRequest request, Model model) {
		String email = (String) request.getSession().getAttribute("user");
		if (email != null) {
			Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				List<Contract> userContracts = contractRepository.getContractsByUserId(user.getId());
				model.addAttribute("contracts", userContracts);

				List<Menu> menus = menuRepository.getAllMenus();
				model.addAttribute("menus", menus);

				return "user/contracts";
			}
		}
		return "redirect:/user/login";
	}

	@GetMapping("/contracts/{id}")
	public String getContractById(@PathVariable("id") int id, HttpServletRequest request, Model model) {
	    String email = (String) request.getSession().getAttribute("user");
	    if (email != null) {
	        Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
	        if (userOptional.isPresent()) {
	            User user = userOptional.get();
	            Contract contract = contractRepository.getContractById(id);

	            if (contract == null || contract.getUsrId() != user.getId()) {
	                return "redirect:/user/contracts?error=notfound";
	            }

	            // Lấy tất cả chi tiết hợp đồng
	            List<ContractDetail> contractDetails = contractDetailRepository.getContractDetailsByContractId(id);

	            // Lấy tất cả menu liên kết với hợp đồng này
	            List<Menu> menus = contractRepository.findMenusByContractId(id);

	            // Lấy giá từng menu
	            Map<Integer, BigDecimal> menuPrices = new HashMap<>();
	            for (Menu menu : menus) {
	                BigDecimal price = menuRepository.getMenuPriceById(menu.getId());
	                menuPrices.put(menu.getId(), price);
	            }

	            model.addAttribute("contract", contract);
	            model.addAttribute("contractDetails", contractDetails);
	            model.addAttribute("menus", menus);
	            model.addAttribute("menuPrices", menuPrices);

	            return "user/contract-details";
	        }
	    }
	    return "redirect:/user/login";
	}

	@PostMapping("/cancel")
	public String cancelContract(@RequestParam("contractId") int contractId, HttpServletRequest request, Model model) {
		String email = (String) request.getSession().getAttribute("user");
		if (email != null) {
			Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				Contract contract = contractRepository.getContractById(contractId);
				if (contract != null && contract.getUsrId() == user.getId()) {
					contractRepository.updateContractStatus(contractId, 4); // 4: Cancelled
					model.addAttribute("message", "Hợp đồng đã bị hủy.");
					return "redirect:/user/contracts";
				}
			}
		}
		return "redirect:/user/login";
	}

	@GetMapping("/create-contract")
	public String showCreateContractForm(HttpServletRequest request, Model model) {
		String email = (String) request.getSession().getAttribute("user");
		if (email != null) {
			Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				model.addAttribute("userName", user.getName());
			}
		}

		// Lấy danh sách menu
		List<Menu> menus = menuRepository.getAllMenus();
		model.addAttribute("menus", menus);

		// Tính và truyền giá của từng menu vào model
		Map<Integer, BigDecimal> menuPrices = new HashMap<>();
		for (Menu menu : menus) {
			BigDecimal menuPrice = menuRepository.getMenuPriceById(menu.getId());
			menuPrices.put(menu.getId(), menuPrice);
		}
		model.addAttribute("menuPrices", menuPrices);

		// Log ra menuPrices và menus
		System.out.println("Menus: " + menus);
		System.out.println("Menu Prices: " + menuPrices);

		return "user/create-contract";
	}

	@PostMapping("/create-contract")
	public String createContract(@RequestParam("menuId") List<Integer> menuIds, 
	                             @RequestParam("startDate") String startDate,
	                             @RequestParam("contractDuration") Integer contractDuration, 
	                             HttpServletRequest request, 
	                             Model model) {
		 System.out.println("Received menuIds: " + menuIds); 
	    String email = (String) request.getSession().getAttribute("user");

	    if (email != null) {
	        Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
	        if (userOptional.isPresent()) {
	            User user = userOptional.get();
	            try {
	                if (contractDuration < 6) {
	                    model.addAttribute("error", "Thời gian hợp đồng phải lớn hơn hoặc bằng 6 tháng.");
	                    return "user/create-contract";  // Trả về form nếu thời gian không hợp lệ
	                }
	                if (menuIds == null || menuIds.isEmpty()) {
	                    model.addAttribute("error", "Bạn phải chọn ít nhất một menu.");
	                    return "user/create-contract"; // Quay lại form với thông báo lỗi
	                }

	                LocalDate start = LocalDate.parse(startDate);
	                LocalDate endDate = start.plusMonths(contractDuration);

	                // Tính tổng giá của menu
	                BigDecimal totalMenuPrice = BigDecimal.ZERO;
	                List<MenuDetails> menuDetails = new ArrayList<>();

	                // Lấy chi tiết của tất cả menuId đã chọn
	                for (Integer menuId : menuIds) {
	                    List<MenuDetails> details = menuDetailsRepository.findByMenuId(menuId);
	                    if (details.isEmpty()) {
	                        model.addAttribute("error", "Một trong các menu không có chi tiết nào.");
	                        return "user/create-contract";
	                    }
	                    menuDetails.addAll(details);
	                }

	                // Tính tổng giá hợp đồng
	                for (MenuDetails detail : menuDetails) {
	                    BigDecimal mealPrice = detail.getPrice();
	                    totalMenuPrice = totalMenuPrice.add(mealPrice);
	                }

	                BigDecimal totalContractAmount = totalMenuPrice.multiply(BigDecimal.valueOf(contractDuration));
	                BigDecimal depositAmount = totalContractAmount.multiply(BigDecimal.valueOf(0.1));

	                // Tạo và lưu hợp đồng mới
	                Contract newContract = new Contract();
	                newContract.setUsrId(user.getId());
	                newContract.setStartDate(start);
	                newContract.setEndDate(endDate);
	                newContract.setDepositAmount(depositAmount);
	                newContract.setTotalAmount(totalContractAmount);
	                newContract.setStatus(2); // Trạng thái "In Progress"
	                newContract.setPaymentStatus(0); // Trạng thái "Unpaid"

	                // Lưu hợp đồng vào cơ sở dữ liệu
	                contractRepository.addContract(newContract);

	                // Lưu từng chi tiết hợp đồng với menuId đã chọn
	                for (MenuDetails detail : menuDetails) {
	                    ContractDetail contractDetail = new ContractDetail();
	                    contractDetail.setContractId(newContract.getId());
	                    contractDetail.setMenuId(detail.getMenuId());
	                    contractDetail.setDescription("Chi tiết hợp đồng cho menu item " + detail.getMenuId());

	                    // Lưu chi tiết hợp đồng vào cơ sở dữ liệu
	                    contractDetailRepository.saveContractDetail(contractDetail);
	                }

	                return "redirect:/user/contracts";
	            } catch (Exception e) {
	                e.printStackTrace();
	                model.addAttribute("error", "Đã có lỗi xảy ra khi tạo hợp đồng: " + e.getMessage());
	                return "user/create-contract";
	            }
	        }
	    }
	    return "redirect:/user/login";
	}



	@PostMapping("/contracts/save")
	public String saveContract(@RequestParam(value = "menuId", required = false) Integer menuId,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate,
			@RequestParam("totalAmount") BigDecimal totalAmount, HttpServletRequest request) {

		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
			if (userOptional.isPresent()) {
				User user = userOptional.get();

				Contract contract = new Contract();
				contract.setUsrId(user.getId());
				contract.setStartDate(LocalDate.parse(startDate));
				contract.setEndDate(LocalDate.parse(endDate));
				contract.setTotalAmount(totalAmount);
				contract.setStatus(0);
				contractRepository.save(contract);

				if (menuId != null && menuId > 0) {
					List<MenuDetails> menuDetails = menuDetailsRepository.findByMenuId(menuId);
					for (MenuDetails detail : menuDetails) {
						ContractDetail contractDetail = new ContractDetail();
						contractDetail.setContractId(contract.getId());
						contractDetail.setMenuId(detail.getMenuId());

						contractDetailRepository.saveContractDetail(contractDetail);
					}
				}

				return "redirect:/user/contracts";
			}
		}
		return "redirect:/user/login";
	}
}