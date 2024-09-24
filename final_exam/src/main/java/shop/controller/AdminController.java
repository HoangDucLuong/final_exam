package shop.controller;

import shop.model.User;
import shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    // Display the admin dashboard
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/ad_index";
    }

    // Display the login form
    @GetMapping("/login")
    public String loginForm() {
        return "admin/ad_login";
    }

    // Handle login using UID and password
    @PostMapping("/chklogin")
    public String checkLogin(@RequestParam("_uid") String username,
                             @RequestParam("_pwd") String password, 
                             HttpServletRequest request, 
                             HttpServletResponse response) {
        User user = userService.findByUid(username);

        if (user != null) {
           
            if (userService.checkPassword(password, user.getPassword())) {
            	HttpSession session = request.getSession();
                session.setAttribute("user", user);

                logger.info("User authenticated: " + username + ", UserType: " + user.getUserType());

                if (user.getUserType() == 1) {
                    return "redirect:/admin/dashboard";
                } else if (user.getUserType() == 1) {
                    return "redirect:/manager/login";
                } else if (user.getUserType() == 0) {
                    return "redirect:/employee/login";
                    }
            } else {
                logger.error("Password check failed for user: " + username);
            }
        } else {
            logger.error("User not found: " + username);
        }
        return "redirect:/admin/login?error=true";
    
}

}
