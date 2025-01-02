package shop.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import shop.Config.VNPayService;
import shop.model.Invoice;
import shop.model.Payment;
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
    
}
