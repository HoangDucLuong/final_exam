package shop;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import shop.interceptor.RoleInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final RoleInterceptor roleInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình để phục vụ các tệp từ thư mục uploads/news
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/news/");
    }
    public WebConfig(RoleInterceptor roleInterceptor) {
        this.roleInterceptor = roleInterceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Áp dụng RoleInterceptor cho tất cả các đường dẫn /admin/**
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/admin/**") // Áp dụng cho tất cả các trang /admin
                .excludePathPatterns("/admin/login", "/admin/register", "/admin/chklogins", "/admin/add"); // Bỏ qua các trang đăng nhập, đăng ký
    }
}
