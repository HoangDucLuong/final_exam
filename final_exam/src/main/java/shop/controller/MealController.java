package shop.controller;

import shop.model.Meal;
import shop.model.MealGroup;
import shop.repository.MealRepository;
import shop.repository.MealGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/meals")
public class MealController {
    @Autowired
    private MealRepository mealRepository;
    @Autowired
    private MealGroupRepository mealGroupRepository;

    @GetMapping
    public String getAllMeals(Model model) {
        List<Meal> meals = mealRepository.getAllMeals();
        model.addAttribute("meals", meals);
        return "admin/meals"; // Trả về view hiển thị danh sách món ăn
    }

    @GetMapping("/add")
    public String showAddMealForm(Model model) {
        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
        model.addAttribute("mealGroups", mealGroups);
        return "admin/add-meal"; // Trả về view thêm món ăn
    }

    @PostMapping("/add")
    public String addMeal(@ModelAttribute Meal meal) {
        mealRepository.addMeal(meal);
        return "redirect:/admin/meals"; // Quay lại danh sách món ăn
    }

    @GetMapping("/edit/{id}")
    public String showEditMealForm(@PathVariable("id") int id, Model model) {
        Meal meal = mealRepository.getMealById(id);
        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
        model.addAttribute("meal", meal);
        model.addAttribute("mealGroups", mealGroups);
        return "admin/edit-meal"; // Trả về view chỉnh sửa món ăn
    }

    @PostMapping("/edit/{id}")
    public String updateMeal(@PathVariable("id") int id, @ModelAttribute Meal meal) {
        meal.setId(id);
        mealRepository.updateMeal(meal);
        return "redirect:/admin/meals"; // Quay lại danh sách món ăn
    }

    @GetMapping("/delete/{id}")
    public String deleteMeal(@PathVariable("id") int id) {
        mealRepository.deleteMeal(id);
        return "redirect:/admin/meals"; // Quay lại danh sách món ăn
    }
    
}
