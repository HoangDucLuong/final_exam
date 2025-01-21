package shop.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import shop.Config.VNPayService;
import shop.model.Contract;
import shop.model.Invoice;
import shop.model.Payment;
import shop.repository.ContractRepository;
import shop.repository.InvoiceRepository;
import shop.repository.PaymentRepository;

@Controller
@RequestMapping("/user/invoices")
public class VNpayController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private ContractRepository contractRepository;
    
    @PostMapping("/{contractId}/pay/vnpay-payment-return")
    public String vnpayReturnPost(@PathVariable("contractId") int contractId,
                                  HttpServletRequest request,
                                  Model model) {
        return processVnpayReturn(contractId, request, model);
    }

    @GetMapping("/{contractId}/pay/vnpay-payment-return")
    public String vnpayReturnGet(@PathVariable("contractId") int contractId,
                                 HttpServletRequest request,
                                 Model model) {
        return processVnpayReturn(contractId, request, model);
    }
    // Phương thức xử lý callback khi VNPay gửi lại
    @PostMapping("/{contractId}/payment-callback")
    public String paymentCallbackPost(@PathVariable("contractId") int contractId,
                                      HttpServletRequest request,
                                      Model model) {
        return processVnpayReturn(contractId, request, model);
    }

    @GetMapping("/{contractId}/payment-callback")
    public String paymentCallbackGet(@PathVariable("contractId") int contractId,
                                     HttpServletRequest request,
                                     Model model) {
        return processVnpayReturn(contractId, request, model);
    }
    private String processVnpayReturn(int contractId, HttpServletRequest request, Model model) {
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String txnRef = request.getParameter("vnp_TxnRef");

        int paymentStatus = 0;

        if ("00".equals(transactionStatus)) {
            paymentStatus = 1;
        }

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String amountStr = request.getParameter("vnp_Amount");

        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDateTime paymentDate = null;

        try {
            if (amountStr != null) {
                totalPrice = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));
            }

            if (paymentTime != null) {
                paymentDate = LocalDateTime.parse(paymentTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi xử lý thông tin thanh toán: " + e.getMessage());
            return "payment/orderfail";
        }

        if (paymentStatus == 1) {
            try {
                Payment payment = new Payment();
                payment.setContractId(contractId);
                payment.setPaymentAmount(totalPrice);
                payment.setPaymentDate(paymentDate);
                payment.setPaymentStatus(1);
                payment.setPaymentMethod("VNPAY");
                payment.setTransactionRef(transactionId);

                paymentRepository.save(payment);
             // Cập nhật trạng thái contract
                Contract contract = contractRepository.findById(contractId);
                if (contract != null) {
                    // Kiểm tra nếu là thanh toán đặt cọc
                    if (contract.getStatus() == 2 && contract.getPaymentStatus() == 0) {
                        contract.setStatus(1); // Chuyển sang trạng thái đã xác nhận
                        contract.setPaymentStatus(1); // Đã thanh toán đặt cọc
                    }
                    // Nếu là thanh toán hóa đơn thông thường
                    else if (contract.getStatus() == 1) {
                        contract.setPaymentStatus(1); // Đã thanh toán
                    }
                    contractRepository.updateContract(contract);
                }
                List<Invoice> invoices = invoiceRepository.findByContractId(contractId);
                Invoice selectedInvoice = null;

                for (Invoice invoice : invoices) {
                    if (invoice.getPaymentStatus() == 0 || invoice.getPaymentStatus() == 2) {
                        BigDecimal remainingAmount = invoice.getTotalAmount()
                                .subtract(invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO);

                        BigDecimal paidAmount = totalPrice.min(remainingAmount);
                        BigDecimal updatedPaidAmount = invoice.getPaidAmount() != null
                                ? invoice.getPaidAmount().add(paidAmount)
                                : paidAmount;

                        invoice.setPaidAmount(updatedPaidAmount);

                        if (updatedPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
                            invoice.setPaymentStatus(1);
                        } else {
                            invoice.setPaymentStatus(2);
                        }

                        invoiceRepository.save(invoice);

                        totalPrice = totalPrice.subtract(paidAmount);
                        selectedInvoice = invoice;

                        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                            break;
                        }
                    }
                }

                model.addAttribute("transactionId", transactionId);
                model.addAttribute("totalPrice", totalPrice != null ? totalPrice : BigDecimal.ZERO);
                model.addAttribute("paymentTime", paymentDate != null ? paymentDate : "Không xác định");
                model.addAttribute("orderInfo", orderInfo != null ? orderInfo : "Không có thông tin");

                model.addAttribute("invoice", selectedInvoice);

                return "payment/ordersuccess";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Không thể lưu thông tin thanh toán: " + e.getMessage());
                return "payment/orderfail";
            }
        } else {
            model.addAttribute("errorMessage", "Thanh toán thất bại. Vui lòng kiểm tra lại.");
            return "payment/orderfail";
        }
    }
    @GetMapping("/{contractId}/payment-history")
    public String getPaymentHistory(
            @PathVariable("contractId") int contractId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortField", defaultValue = "paymentDate") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        int totalRecords = (search != null && !search.isEmpty()) 
            ? paymentRepository.countPaymentsByContractIdAndTransactionRef(contractId, search)
            : paymentRepository.countPaymentsByContractId(contractId);
        
        int totalPages = (int) Math.ceil((double) totalRecords / size);

        // Chọn kiểu sắp xếp cho Sort
        Sort sort = Sort.by(Sort.Order.by(sortField));
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        // Tạo Pageable với phân trang và sắp xếp
        Pageable pageable = PageRequest.of(page, size, sort);
        List<Payment> payments;
        
        if (search != null && !search.isEmpty()) {
            payments = paymentRepository.findByContractIdAndTransactionRefWithPagination(contractId, search, page * size, size);
        } else {
            payments = paymentRepository.findByContractIdWithPagination(contractId, page * size, size);
        }

        model.addAttribute("payments", payments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalRecords);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        // Đảm bảo rằng sortDir là đảo ngược trong các liên kết phân trang
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "payment/history";
    }


}
