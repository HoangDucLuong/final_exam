package shop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import shop.model.Contract;
import shop.model.Invoice;

@Service
public class MailService {

	@Autowired
	private JavaMailSender mailSender;

	public boolean sendContractExpiryMail(String recipientEmail, Contract contract) {
		try {
			String subject = "Thông Báo Hợp Đồng Sắp Hết Hạn";
			String body = String.format(
					"Kính chào, hợp đồng có ID %d của bạn sẽ hết hạn vào %s. "
							+ "Vui lòng gia hạn sớm để không gián đoạn dịch vụ.",
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

	public boolean sendOtpMail(String recipientEmail, String otpCode) {
		try {
			String subject = "OTP Verification Code";
			String body = String.format("Your OTP code is: %s. It is valid for 5 minutes.", otpCode);

			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(recipientEmail);
			message.setSubject(subject);
			message.setText(body);

			mailSender.send(message);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean sendContractUpdateMail(String recipientEmail, Contract contract) {
		try {
			String subject = "Cập Nhật Trạng Thái Hợp Đồng";
			String body = String.format(
					"Xin chào, hợp đồng của bạn có ID %d đã được cập nhật. \n" + "Trạng thái hiện tại: %s \n"
							+ "Trạng thái thanh toán: %s \n" + "Vui lòng liên hệ nếu có bất kỳ câu hỏi nào.",
					contract.getId(), getStatusDescription(contract.getStatus()),
					contract.getPaymentStatus() == 1 ? "Đã Thanh Toán" : "Chưa Thanh Toán");

			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(recipientEmail);
			message.setSubject(subject);
			message.setText(body);

			// Gửi email
			mailSender.send(message);
			return true; // Email đã được gửi thành công
		} catch (Exception e) {
			return false; // Gửi email thất bại
		}
	}

<<<<<<< HEAD
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(message);
            System.out.println("Email đã được gửi tới: " + recipientEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace(); // Log lỗi chi tiết
            return false;
        }
    }
=======
	private String getStatusDescription(int status) {
		return switch (status) {
		case 0 -> "Đang chờ";
		case 1 -> "Đã xác nhận";
		case 2 -> "Đang thực hiện";
		case 3 -> "Đã hoàn thành";
		case 4 -> "Đã hủy";
		default -> "Không xác định";
		};
	}

	public boolean sendInvoiceNotification(String recipientEmail, Invoice invoice) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
>>>>>>> branch 'main' of https://github.com/HoangDucLuong/final_exam

			helper.setTo(recipientEmail);
			helper.setSubject("Hóa đơn thanh toán #" + invoice.getId());
			helper.setText(String.format(
					"Kính chào, \n\nHóa đơn của bạn với tổng số tiền %s VND đã được tạo vào ngày %s. Vui lòng thanh toán trước ngày đến hạn: %s.\n\nXin cảm ơn!",
					invoice.getTotalAmount(), invoice.getCreatedAt().toString(), invoice.getDueDate().toString()));

			mailSender.send(message);
			System.out.println("Gửi thông báo hóa đơn thành công tới: " + recipientEmail);
			return true;
		} catch (Exception e) {
			System.err.println("Lỗi khi gửi email thông báo hóa đơn: " + e.getMessage());
			return false;
		}
	}

	private boolean sendEmail(String recipientEmail, String subject, String body) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(recipientEmail);
			helper.setSubject(subject);
			helper.setText(body);

			mailSender.send(message);
			System.out.println("Email đã được gửi tới: " + recipientEmail);
			return true;
		} catch (Exception e) {
			System.err.println("Lỗi khi gửi email: " + e.getMessage());
			e.printStackTrace(); // Log lỗi chi tiết
			return false;
		}
	}

	public boolean sendGroupedInvoiceNotification(String recipientEmail, BigDecimal totalAmount) {
		String subject = "Thông báo tổng số tiền cần thanh toán";
		String body = String
				.format("Kính gửi quý khách,\n\n" + "Tổng số tiền cần thanh toán của quý khách là: %s VND.\n"
						+ "Vui lòng thanh toán trước thời hạn để tránh phát sinh phí phạt.\n\n"
						+ "Trân trọng,\nĐội ngũ hỗ trợ khách hàng.", totalAmount);

		return sendEmail(recipientEmail, subject, body);
	}

}