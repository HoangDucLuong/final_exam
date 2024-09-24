package shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Hiển thị trang chính
    @GetMapping("/")
    public String home() {
        return "home/index"; // Đường dẫn đến template trang chính
    }
    @GetMapping("/")
    public String about() {
        return "home/index"; // Đường dẫn đến template trang chính
    }
}
