package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shop.model.News;
import shop.repository.NewsRepository;
import shop.utils.FileUtility;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/news")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    // Hiển thị tất cả tin tức
    @GetMapping
    public String listNews(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "search", required = false, defaultValue = "") String search) {
    	
        int pageSize = 5;     
        int totalNews = newsRepository.countNews(search);
        int totalPages = (int) Math.ceil((double) totalNews / pageSize);
        
        List<News> newsList = newsRepository.findAllNews(page, search);
        model.addAttribute("newsList", newsList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);

        return "admin/news/news-list";
    }


    // Hiển thị form tạo tin tức mới
    @GetMapping("/create")
    public String showCreateNewsForm(Model model) {
        model.addAttribute("news", new News());
        return "admin/news/news-create";
    }

    // Xử lý yêu cầu tạo tin tức mới
    @PostMapping("/create")
    public String createNews(@ModelAttribute News news, 
                             @RequestParam("thumbnailFile") MultipartFile thumbnailFile) {
        // Xử lý upload ảnh
        String thumbnailFileName = FileUtility.uploadFileImage(thumbnailFile, "uploads/news");  // Lưu trong thư mục uploads/news
        if (thumbnailFileName.isEmpty()) {
            // Xử lý khi không có ảnh hoặc lỗi khi tải ảnh lên
            return "admin/news/news-create";
        }
        
        news.setThumbnail(thumbnailFileName);

        // Đặt ngày tạo và ngày cập nhật
        news.setCreatedAt(Date.valueOf(LocalDate.now()));
        news.setUpdatedAt(Date.valueOf(LocalDate.now()));

        // Lưu tin tức vào CSDL
        newsRepository.addNews(news);

        return "redirect:/admin/news";
    }

    // Hiển thị form chỉnh sửa tin tức
    @GetMapping("/edit/{id}")
    public String showEditNewsForm(@PathVariable("id") int id, Model model) {
        News news = newsRepository.findById(id);
        model.addAttribute("news", news);
        return "admin/news/news-edit";
    }

    // Xử lý yêu cầu cập nhật tin tức
    @PostMapping("/edit")
    public String editNews(@ModelAttribute News news, 
                           @RequestParam("thumbnailFile") MultipartFile thumbnailFile) {
        // Lấy tin tức hiện tại từ CSDL
        News currentNews = newsRepository.findById(news.getId());

        // Kiểm tra nếu người dùng không tải lên hình ảnh mới
        if (!thumbnailFile.isEmpty()) {
            // Xử lý upload ảnh nếu có tệp mới
            String thumbnailFileName = FileUtility.uploadFileImage(thumbnailFile, "uploads/news");  // Lưu trong thư mục uploads/news
            if (!thumbnailFileName.isEmpty()) {
                news.setThumbnail(thumbnailFileName);  // Sử dụng tên tệp mới
            } else {
                // Xử lý khi lỗi tải ảnh
                return "admin/news/news-edit";
            }
        } else {
            // Nếu không có ảnh mới, giữ lại ảnh hiện tại
            news.setThumbnail(currentNews.getThumbnail());
        }

        // Cập nhật ngày sửa
        news.setUpdatedAt(Date.valueOf(LocalDate.now()));

        // Cập nhật tin tức vào CSDL
        newsRepository.updateNews(news);

        return "redirect:/admin/news";
    }


    // Xóa tin tức
    @GetMapping("/delete/{id}")
    public String deleteNews(@PathVariable("id") int id) {
        newsRepository.deleteNews(id);
        return "redirect:/admin/news";
    }

    // Xem chi tiết tin tức
    @GetMapping("/{id}")
    public String viewNewsDetails(@PathVariable("id") int id, Model model) {
        News news = newsRepository.findById(id);
        model.addAttribute("news", news);
        return "admin/news/news-details";
    }
}
