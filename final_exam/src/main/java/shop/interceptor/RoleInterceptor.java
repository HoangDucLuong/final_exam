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
        if (session == null || session.getAttribute("user") == null) {
            // Người dùng chưa đăng nhập, chỉ cho phép truy cập vào trang user (client)
            if (requestURI.equals("/user")) {
                return true; // Cho phép truy cập
            } else {
                response.sendRedirect("/user"); // Chuyển hướng đến trang user (client)
                return false; // Chặn truy cập các trang khác
            }
        }

        // Người dùng đã đăng nhập, lấy userType từ session
        Integer userType = (Integer) session.getAttribute("userType");

        // Kiểm tra quyền truy cập dựa trên userType
        if (userType == 1) {
            // userType 1: Admin, cho phép truy cập trang admin
            if (requestURI.startsWith("/admin")) {
                return true; // Cho phép truy cập trang admin
            } else {
                response.sendRedirect("/admin"); // Chuyển hướng đến trang admin
                return false; // Chặn truy cập các trang khác
            }
        } else if (userType == 0) {
            // userType 0: User, cho phép truy cập trang user (client)
            if (requestURI.startsWith("/user")) {
                return true; // Cho phép truy cập trang user (client)
            } else {
                response.sendRedirect("/user"); // Chuyển hướng đến trang user (client)
                return false; // Chặn truy cập các trang khác
            }
        }

        // Mặc định: Chặn truy cập
        response.sendRedirect("/user"); // Chuyển hướng đến trang user (client)
        return false;
    }
}
