package shop.controller;

import shop.model.Contract;
import shop.model.Menu; // Đổi từ Meal thành Menu
import shop.model.MealRequest;
import shop.model.MealRequestDetail;
import shop.model.User;
import shop.repository.ContractRepository;
import shop.repository.MenuRepository; // Thay đổi repository
import shop.repository.MealRequestDetailRepository;
import shop.repository.MealRequestRepository;
import shop.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private MenuRepository menuRepository; // Sử dụng MenuRepository thay vì MealRepository
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/create")
    public String showCreateForm(@RequestParam("contractId") int contractId, Model model) {
        MealRequest mealRequest = new MealRequest();
        mealRequest.setContractId(contractId);
        model.addAttribute("mealRequest", mealRequest);
        
        // Lấy danh sách menu từ hợp đồng
        List<Menu> menus = contractRepository.findMenusByContractId(contractId); // Đảm bảo phương thức này tồn tại
        model.addAttribute("menus", menus);
        
        return "mealRequest/create"; // Trả về giao diện tạo yêu cầu suất ăn
    }

    @PostMapping("/create")
    public String createMealRequest(@ModelAttribute MealRequest mealRequest, 
                                     @RequestParam List<Integer> menuIds, // Thay đổi để lấy menuIds
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
        for (int i = 0; i < menuIds.size(); i++) {
            MealRequestDetail detail = new MealRequestDetail();
            detail.setMenuId(menuIds.get(i)); // Giả sử bạn có phương thức để lấy mealId từ menuId
            detail.setQuantity(quantities.get(i));
            detail.setMealRequestId(mealRequestId); // Sử dụng ID từ yêu cầu vừa tạo

            // Lấy giá món ăn từ cơ sở dữ liệu
            BigDecimal price = menuRepository.getMenuPriceById(menuIds.get(i)); // Cần thêm phương thức này trong MenuRepository
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
    public String listMealRequests(
            @RequestParam(value = "contractId", required = false) Integer contractId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model, HttpServletRequest request) {

        // Lấy email từ session
        String email = (String) request.getSession().getAttribute("user");
        
        if (email != null) {
            Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Lấy danh sách hợp đồng của user để hiển thị trong dropdown
                List<Contract> contracts = contractRepository.getContractsByUserId(user.getId());
                model.addAttribute("contracts", contracts);

                // Lấy danh sách yêu cầu suất ăn và tổng số yêu cầu dựa trên hợp đồng (nếu có)
                List<MealRequest> mealRequests;
                int totalRequests;
                int pageSize = 5; // Số lượng kết quả trên mỗi trang
                
                if (contractId != null) {
                    mealRequests = mealRequestRepository.getMealRequestsByContractId(contractId, page, pageSize);
                    totalRequests = mealRequestRepository.countMealRequestsByContractId(contractId);
                } else {
                    mealRequests = mealRequestRepository.getMealRequestsByUserId(user.getId(), page, pageSize);
                    totalRequests = mealRequestRepository.countMealRequestsByUserId(user.getId());
                }

                Map<Integer, List<MealRequestDetail>> mealRequestDetailsMap = new HashMap<>();
                for (MealRequest mealRequest : mealRequests) {
                    List<MealRequestDetail> details = mealRequestDetailRepository.getDetailsByMealRequestId(mealRequest.getId());
                    mealRequestDetailsMap.put(mealRequest.getId(), details);
                }

                // Tính tổng số trang
                int totalPages = (totalRequests + pageSize - 1) / pageSize;
                
                // Thêm các thuộc tính vào model
                model.addAttribute("mealRequestDetailsMap", mealRequestDetailsMap);
                model.addAttribute("mealRequests", mealRequests);
                model.addAttribute("totalPages", totalPages);
                model.addAttribute("currentPage", page);
                model.addAttribute("pageSize", pageSize);
                
                // Trả về giao diện danh sách yêu cầu suất ăn
                return "mealRequest/list";
            }
        }
        // Chuyển hướng về trang đăng nhập nếu không tìm thấy user
        return "redirect:/user/login";
    }
}
