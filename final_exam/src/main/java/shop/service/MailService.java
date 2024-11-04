package shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import shop.model.Contract;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendContractExpiryMail(String recipientEmail, Contract contract) {
        try {
            String subject = "Thông Báo Hợp Đồng Sắp Hết Hạn";
            String body = String.format("Kính chào, hợp đồng có ID %d của bạn sẽ hết hạn vào %s. " +
                            "Vui lòng gia hạn sớm để không gián đoạn dịch vụ.",
                    contract.getId(), contract.getEndDate().toString());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            // Gửi email
            mailSender.send(message);
            return true; // Email đã được gửi thành công
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            return false; // Gửi email thất bại
        }
    }


}
