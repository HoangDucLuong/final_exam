package shop.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String getMealRequestDetail(@RequestParam("id") int id, Model model) {
        // Tìm kiếm mealRequest dựa trên id
        MealRequest mealRequest = mealRequestRepository.findById(id);
        
        // Kiểm tra nếu mealRequest không tồn tại
        if (mealRequest == null) {
            // Chuyển hướng đến trang thông báo lỗi
            return "error/404"; // Hoặc bạn có thể dùng một trang thông báo không tìm thấy khác
        }
        
        Contract contract = contractRepository.getContractById(mealRequest.getContractId()); // Lấy hợp đồng

        // Kiểm tra xem hợp đồng có tồn tại không
        if (contract != null) {
            mealRequest.setContract(contract); // Thiết lập hợp đồng cho MealRequest
        }

        // Lấy các chi tiết của mealRequest
        List<MealRequestDetail> mealRequestDetails = mealRequestDetailRepository.getDetailsByMealRequestId(id);
        model.addAttribute("mealRequest", mealRequest);
        model.addAttribute("mealRequestDetails", mealRequestDetails);
        List<Meal> meals = mealRepository.getAllMeals(); // Giả sử bạn có phương thức này
        model.addAttribute("meals", meals);
        
        return "admin/meal-request-detail"; // Trả về trang detail.html
    }




    // Trang đăng nhập
    @GetMapping("/login")
    public String login() {
        return "admin/login"; // Trả về trang đăng nhập admin
    }

    // Quản lý người dùng
    @GetMapping("/user-management") 
    public String userManagement() {
        return "admin/user-management"; // Trả về trang quản lý người dùng
    }

    // Kiểm tra đăng nhập
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
