package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import shop.model.Contract;
import shop.model.Invoice;
import shop.model.User;
import shop.repository.ContractRepository;
import shop.repository.InvoiceRepository;
import shop.repository.UserRepository;
import shop.service.MailService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/invoices")
public class InvoiceAdminController {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MailService mailService;

    /**
     * Hiển thị danh sách tất cả hóa đơn.
     */
    @GetMapping
    public String listAllInvoices(
            @RequestParam(required = false, defaultValue = "false") boolean unpaid,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {
        List<Invoice> invoices;

        if (unpaid) {
            invoices = invoiceRepository.findByPaymentStatus(0);
        } else {
            invoices = invoiceRepository.findAll();
        }

        if (year != null || month != null) {
            invoices = invoices.stream()
                    .filter(invoice -> {
                        boolean match = true;
                        if (year != null) {
                            match &= invoice.getDueDate().getYear() == year;
                        }
                        if (month != null) {
                            match &= invoice.getDueDate().getMonthValue() == month;
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
        }

        model.addAttribute("invoices", invoices);
        model.addAttribute("unpaid", unpaid);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        return "admin/invoice/invoice_list";
    }


    /**
     * Hiển thị danh sách hóa đơn theo contractId.
     */
    @GetMapping("/contract/{contractId}")
    public String listInvoicesByContractId(@PathVariable int contractId, Model model) {
        // Lấy danh sách hóa đơn theo contractId
        List<Invoice> invoices = invoiceRepository.findByContractId(contractId);

        // Lấy thông tin hợp đồng
        Contract contract = contractRepository.findById(contractId);

        // Kiểm tra nếu hợp đồng tồn tại, lấy thông tin người dùng
        String userName = "Không xác định"; // Tên mặc định nếu không tìm thấy
        if (contract != null) {
            User user = userRepository.findById(contract.getUsrId());
            if (user != null) {
                userName = user.getName();
            }
        }

        // Thêm danh sách hóa đơn và thông tin hợp đồng vào model
        model.addAttribute("invoices", invoices);
        model.addAttribute("userName", userName); // Tên người dùng
        model.addAttribute("contract", contract); // Thông tin hợp đồng

        return "admin/invoice/invoice"; // Trả về template admin/invoice/invoice
    }

    /**
     * Hiển thị chi tiết hóa đơn theo ID.
     */
    @GetMapping("/{id}")
    public String getInvoiceById(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepository.findById(id);
        model.addAttribute("invoice", invoice);
        return "admin/invoice/invoice_detail";
    }

    /**
     * Hiển thị danh sách hóa đơn theo userId.
     */
    @GetMapping("/user/{userId}")
    public String listInvoicesByUserId(@PathVariable int userId, Model model) {
        List<Invoice> invoices = invoiceRepository.findByUserId(userId);
        model.addAttribute("invoices", invoices);
        return "admin/invoice/invoice";
    }

    /**
     * Hiển thị danh sách hóa đơn theo contractId và khoảng ngày đến hạn.
     */
    @GetMapping("/contract/{contractId}/due-date-range")
    public String listInvoicesByContractAndDateRange(
            @PathVariable int contractId,
            @RequestParam Timestamp startDate,
            @RequestParam Timestamp endDate,
            Model model
    ) {
        List<Invoice> invoices = invoiceRepository.findByContractIdAndDueDateBetween(contractId, startDate, endDate);
        model.addAttribute("invoices", invoices);
        return "admin/invoice/invoice";
    }
    
    @GetMapping("/contract/{contractId}/grouped-by-month")
    public String getInvoicesGroupedByContractAndMonth(
            @PathVariable int contractId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model
    ) {
        // Lấy danh sách hóa đơn theo contractId
        List<Invoice> invoices = invoiceRepository.findByContractId(contractId);

        // Nhóm hóa đơn theo tháng và áp dụng bộ lọc năm/tháng nếu cần
        Map<String, Map<String, Object>> groupedInvoices = invoices.stream()
                .filter(invoice -> {
                    if (year != null && month != null) {
                        return invoice.getDueDate().getYear() == year &&
                               invoice.getDueDate().getMonthValue() == month;
                    } else if (year != null) {
                        return invoice.getDueDate().getYear() == year;
                    }
                    return true; // Nếu không lọc, lấy tất cả
                })
                .collect(Collectors.groupingBy(
                        invoice -> invoice.getDueDate().getYear() + "-" + String.format("%02d", invoice.getDueDate().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            BigDecimal totalUnpaid = list.stream()
                                    .filter(i -> i.getPaymentStatus() == 0)
                                    .map(i -> i.getTotalAmount().subtract(i.getPaidAmount()))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            Map<String, Object> data = new HashMap<>();
                            data.put("invoices", list);
                            data.put("unpaidAmount", totalUnpaid);
                            return data;
                        })
                ));

        // Gửi danh sách đã nhóm vào model
        model.addAttribute("groupedInvoices", groupedInvoices);

        // Gửi thêm thông tin contractId để hiển thị và lọc
        model.addAttribute("contractId", contractId);

        return "admin/invoice/invoice"; // Tên template view
    }
    @PostMapping("/send")
    public String sendInvoiceNotification(
            @RequestParam("invoiceId") int invoiceId,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        Invoice invoice = invoiceRepository.findById(invoiceId);
        if (invoice == null) {
            System.err.println("Không tìm thấy hóa đơn với ID: " + invoiceId);
            redirectAttributes.addFlashAttribute("error", "Hóa đơn không tồn tại!");
            return referer != null ? "redirect:" + referer : "redirect:/admin/invoices";
        }

        String recipientEmail = userRepository.findEmailByContractId(invoice.getContractId());
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            System.err.println("Không tìm thấy email người nhận cho contractId: " + invoice.getContractId());
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy email người nhận!");
            return referer != null ? "redirect:" + referer : "redirect:/admin/invoices";
        }

        if (!invoice.isSent()) {
            boolean sent = mailService.sendInvoiceNotification(recipientEmail, invoice);
            if (sent) {
                invoice.setSent(true);
                invoiceRepository.save(invoice);
                redirectAttributes.addFlashAttribute("message", "Thông báo hóa đơn đã được gửi thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể gửi thông báo hóa đơn. Vui lòng kiểm tra cấu hình email.");
            }
        } else {
            redirectAttributes.addFlashAttribute("warning", "Thông báo hóa đơn đã được gửi trước đó!");
        }

        return referer != null ? "redirect:" + referer : "redirect:/admin/invoices";
    }
    @PostMapping("/send-by-month")
    public String sendInvoiceNotificationsByMonth(
            @RequestParam("contractId") int contractId,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        List<Invoice> invoices = invoiceRepository.findByContractId(contractId).stream()
                .filter(invoice -> invoice.getDueDate().getYear() == year &&
                                   invoice.getDueDate().getMonthValue() == month &&
                                   invoice.getPaymentStatus() == 0) // Chỉ lấy hóa đơn chưa thanh toán
                .collect(Collectors.toList());

        if (invoices.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không có hóa đơn nào chưa thanh toán trong tháng được chọn.");
            return referer != null ? "redirect:" + referer : "redirect:/admin/invoices";
        }

        // Tính tổng số tiền chưa thanh toán
        BigDecimal totalUnpaid = invoices.stream()
                .map(i -> i.getTotalAmount().subtract(i.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy email người nhận (giả định hóa đơn thuộc cùng một người dùng)
        String recipientEmail = userRepository.findEmailByContractId(contractId);

        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            // Gửi email tổng số tiền chưa thanh toán
            boolean sent = mailService.sendGroupedInvoiceNotification(recipientEmail, totalUnpaid);
            if (sent) {
                invoices.forEach(invoice -> {
                    invoice.setSent(true); // Đánh dấu hóa đơn đã gửi
                    invoiceRepository.save(invoice);
                });
                redirectAttributes.addFlashAttribute("message",
                        String.format("Đã gửi thông báo tổng nợ tháng %d-%02d (Tổng nợ: %.2f).", year, month, totalUnpaid));
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể gửi thông báo tổng nợ. Vui lòng kiểm tra cấu hình email.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy email người nhận!");
        }

        return referer != null ? "redirect:" + referer : "redirect:/admin/invoices";
    }

}