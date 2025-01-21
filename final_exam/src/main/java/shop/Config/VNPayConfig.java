package shop.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class VNPayConfig {
    // Cấu hình VNPay
    public static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String vnp_TmnCode = "5ZE6ZJZ6";
    public static final String vnp_HashSecret = "FI82AEZOW3SZ35S5CENAQQ4G45MPIR4W";
    public static final String vnp_apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    // Lấy URL return theo loại thanh toán
    public static String getReturnUrl(String contractId, boolean isDeposit) {
        if (isDeposit) {
            return "http://localhost:8080/user/contracts/" + contractId + "/payment-callback";
        } else {
            return "http://localhost:8080/user/invoices/" + contractId + "/pay/vnpay-payment-return";
        }
    }

    // Hàm tạo hash cho VNPay
    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames); // Sắp xếp theo thứ tự tăng dần

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty() && !fieldName.equals("vnp_SecureHash")) {
                hashData.append(fieldName).append('=').append(fieldValue).append('&');
            }
        }

        // Loại bỏ ký tự `&` cuối cùng
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        // Log để kiểm tra
        System.out.println("Data to hash: " + hashData);

        // Tạo HMAC SHA512
        return hmacSHA512(vnp_HashSecret, hashData.toString());
    }

    public static boolean validateSignature(Map<String, String> fields, String secureHash) {
        // Loại bỏ vnp_SecureHash khỏi các tham số cần băm
        fields.remove("vnp_SecureHash");
        
        // Tạo lại chữ ký từ các tham số
        String data = hashAllFields(fields);
        String calculatedHash = hmacSHA512(vnp_HashSecret, data);

        // So sánh chữ ký nhận được với chữ ký tính toán
        return secureHash.equalsIgnoreCase(calculatedHash);
    }



    // Hàm tạo HMAC SHA512
    public static String hmacSHA512(final String key, final String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hashBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Chuyển đổi byte array thành chuỗi hex
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error creating HMAC SHA-512", ex);
        }
    }

    // Lấy địa chỉ IP của client
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    // Tạo số ngẫu nhiên
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(rnd.nextInt(10)); // Chỉ sử dụng các chữ số từ 0-9
        }
        return sb.toString();
    }
}
