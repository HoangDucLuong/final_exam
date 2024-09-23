package shop.interceptor;

import shop.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
	    HttpSession session = request.getSession(false);
	    User user = (session != null) ? (User) session.getAttribute("user") : null;

	    String requestURI = request.getRequestURI();

	    // Kiểm tra nếu đang truy cập vào admin
	    if (requestURI.startsWith("/admin")) {
	        if (user == null) {
	            // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	            response.sendRedirect("/admin/login");
	            return false;
	        }
	    }

	    return true; // Cho phép truy cập nếu là user hoặc đang ở trang khác
	}


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Xử lý sau khi controller xử lý nhưng trước khi view được render (tùy chọn)
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Xử lý sau khi view được render (tùy chọn)
    }
}
