package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import shop.model.MealRequest;
import shop.repository.MealRequestRepository;

import java.util.List;

@Controller
@RequestMapping("/admin/meal-requests")
public class MealRequestController {

    @Autowired
    private MealRequestRepository mealRequestRepository;

    // Lấy danh sách tất cả yêu cầu suất ăn
    @GetMapping
    public String getAllMealRequests(Model model) {
        List<MealRequest> mealRequests = mealRequestRepository.getAllMealRequests();
        model.addAttribute("mealRequests", mealRequests);
        return "admin/meal-requests"; // Thay đổi để trỏ đến giao diện trong folder home
    }

    // Lấy thông tin chi tiết một yêu cầu suất ăn theo ID
    @GetMapping("/{id}")
    public String getMealRequestById(@PathVariable("id") int id, Model model) {
        MealRequest mealRequest = mealRequestRepository.getMealRequestById(id);
        if (mealRequest == null) {
            return "redirect:/admin/meal-requests?error=notfound"; // Xử lý khi không tìm thấy mealRequest
        }
        model.addAttribute("mealRequest", mealRequest);
        return "admin/meal-request-details"; // Trả về giao diện chi tiết
    }

    // Thêm mới yêu cầu suất ăn
    @GetMapping("/add")
    public String addMealRequestForm(Model model) {
        model.addAttribute("mealRequest", new MealRequest());
        return "admin/add-meal-request"; // Trả về trang thêm yêu cầu suất ăn
    }

    @PostMapping("/add")
    public String addMealRequest(@ModelAttribute MealRequest mealRequest) {
        mealRequestRepository.addMealRequest(mealRequest);
        return "redirect:/admin/meal-requests"; // Điều hướng về trang danh sách sau khi thêm
    }

    // Cập nhật yêu cầu suất ăn
    @PostMapping("/update/{id}")
    public String updateMealRequest(@PathVariable("id") int id, @ModelAttribute MealRequest mealRequest) {
        MealRequest existingMealRequest = mealRequestRepository.getMealRequestById(id);
        if (existingMealRequest == null) {
            return "redirect:/admin/meal-requests?error=notfound"; // Xử lý khi không tìm thấy mealRequest
        }
        mealRequest.setId(id);
        mealRequestRepository.updateMealRequest(mealRequest);
        return "redirect:/admin/meal-requests"; // Điều hướng về trang danh sách sau khi cập nhật
    }

    // Xóa yêu cầu suất ăn theo ID
    @GetMapping("/delete/{id}")
    public String deleteMealRequest(@PathVariable("id") int id) {
        MealRequest existingMealRequest = mealRequestRepository.getMealRequestById(id);
        if (existingMealRequest != null) {
            mealRequestRepository.deleteMealRequest(id);
        }
        return "redirect:/admin/meal-requests"; // Điều hướng về trang danh sách sau khi xóa
    }
}
