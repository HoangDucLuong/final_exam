package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import shop.model.Contract;
import shop.model.ContractDetail;
import shop.model.Meal; // Giả sử bạn có một model cho Meal
import shop.repository.ContractRepository;
import shop.repository.ContractDetailRepository;
import shop.repository.MealRepository; // Repository để lấy thông tin món ăn

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ContractDetailRepository contractDetailRepository;
    @Autowired
    private MealRepository mealRepository; // Thêm repository cho món ăn

    // Lấy danh sách hợp đồng của user
    @GetMapping("/user/{usrId}")
    public String getAllContractsForUser(@PathVariable("usrId") int usrId, Model model) {
        List<Contract> userContracts = contractRepository.getContractsByUserId(usrId);
        model.addAttribute("contracts", userContracts);
        return "user/contracts"; // Trả về trang danh sách hợp đồng cho user
    }

    // Chi tiết hợp đồng (có thể dùng chung cho cả user và admin)
    @GetMapping("/{id}")
    public String getContractById(@PathVariable("id") int id, Model model) {
        Contract contract = contractRepository.getContractById(id);
        if (contract == null) {
            return "redirect:/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
        }
        List<ContractDetail> contractDetails = contractDetailRepository.getContractDetailsByContractId(id);
        List<Meal> meals = mealRepository.getMealsByContractId(id); // Lấy danh sách món ăn theo hợp đồng

        model.addAttribute("contract", contract);
        model.addAttribute("contractDetails", contractDetails);
        model.addAttribute("meals", meals); // Thêm danh sách món ăn vào model
        return "user/contract-details"; // Trả về trang chi tiết hợp đồng (có thể dùng chung)
    }

    // Lấy danh sách món ăn theo hợp đồng
    @GetMapping("/meals/{contractId}")
    @ResponseBody
    public List<Meal> getMealsByContractId(@PathVariable("contractId") int contractId) {
        // Giả sử bạn có một phương thức trong MealRepository để lấy món ăn theo contractId
        return mealRepository.getMealsByContractId(contractId);
    }

    // Hủy hợp đồng - User có thể hủy hợp đồng của mình
    @PostMapping("/user/cancel")
    public String cancelContract(@RequestParam("contractId") int contractId, @RequestParam("usrId") int usrId,
                                 Model model) {
        contractRepository.updateContractStatus(contractId, 4); // 4: Cancelled
        model.addAttribute("message", "Hợp đồng đã bị hủy.");
        return "redirect:/contracts/user/" + usrId; // Quay lại trang hợp đồng của user
    }

    // Tạo hợp đồng mới cho user
    @PostMapping("/create-contract")
    public String createContract(
            @RequestParam("startDate") LocalDate startDate,
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
