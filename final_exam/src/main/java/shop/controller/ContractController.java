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
import java.util.Optional;

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
    private MealRepository mealRepository;

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

                List<Meal> meals = mealRepository.findAll();
                model.addAttribute("meals", meals);

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
                List<ContractDetail> contractDetails = contractDetailRepository.getContractDetailsByContractId(id);
                List<Meal> meals = mealRepository.getMealsByContractId(id);
                model.addAttribute("contract", contract);
                model.addAttribute("contractDetails", contractDetails);
                model.addAttribute("meals", meals);
                return "user/contract-details";
            }
        }
        return "redirect:/user/login";
    }
    @GetMapping("/meals/{contractId}")
    @ResponseBody
    public List<Meal> getMealsByContractId(@PathVariable("contractId") int contractId) {
        // Giả sử bạn có một phương thức trong MealRepository để lấy món ăn theo contractId
        return mealRepository.getMealsByContractId(contractId);
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
    public String showCreateContractForm() {
        return "user/create-contract";
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
            Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
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
                    return "user/create-contract";
                }
            }
        }
        return "redirect:/user/login";
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
                        contractDetail.setMealId(detail.getMealId());

                        mealRepository.findById(detail.getMealId()).ifPresent(meal ->
                            contractDetail.setDescription("Món ăn từ menu " + meal.getMealName())
                        );
                        contractDetailRepository.saveContractDetail(contractDetail);
                    }
                } else if (customMenuIds != null && !customMenuIds.isEmpty()) {
                    for (Integer mealId : customMenuIds) {
                        mealRepository.findById(mealId).ifPresent(meal -> {
                            ContractDetail contractDetail = new ContractDetail();
                            contractDetail.setContractId(contract.getId());
                            contractDetail.setMealId(mealId);
                            contractDetail.setDescription("Món ăn tự chọn: " + meal.getMealName());
                            contractDetailRepository.saveContractDetail(contractDetail);
                        });
                    }
                }
                return "redirect:/user/contracts";
            }
        }
        return "redirect:/user/login";
    }
}