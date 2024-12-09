package shop.controller;

import shop.model.Contract;
import shop.model.Invoice;
import shop.model.Menu; // Đổi từ Meal thành Menu
import shop.model.MealRequest;
import shop.model.MealRequestDetail;
import shop.model.User;
import shop.repository.ContractRepository;
import shop.repository.InvoiceRepository;
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
import java.time.LocalDateTime;
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
    @Autowired
    private InvoiceRepository invoiceRepository;

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
                                     @RequestParam List<Integer> menuIds, 
                                     @RequestParam List<Integer> quantities) {
        if (mealRequest.getRequestDate() == null) {
            mealRequest.setRequestDate(LocalDate.now());
        }
        if (mealRequest.getDeliveryDate() == null) {
            mealRequest.setDeliveryDate(mealRequest.getRequestDate().plusDays(1));
        }

        // Lưu yêu cầu suất ăn vào cơ sở dữ liệu và lấy ID của yêu cầu vừa được tạo
        int mealRequestId = mealRequestRepository.addMealRequest(mealRequest);

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalMeals = 0;

        // Lưu chi tiết của yêu cầu suất ăn
        for (int i = 0; i < menuIds.size(); i++) {
            MealRequestDetail detail = new MealRequestDetail();
            detail.setMenuId(menuIds.get(i));
            detail.setQuantity(quantities.get(i));
            detail.setMealRequestId(mealRequestId);

            BigDecimal price = menuRepository.getMenuPriceById(menuIds.get(i));
            detail.setPrice(price);
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantities.get(i))));
            totalMeals += quantities.get(i);

            mealRequestDetailRepository.addMealRequestDetail(detail);
        }

        // Cập nhật tổng số suất ăn của MealRequest
        mealRequest.setTotalMeals(totalMeals);
        mealRequestRepository.updateTotalMeals(mealRequestId, totalMeals);

     // Tạo và lưu một hóa đơn mới
        Invoice invoice = new Invoice();
        invoice.setContractId(mealRequest.getContractId());
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO); // Đặt giá trị thanh toán ban đầu là 0

        // Tính toán ngày thanh toán là ngày 5 của tháng sau ngày giao hàng
        LocalDate deliveryDate = mealRequest.getDeliveryDate();
        LocalDateTime dueDate = deliveryDate.plusMonths(1).withDayOfMonth(5).atStartOfDay();

        invoice.setDueDate(dueDate); // Đặt ngày thanh toán
        invoice.setPaymentStatus(0); // 0 nghĩa là chưa thanh toán
        invoice.setCreatedAt(LocalDateTime.now());
        // Lưu hóa đơn vào cơ sở dữ liệu
        invoiceRepository.save(invoice);


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
    @GetMapping("/detail")
    public String showMealRequestDetail(@RequestParam("id") int id, Model model) {
        MealRequest mealRequest = mealRequestRepository.getMealRequestById(id);
        List<MealRequestDetail> mealRequestDetails = mealRequestDetailRepository.getDetailsByMealRequestId(id);

        model.addAttribute("mealRequest", mealRequest);
        model.addAttribute("mealRequestDetails", mealRequestDetails);
        
        return "mealRequest/detail"; // Trả về giao diện chi tiết yêu cầu
    }

}
