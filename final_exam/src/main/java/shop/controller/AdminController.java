package shop.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.Contract;
import shop.model.Meal;
import shop.model.MealRequest;
import shop.model.MealRequestDetail;
import shop.model.User;
import shop.repository.AuthRepository;
import shop.repository.ContractRepository;
import shop.repository.MealRepository;
import shop.repository.MealRequestDetailRepository;
import shop.repository.MealRequestRepository;
import shop.repository.UserRepository; 
import shop.utils.SecurityUtility;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AuthRepository repAuth;
    @Autowired
    UserRepository repUser; 
    @Autowired
    private MealRequestRepository mealRequestRepository;
    @Autowired
    private ContractRepository contractRepository; // Repository cho Contract
    
    @Autowired
    private MealRequestDetailRepository mealRequestDetailRepository;
    @Autowired
    private MealRepository mealRepository;

    // Trang chính của admin
    @GetMapping("/index")
    public String viewMealRequests(Model model) {
        // Lấy danh sách meal request từ repository
        List<MealRequest> mealRequests = mealRequestRepository.getAllMealRequests();

        // Lặp qua từng MealRequest để thiết lập thông tin người dùng và hợp đồng
        for (MealRequest mealRequest : mealRequests) {
            List<MealRequestDetail> details = mealRequestDetailRepository.getDetailsByMealRequestId(mealRequest.getId());
            mealRequest.setMealRequestDetails(details);
            Integer userId = mealRequest.getUserId(); // Lấy userId từ mealRequest
            if (userId != null) { // Kiểm tra userId không null
                User user = repUser.findById(userId); // Lấy người dùng từ repository	
                mealRequest.setUser(user); // Thiết lập thông tin người dùng cho MealRequest
            }
            Contract contract = contractRepository.getContractById(mealRequest.getContractId()); // Lấy hợp đồng

            // Kiểm tra xem hợp đồng có tồn tại không
            if (contract != null) {
                mealRequest.setContract(contract); // Thiết lập hợp đồng cho MealRequest
            }

            // Thiết lập trạng thái dưới dạng chữ
            switch (mealRequest.getStatus()) {
                case 0:
                    mealRequest.setStatusText("Pending");
                    break;
                case 1:
                    mealRequest.setStatusText("Confirmed");
                    break;
                case 2:
                    mealRequest.setStatusText("Delivered");
                    break;
                default:
                    mealRequest.setStatusText("Unknown");
            }
        }

        model.addAttribute("mealRequests", mealRequests);
        return "admin/index"; // Trả về trang admin chính
    }
    @GetMapping("/meal-request/detail")
    public String getMealRequestDetail(@RequestParam("id") int id, @RequestParam("userId") int userId, Model model) {
        MealRequest mealRequest = mealRequestRepository.findById(id);

        if (mealRequest == null) {
            return "error/404";
        }

        // Lấy thông tin User dựa trên userId
        if (userId != 0) {
            User user = repUser.findById(userId);
            mealRequest.setUser(user); // Gắn User vào MealRequest
        }

        // Lấy thông tin Contract
        Contract contract = contractRepository.getContractById(mealRequest.getContractId());
        if (contract != null) {
            mealRequest.setContract(contract);
        }

        // Lấy chi tiết món ăn
        List<MealRequestDetail> mealRequestDetails = mealRequestDetailRepository.getDetailsByMealRequestId(id);
        for (MealRequestDetail detail : mealRequestDetails) {
            Optional<Meal> optionalMeal = mealRepository.findById(detail.getMealId());
            if (optionalMeal.isPresent()) {
                detail.setMealName(optionalMeal.get().getMealName());
            } else {
                detail.setMealName("Unknown Meal");
            }
        }

        // Đưa dữ liệu vào model
        model.addAttribute("mealRequest", mealRequest);
        model.addAttribute("mealRequestDetails", mealRequestDetails);

        return "admin/meal-request-detail"; // Trả về giao diện chi tiết
    }



    @GetMapping("/meal-request/approve/{id}")
    public String approveMealRequest(@PathVariable("id") int id) {
        MealRequest mealRequest = mealRequestRepository.findById(id);
        if (mealRequest != null) {
            mealRequest.setStatus(1); // Cập nhật trạng thái thành Confirmed (1)
            mealRequestRepository.updateMealRequest(mealRequest);
        }
        return "redirect:/admin/index"; // Quay lại danh sách yêu cầu
    }

    // Phương thức từ chối yêu cầu (Reject)
    @GetMapping("/meal-request/reject/{id}")
    public String rejectMealRequest(@PathVariable("id") int id) {
        MealRequest mealRequest = mealRequestRepository.findById(id);
        if (mealRequest != null) {
            mealRequest.setStatus(2); // Cập nhật trạng thái thành Delivered (2)
            mealRequestRepository.updateMealRequest(mealRequest);
        }
        return "redirect:/admin/index"; // Quay lại danh sách yêu cầu
    }

    // Trang đăng nhập
    @GetMapping("/login")
    public String login() {
        return "admin/login"; // Trả về trang đăng nhập admin
    }

    // Quản lý người dùng
    @GetMapping("/admin/user-management") 
    public String usermanagement(){
        return "admin/index";
    }


    @PostMapping("/chklogins")
    public String chkLogins(@RequestParam("email") String email, @RequestParam("pwd") String password,
                            HttpServletRequest request) {
        Logger log = Logger.getGlobal();
        log.info("Attempted login by user: " + email);

        // Lấy mật khẩu đã mã hóa và quyền user từ repository
        String encryptedPassword = repAuth.findPasswordByUid(email);
        Integer role = repAuth.findUserTypeByUid(email);

        // Kiểm tra mật khẩu
        if (encryptedPassword == null || !SecurityUtility.compareBcrypt(encryptedPassword, password)) {
            request.setAttribute("error", "Invalid email or password");
            return "admin/login"; // Trả về trang đăng nhập với thông báo lỗi
        }

        // Kiểm tra quyền người dùng
        if (role == 1) { // Nếu là admin
            request.getSession().setAttribute("usr_type", role);
            return "redirect:/admin/index"; // Điều hướng đến trang chính của admin
        } else {
            request.setAttribute("error", "You do not have permission to access the admin area.");
            return "admin/login"; // Trả về trang đăng nhập với thông báo lỗi
        }
    }

    // Trang đăng ký
    @GetMapping("/register")
    public String register() {
        return "admin/register"; // Trả về trang đăng ký admin
    }

    // Xử lý đăng ký
    @PostMapping("/add")
    public String addUser(@RequestParam String email, 
                          @RequestParam String pwd, 
                          @RequestParam String name,
                          @RequestParam String phone,
                          @RequestParam String address) {
        // Kiểm tra giá trị address
        if (address == null || address.isEmpty()) {
            return "redirect:/admin/register?error=Address is required"; // Thông báo lỗi nếu không có địa chỉ
        }

        // Tạo đối tượng User mới
        User user = new User();
        user.setEmail(email); // Đặt email là username

        // Hash mật khẩu
        String hashedPassword = SecurityUtility.encryptBcrypt(pwd);
        user.setPwd(hashedPassword); // Lưu mật khẩu đã hash

        user.setName(name); // Lưu tên đầy đủ
        user.setCreatedAt(LocalDateTime.now()); // Thời gian tạo tài khoản
        user.setPhone(phone); // Lưu số điện thoại
        user.setAddress(address); // Lưu địa chỉ
        user.setStatus(1); // Đặt trạng thái mặc định là active (1)
        user.setUsrType(0); // Đặt quyền mặc định là user (0)

        // Thêm người dùng vào cơ sở dữ liệu
        repUser.addUser(user);

        // Điều hướng lại trang admin sau khi thêm user thành công
        return "redirect:/admin/index";
    }
}
