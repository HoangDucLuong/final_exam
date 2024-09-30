package shop.controller;

import shop.model.Contract;
import shop.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/contracts")
public class ContractController {

    @Autowired
    private ContractRepository contractRepository;

    // Hiển thị danh sách hợp đồng
    @GetMapping
    public String listContracts(Model model) {
        List<Contract> contracts = contractRepository.getAllContracts();
        model.addAttribute("contracts", contracts);
        return "admin/contracts"; // View cho danh sách hợp đồng
    }

    // Hiển thị form thêm hợp đồng
 // Hiển thị form thêm hợp đồng
    @GetMapping("/add")
    public String showAddForm(@RequestParam("userId") int userId, Model model) {
        Contract contract = new Contract();
        contract.setUsrId(userId); // Thiết lập ID người dùng vào hợp đồng
        model.addAttribute("contract", contract);
        return "admin/add_contract"; // View cho form thêm hợp đồng
    }

    // Thêm hợp đồng mới
    @PostMapping("/add")
    public String addContract(@ModelAttribute Contract contract) {
        contractRepository.addContract(contract);
        return "redirect:/admin/contracts";
    }

    // Xóa hợp đồng
    @GetMapping("/delete/{id}")
    public String deleteContract(@PathVariable int id) {
        contractRepository.deleteContract(id);
        return "redirect:/admin/contracts";
    }

    // Hiển thị form cập nhật hợp đồng
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Contract contract = contractRepository.getContractById(id);
        model.addAttribute("contract", contract);
        return "admin/edit_contract"; // View cho form cập nhật hợp đồng
    }

    // Cập nhật hợp đồng
    @PostMapping("/edit")
    public String updateContract(@ModelAttribute Contract contract) {
        contractRepository.updateContract(contract);
        return "redirect:/admin/contracts";
    }
}
