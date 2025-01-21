package shop.controller;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import shop.Config.VNPayService;
import shop.model.Contract;
import shop.model.Invoice;
import shop.model.MealRequest;
import shop.model.MealRequestDetail;
import shop.repository.ContractRepository;
import shop.repository.InvoiceRepository;
import shop.repository.MealRequestDetailRepository;
import shop.repository.MealRequestRepository;
import shop.repository.PaymentRepository;

@Controller
@RequestMapping("/user/invoices")
public class InvoiceUserController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private MealRequestRepository mealRequestRepository;
    
    @Autowired
    private MealRequestDetailRepository mealRequestDetailRepository;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Xem danh sách hóa đơn của hợp đồng theo contractId
     */
    @GetMapping("/{contractId}")
    public String viewInvoiceByContractId(@PathVariable int contractId, Model model) {
        // Lấy hợp đồng theo contractId
        Contract contract = contractRepository.getContractById(contractId);

        if (contract == null) {
            return "redirect:/user/contracts";  // Quay lại danh sách hợp đồng nếu không có hợp đồng
        }

        // Tìm hóa đơn của hợp đồng này với trạng thái sent = true
        List<Invoice> invoices = invoiceRepository.findByContractIdAndSent(contractId, 1);

        // Nhóm hóa đơn theo tháng
        Map<String, List<Invoice>> invoicesByMonth = invoices.stream()
                .collect(Collectors.groupingBy(invoice -> {
                    // Chuyển đổi ngày thanh toán thành tháng, ví dụ: "2024-11"
                    return invoice.getDueDate().toLocalDate().getYear() + "-" + String.format("%02d", invoice.getDueDate().toLocalDate().getMonthValue());
                }));

        // Tính toán tổng số tiền chưa thanh toán cho từng tháng
        Map<String, BigDecimal> unpaidAmountsByMonth = invoicesByMonth.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    entry -> entry.getValue().stream()
                            .filter(invoice -> invoice.getPaymentStatus() == 0)  // Lọc hóa đơn chưa thanh toán
                            .map(Invoice::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)  // Tính tổng số tiền chưa thanh toán
                ));

        // Thêm dữ liệu vào model
        model.addAttribute("invoicesByMonth", invoicesByMonth);
        model.addAttribute("unpaidAmountsByMonth", unpaidAmountsByMonth);

        return "user/invoice";  // Trả về view hiển thị hóa đơn
    }

    /**
     * Tính toán tổng số tiền của hóa đơn dựa trên các yêu cầu suất ăn
     */
    private BigDecimal calculateInvoiceAmount(int contractId) {
        List<MealRequest> mealRequests = mealRequestRepository.findByContractId(contractId);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MealRequest mealRequest : mealRequests) {
            BigDecimal requestTotal = mealRequest.getMealRequestDetails().stream()
                    .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalAmount = totalAmount.add(requestTotal);
        }
        return totalAmount;
    }
    

    /**
     * Tạo hóa đơn cho hợp đồng vào cuối mỗi tháng (trước ngày 5 của tháng tiếp theo)
     */
    private void generateMonthlyInvoice() {
        List<Contract> contracts = contractRepository.findActiveContracts();

        for (Contract contract : contracts) {
            if (shouldGenerateInvoice(contract)) {
                BigDecimal totalAmount = calculateInvoiceAmount(contract.getId());

                Invoice invoice = new Invoice();
                invoice.setContractId(contract.getId());
                invoice.setTotalAmount(totalAmount);
                invoice.setPaidAmount(BigDecimal.ZERO); // Đặt giá trị thanh toán ban đầu là 0
                invoice.setDueDate(getNextPaymentDate()); // Ngày thanh toán là ngày 5 của tháng tiếp theo
                invoice.setPaymentStatus(0); // Chưa thanh toán

                invoiceRepository.save(invoice);
            }
        }
    }

    /**
     * Kiểm tra nếu cần tạo hóa đơn mới cho hợp đồng vào cuối tháng
     */
    private boolean shouldGenerateInvoice(Contract contract) {
        LocalDate today = LocalDate.now();
        return today.getDayOfMonth() == 5 && contract.getStatus() != 3; // Status khác 3 (Completed)
    }

    /**
     * Lấy ngày thanh toán tiếp theo (ngày 5 của tháng tiếp theo)
     */
    private LocalDateTime getNextPaymentDate() {
        LocalDate today = LocalDate.now();
        return today.withDayOfMonth(5).plusMonths(1).atStartOfDay();
    }

    /**
     * Thanh toán tất cả các hóa đơn trong tháng (Cập nhật trạng thái thành đã thanh toán)
     */
    @PostMapping("/{contractId}/pay")
    public String payInvoices(@PathVariable("contractId") int contractId,
                              @RequestParam("month") String month,
                              HttpServletRequest request) {
        // Kiểm tra giá trị contractId
        if (contractId <= 0) {
            throw new IllegalArgumentException("Invalid contractId: " + contractId);
        }

        // Kiểm tra giá trị month
        if (month == null || !month.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Invalid month format. Expected yyyy-MM, but received: " + month);
        }

        System.out.println("Received contractId: " + contractId);
        System.out.println("Received month: " + month);

        // Lấy hợp đồng theo contractId
        Contract contract = contractRepository.getContractById(contractId);
        if (contract == null) {
            return "redirect:/user/contracts"; // Quay lại trang hợp đồng nếu không tìm thấy hợp đồng
        }

        try {
            LocalDateTime monthStart = LocalDate.parse(month + "-01").atStartOfDay();
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusNanos(1);

            // Lấy danh sách hóa đơn trong tháng đó
            List<Invoice> invoices = invoiceRepository.findByContractIdAndDueDateBetween(
                contractId,
                Timestamp.valueOf(monthStart),
                Timestamp.valueOf(monthEnd)
            );

            BigDecimal totalAmountToPay = invoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == 0) // Chỉ thanh toán các hóa đơn chưa thanh toán
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalAmountToPay.compareTo(BigDecimal.ZERO) <= 0) {
                return "redirect:/user/invoices/" + contractId; // Không có hóa đơn cần thanh toán
            }

            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            boolean isDeposit = false; // Đây là thanh toán hóa đơn thông thường
            String vnpayUrl = vnPayService.createOrder(request, totalAmountToPay.intValue(), contractId, isDeposit, baseUrl);


            return "redirect:" + vnpayUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user/invoices/" + contractId; // Chuyển về danh sách hóa đơn nếu có lỗi
        }
    }


    @PostMapping("/paySingle")
    public String paySingleInvoice(@RequestParam("contractId") int contractId, 
                                   @RequestParam("invoiceId") int invoiceId) {
        // Lấy hợp đồng theo contractId
        Contract contract = contractRepository.getContractById(contractId);
        if (contract == null) {
            return "redirect:/user/contracts";  // Quay lại trang hợp đồng nếu không tìm thấy hợp đồng
        }

        // Lấy hóa đơn theo invoiceId
        Invoice invoice = invoiceRepository.findById(invoiceId);
        if (invoice == null || invoice.getPaymentStatus() == 1) {
            // Nếu hóa đơn không tồn tại hoặc đã thanh toán rồi, quay lại trang danh sách hóa đơn
            return "redirect:/user/invoices/" + contractId;
        }

        // Cập nhật trạng thái hóa đơn là đã thanh toán
        invoice.setPaymentStatus(1);
        invoice.setPaidAmount(invoice.getTotalAmount());  // Thanh toán toàn bộ số tiền
        invoiceRepository.save(invoice);  // Lưu lại thay đổi

        // Chuyển hướng về trang danh sách hóa đơn của hợp đồng
        return "redirect:/user/invoices/" + contractId;
    }

    @GetMapping("/detail/{invoiceId}")
    public String viewInvoiceDetail(@PathVariable int invoiceId, Model model) {
        Invoice invoice = invoiceRepository.findById(invoiceId);
        if (invoice == null) {
            return "redirect:/user/invoices";
        }

        // Lấy danh sách Meal Requests
        List<MealRequest> mealRequests = mealRequestRepository.findMealRequestsByInvoiceId(invoiceId);
        List<MealRequest> filteredMealRequests = new ArrayList<>();

        for (MealRequest mealRequest : mealRequests) {
            // Lấy danh sách chi tiết món ăn
            List<MealRequestDetail> details = mealRequestDetailRepository.findByMealRequestId(mealRequest.getId());
            mealRequest.setMealRequestDetails(details);

            // Tính tổng tiền cho từng mealRequest
            BigDecimal totalAmount = details.stream()
                .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            mealRequest.setTotalAmount(totalAmount);

            // Điều kiện: Tổng tiền trùng với hóa đơn
            if (totalAmount.compareTo(invoice.getTotalAmount()) == 0) {
                filteredMealRequests.add(mealRequest);
            }
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("mealRequests", filteredMealRequests);

        return "user/invoice_detail";
    }


    @GetMapping("/invoice/{invoiceId}")
    public String getInvoiceDetail(@PathVariable int invoiceId, Model model) {
        // Lấy dữ liệu hóa đơn
        Invoice invoice = invoiceRepository.findById(invoiceId);
        if (invoice == null) {
            model.addAttribute("error", "Invoice not found");
            return "error";
        }

        // Lấy danh sách Meal Requests liên quan
        List<MealRequest> mealRequests = mealRequestRepository.findMealRequestsByInvoiceId(invoiceId);
        for (MealRequest mealRequest : mealRequests) {
            // Thêm các chi tiết món ăn vào từng Meal Request
            mealRequest.setMealRequestDetails(mealRequestDetailRepository.findByMealRequestId(mealRequest.getId()));
        }

        // Truyền dữ liệu sang giao diện
        model.addAttribute("invoice", invoice);
        model.addAttribute("mealRequests", mealRequests);
        return "invoice_detail"; // Tên file HTML
    }

}
