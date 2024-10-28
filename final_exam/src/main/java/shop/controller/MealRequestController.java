package shop.controller;

import shop.model.Meal;
import shop.model.MealRequest;
import shop.model.MealRequestDetail;
import shop.repository.ContractRepository;
import shop.repository.MealRepository;
import shop.repository.MealRequestDetailRepository;
import shop.repository.MealRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/mealRequest")
public class MealRequestController {

    @Autowired
    private MealRequestRepository mealRequestRepository;
    
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    private MealRequestDetailRepository mealRequestDetailRepository;
    
    @Autowired
    private MealRepository mealRepository;

    @GetMapping("/create")
    public String showCreateForm(@RequestParam("contractId") int contractId, Model model) {
        MealRequest mealRequest = new MealRequest();
        mealRequest.setContractId(contractId);
        model.addAttribute("mealRequest", mealRequest);
        
        // Lấy danh sách món ăn từ hợp đồng
        List<Meal> meals = contractRepository.findMealsByContractId(contractId);
        model.addAttribute("meals", meals);
        
        return "mealRequest/create"; // Trả về giao diện tạo yêu cầu suất ăn
    }

    @PostMapping("/create")
    public String createMealRequest(@ModelAttribute MealRequest mealRequest, 
                                     @RequestParam List<Integer> mealIds, 
                                     @RequestParam List<Integer> quantities) {
        // Thiết lập giá trị mặc định cho requestDate nếu chưa có
        if (mealRequest.getRequestDate() == null) {
            mealRequest.setRequestDate(LocalDate.now());
        }
        
        // Thiết lập giá trị cho deliveryDate nếu chưa có
        if (mealRequest.getDeliveryDate() == null) {
            mealRequest.setDeliveryDate(mealRequest.getRequestDate().plusDays(1)); // Ví dụ: ngày giao hàng là ngày sau
        }

        // Lưu yêu cầu suất ăn vào cơ sở dữ liệu và lấy ID của yêu cầu vừa được tạo
        int mealRequestId = mealRequestRepository.addMealRequest(mealRequest); // Cập nhật để lưu ID

        int totalMeals = 0; // Khởi tạo biến tổng số suất ăn

        // Thêm chi tiết món ăn vào yêu cầu suất ăn
        for (int i = 0; i < mealIds.size(); i++) {
            MealRequestDetail detail = new MealRequestDetail();
            detail.setMealId(mealIds.get(i));
            detail.setQuantity(quantities.get(i));
            detail.setMealRequestId(mealRequestId); // Sử dụng ID từ yêu cầu vừa tạo

            // Lấy giá món ăn từ cơ sở dữ liệu
            Double price = mealRepository.getMealPriceById(mealIds.get(i));
            detail.setPrice(price); // Lưu giá vào detail

            // Cộng dồn số lượng món ăn vào totalMeals
            totalMeals += quantities.get(i);

            // Lưu chi tiết món ăn
            mealRequestDetailRepository.addMealRequestDetail(detail); // Thêm dòng này để lưu chi tiết vào cơ sở dữ liệu
        }

        // Cập nhật trường total_meals trong yêu cầu suất ăn
        mealRequest.setTotalMeals(totalMeals);
        mealRequestRepository.updateTotalMeals(mealRequestId, totalMeals); // Bạn cần tạo phương thức này trong repository để cập nhật tổng suất ăn

        return "redirect:/mealRequest/list"; // Chuyển hướng đến danh sách yêu cầu sau khi lưu
    }



    @GetMapping("/list")
    public String listMealRequests(Model model) {
        model.addAttribute("mealRequests", mealRequestRepository.getAllMealRequests());
        return "mealRequest/list"; // Tên view (HTML) để hiển thị danh sách yêu cầu
    }
}
