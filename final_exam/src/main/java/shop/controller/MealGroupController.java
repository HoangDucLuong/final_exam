package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import shop.model.MealGroup;
import shop.repository.MealGroupRepository;

import java.util.List;

@Controller
public class MealGroupController {

    @Autowired
    private MealGroupRepository mealGroupRepository;

    // Hiển thị danh sách nhóm món ăn
    @GetMapping("/admin/meal-groups")
    public String listMealGroups(Model model) {
        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
        model.addAttribute("mealGroups", mealGroups);
        return "admin/meal/meal-groups";
    }

    // Hiển thị form để tạo mới nhóm món ăn
    @GetMapping("/admin/meal-groups/create")
    public String showCreateForm(Model model) {
        MealGroup mealGroup = new MealGroup();
        model.addAttribute("mealGroup", mealGroup);
        return "admin/meal/create-meal-group";
    }

    // Xử lý form để tạo mới nhóm món ăn
    @PostMapping("/admin/meal-groups/save")
    public String saveMealGroup(@ModelAttribute("mealGroup") MealGroup mealGroup) {
        mealGroupRepository.addMealGroup(mealGroup);
        return "redirect:/admin/meal-groups"; // Sau khi lưu, chuyển về trang danh sách nhóm món ăn
    }
}
