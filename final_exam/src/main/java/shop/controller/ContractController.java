package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.Contract;
import shop.model.ContractDetail;
import shop.model.Meal;
import shop.model.MenuDetails;
import shop.model.User;
import shop.model.Menu;

import shop.repository.ContractRepository;
import shop.repository.ContractDetailRepository;
import shop.repository.MealRepository;
import shop.repository.MenuDetailsRepository;
import shop.repository.MenuRepository;
import shop.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private MealRepository mealRepository; // Khai báo MealRepository

    @Autowired
    private MenuDetailsRepository menuDetailsRepository; // Khai báo MenuDetailsRepository
    @Autowired
    private MenuRepository menuRepository; // Khai báo MenuRepository


    @GetMapping("/contracts")
    public String getAllContractsForUser(HttpServletRequest request, Model model) {
        String email = (String) request.getSession().getAttribute("user");
        if (email != null) {
            User user = userRepository.findUserByEmail(email);
            if (user != null) {
                List<Contract> userContracts = contractRepository.getContractsByUserId(user.getId());
                model.addAttribute("contracts", userContracts);

                // Truyền danh sách các món ăn vào model
                List<Meal> meals = mealRepository.findAll();
                model.addAttribute("meals", meals);

                // Giả sử có danh sách menus cần truyền vào
                List<Menu> menus = menuRepository.getAllMenus();
                model.addAttribute("menus", menus);

                return "user/contracts"; // Trả về trang danh sách hợp đồng cho user
            }
        }
        return "redirect:/user/login"; // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
    }

    @GetMapping("/{id}")
    public String getContractById(@PathVariable("id") int id, HttpServletRequest request, Model model) {
        String email = (String) request.getSession().getAttribute("user");
        if (email != null) {
            User user = userRepository.findUserByEmail(email);
            if (user != null) {
                Contract contract = contractRepository.getContractById(id);
                if (contract == null || contract.getUsrId() != user.getId()) {
                    return "redirect:/contracts?error=notfound"; // Nếu không tìm thấy hợp đồng hoặc hợp đồng không thuộc user
                }
                List<ContractDetail> contractDetails = contractDetailRepository.getContractDetailsByContractId(id);
                model.addAttribute("contract", contract);
                model.addAttribute("contractDetails", contractDetails);
                return "user/contract-details"; // Trả về trang chi tiết hợp đồng cho user
            }
        }
        return "redirect:/user/login"; // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
    }

    @PostMapping("/cancel")
    public String cancelContract(@RequestParam("contractId") int contractId, HttpServletRequest request, Model model) {
        String email = (String) request.getSession().getAttribute("user");
        if (email != null) {
            User user = userRepository.findUserByEmail(email);
            if (user != null) {
                Contract contract = contractRepository.getContractById(contractId);
                if (contract != null && contract.getUsrId() == user.getId()) {
                    contractRepository.updateContractStatus(contractId, 4); // 4: Cancelled
                    model.addAttribute("message", "Hợp đồng đã bị hủy.");
                    return "redirect:/contracts/user"; // Quay lại trang hợp đồng của user
                }
            }
        }
        return "redirect:/user/login"; // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
    }

    @PostMapping("/user/create-contract")
    public String createContract(
    	    @RequestParam("startDate") String startDate,
    	    @RequestParam("contractDuration") Integer contractDuration,
    	    @RequestParam("depositAmount") BigDecimal depositAmount,
    	    HttpServletRequest request,
    	    Model model) {
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = userRepository.findUserByEmail(email);

            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate endDate = start.plusMonths(contractDuration);

                Contract newContract = new Contract();
                newContract.setUsrId(user.getId());
                newContract.setStartDate(start);
                newContract.setEndDate(endDate);
                newContract.setDepositAmount(depositAmount);
                newContract.setStatus(0);
                newContract.setPaymentStatus(0);

                contractRepository.addContract(newContract);

                return "redirect:/user/contracts";
            } catch (Exception e) {
                model.addAttribute("error", "Đã có lỗi xảy ra khi tạo hợp đồng: " + e.getMessage());
                return "user/create-contract"; // Trở về trang tạo hợp đồng với thông báo lỗi
            }
        }
        return "redirect:/user/login"; // Nếu không tìm thấy email, redirect đến trang đăng nhập
    }

    @PostMapping("/contracts/save")
    public String saveContract(
            @RequestParam(value = "menuId", required = false) Integer menuId,
            @RequestParam(value = "customMenuId", required = false) List<Integer> customMenuIds,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("totalAmount") BigDecimal totalAmount,
            HttpServletRequest request) {
        
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = userRepository.findUserByEmail(email);

            // Tạo hợp đồng mới
            Contract contract = new Contract();
            contract.setUsrId(user.getId());
            contract.setStartDate(LocalDate.parse(startDate));
            contract.setEndDate(LocalDate.parse(endDate));
            contract.setTotalAmount(totalAmount);
            contract.setStatus(0); // 0: Pending
            contractRepository.save(contract); // Lưu hợp đồng vào database

            // Nếu người dùng chọn menu có sẵn
            if (menuId != null && menuId > 0) {
                // Lấy chi tiết của menu có sẵn và thêm vào hợp đồng
                List<MenuDetails> menuDetails = menuDetailsRepository.findByMenuId(menuId);
                for (MenuDetails detail : menuDetails) {
                    ContractDetail contractDetail = new ContractDetail();
                    contractDetail.setContractId(contract.getId());
                    contractDetail.setMealId(detail.getMealId());
                    
                    // Lấy thông tin món ăn
                    Meal meal = mealRepository.findById(detail.getMealId()).orElse(null);
                    if (meal != null) {
                        contractDetail.setDescription("Món ăn từ menu " + meal.getMealName());
                    }
                    
                    contractDetailRepository.saveContractDetail(contractDetail);
                }
            } else if (customMenuIds != null && !customMenuIds.isEmpty()) {
                // Nếu người dùng tự chọn món ăn, thêm từng món ăn vào hợp đồng
                for (Integer mealId : customMenuIds) {
                    Meal meal = mealRepository.findById(mealId).orElse(null);
                    if (meal != null) {
                        ContractDetail contractDetail = new ContractDetail();
                        contractDetail.setContractId(contract.getId());
                        contractDetail.setMealId(mealId);
                        contractDetail.setDescription("Món ăn tự chọn: " + meal.getMealName());
                        contractDetailRepository.saveContractDetail(contractDetail);
                    }
                }
            }

            return "redirect:/contracts"; // Quay lại trang hợp đồng
        }
        return "redirect:/user/login"; // Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập
    }

}
