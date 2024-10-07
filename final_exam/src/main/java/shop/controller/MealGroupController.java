package shop.controller;

import shop.model.MealGroup;
import shop.repository.MealGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/meal-groups")
public class MealGroupController {
    @Autowired
    private MealGroupRepository mealGroupRepository;

    @GetMapping
    public String getAllMealGroups(Model model) {
        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
        model.addAttribute("mealGroups", mealGroups);
        return "admin/meal-groups"; // Trả về view hiển thị danh sách nhóm món ăn
    }

    @GetMapping("/add")
    public String showAddMealGroupForm() {
        return "admin/add-meal-group"; // Trả về view thêm nhóm món ăn
    }

    @PostMapping("/add")
    public String addMealGroup(@ModelAttribute MealGroup mealGroup) {
        mealGroupRepository.addMealGroup(mealGroup);
        return "redirect:/admin/meal-groups"; // Quay lại danh sách nhóm món ăn
    }

    @GetMapping("/edit/{id}")
    public String showEditMealGroupForm(@PathVariable("id") int id, Model model) {
        MealGroup mealGroup = mealGroupRepository.getMealGroupById(id);
        model.addAttribute("mealGroup", mealGroup);
        return "admin/edit-meal-group"; // Trả về view chỉnh sửa nhóm món ăn
    }

    @PostMapping("/edit/{id}")
    public String updateMealGroup(@PathVariable("id") int id, @ModelAttribute MealGroup mealGroup) {
        mealGroup.setId(id);
        mealGroupRepository.updateMealGroup(mealGroup);
        return "redirect:/admin/meal-groups"; // Quay lại danh sách nhóm món ăn
    }

    @GetMapping("/delete/{id}")
    public String deleteMealGroup(@PathVariable("id") int id) {
        mealGroupRepository.deleteMealGroup(id);
        return "redirect:/admin/meal-groups"; // Quay lại danh sách nhóm món ăn
    }
}
