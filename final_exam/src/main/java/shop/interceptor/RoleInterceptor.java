package shop.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        String requestURI = request.getRequestURI();

        // Kiểm tra nếu người dùng chưa đăng nhập
        if (session == null || session.getAttribute("admin") == null) {
            // Nếu chưa đăng nhập và truy cập vào bất kỳ trang /admin nào
            if (requestURI.startsWith("/admin")) {
                response.sendRedirect("/admin/login"); // Chuyển hướng về trang login
                return false; // Ngừng xử lý và chuyển hướng
            }
            return true; // Cho phép truy cập các trang khác
        }

        // Người dùng đã đăng nhập, lấy userType từ session
        Integer userType = (Integer) session.getAttribute("usr_type");

        // Kiểm tra nếu userType là 0 (không phải admin)
        if (userType == 0) {
            // Nếu userType là 0 (người dùng thường), không cho phép truy cập vào các trang admin
            response.sendRedirect("/user"); // Chuyển hướng về trang người dùng
            return false;
        }

        // Kiểm tra nếu userType là 1 (admin)
        if (userType == 1) {
            // Cho phép truy cập trang admin
            if (requestURI.startsWith("/admin")) {
                return true; // Cho phép truy cập vào trang admin
            } else {
                response.sendRedirect("/admin"); // Nếu là admin nhưng cố truy cập các trang không phải admin, chuyển hướng về trang admin
                return false;
            }
        }

        // Mặc định: Chặn truy cập
        response.sendRedirect("/user"); // Chuyển hướng đến trang người dùng nếu không phải admin
        return false;
    }
}
