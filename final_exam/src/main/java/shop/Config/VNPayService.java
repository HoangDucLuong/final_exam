package shop.Config;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.Contract;
import shop.model.Payment;
import shop.repository.ContractRepository;
import shop.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPayService {
    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;  // Thêm ContractRepository

    @Autowired
    public VNPayService(PaymentRepository paymentRepository, ContractRepository contractRepository) {
        this.paymentRepository = paymentRepository;
        this.contractRepository = contractRepository;    // Inject ContractRepository
    }

    public String createOrder(HttpServletRequest request, int amount, int contractId, boolean isDeposit, String baseUrl){
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "bill_payment";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "ContractID:" + contractId);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");

     // Giả sử bạn có tham số isDeposit để biết loại thanh toán
        String urlReturn;
        if (isDeposit) {
            urlReturn = baseUrl + "/user/contracts/" + contractId + "/payment-callback";
        } else {
            urlReturn = baseUrl + "/user/invoices/" + contractId + "/pay/vnpay-payment-return";
        }

//        String urlReturn = baseUrl + "/user/invoices/" + contractId + "/pay/vnpay-payment-return";
//        vnp_Params.put("vnp_ReturnUrl", urlReturn);
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        // Đưa URL trả về vào tham số VNPay
        vnp_Params.put("vnp_ReturnUrl", urlReturn);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);


        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Tạo danh sách các trường và sắp xếp theo thứ tự
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        // Tạo chuỗi dữ liệu để hash và query string
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                hashData.append(fieldName).append('=').append(encodeValue(fieldValue));
                query.append(encodeValue(fieldName)).append('=').append(encodeValue(fieldValue));
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        System.out.println("=== Debugging deposit payment ===");
        System.out.println("Parameters sent to VNPay: " + vnp_Params);
        System.out.println("URL Return: " + urlReturn);
        System.out.println("===============================");

        // Tạo chữ ký bảo mật (SecureHash)
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return VNPayConfig.vnp_PayUrl + "?" + query.toString();
    }

    public int orderReturn(HttpServletRequest request) {
        try {
            Map<String, String> fields = new HashMap<>();
            
            // Debug logging
            System.out.println("====== VNPay Response Parameters ======");
            
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(fieldName, fieldValue);
                    System.out.println(fieldName + ": " + fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            System.out.println("Received SecureHash: " + vnp_SecureHash);
            
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            // Sắp xếp các tham số theo thứ tự
            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);

            // Tạo chuỗi dữ liệu để băm
            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = fields.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(encodeValue(fieldValue));
                    hashData.append('&');
                }
            }

            // Loại bỏ dấu '&' cuối cùng
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
            }

            // Tính toán SecureHash
            String signValue = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
            System.out.println("Calculated SecureHash: " + signValue);
            
            if (signValue.equals(vnp_SecureHash)) {
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
                
                System.out.println("ResponseCode: " + vnp_ResponseCode);
                System.out.println("TransactionStatus: " + vnp_TransactionStatus);
                
                if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                    System.out.println("Payment Successful!");
                    return 1;
                } else {
                    System.out.println("Payment Failed - Invalid Response Code or Transaction Status");
                    return 0;
                }
            } else {
                System.out.println("Payment Failed - Invalid Signature");
                return -1;
            }
        } catch (Exception e) {
            System.out.println("Exception in orderReturn: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.US_ASCII.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
