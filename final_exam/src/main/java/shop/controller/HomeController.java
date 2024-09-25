package shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/")
    public ModelAndView showHomePage() {
        return new ModelAndView("home/index");
    }
    @GetMapping("/about")
    public ModelAndView showAboutPage() {
        return new ModelAndView("home/about");
    }
    @GetMapping("/booking")
    public ModelAndView showBookingPage() {
        return new ModelAndView("home/booking");
    }
    @GetMapping("/contact")
    public ModelAndView showContactPage() {
        return new ModelAndView("home/contact");
    }
    @GetMapping("/menu")
    public ModelAndView showMenuPage() {
        return new ModelAndView("home/menu");
    }
    @GetMapping("/service")
    public ModelAndView showServicePage() {
        return new ModelAndView("home/service");
    }
    @GetMapping("/team")
    public ModelAndView showTeamPage() {
        return new ModelAndView("home/team");
    }
    @GetMapping("/testimonial")
    public ModelAndView showTestimonialPage() {
        return new ModelAndView("home/testimonial");
    }
}
