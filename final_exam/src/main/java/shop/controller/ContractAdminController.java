package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import shop.model.Contract;
import shop.model.User;
import shop.repository.ContractRepository;
import shop.repository.UserRepository;
import shop.service.MailService;
import shop.repository.ContractDetailRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/contracts") // Đường dẫn dành riêng cho admin
public class ContractAdminController {

	@Autowired
	private ContractRepository contractRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContractDetailRepository contractDetailRepository;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private MailService mailService;

	@GetMapping("")
	public String getAllContractsForAdmin(@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "search", required = false) String search, Model model) {

		String keyword = search != null ? search : "";
		int offset = (page - 1) * size;
		List<Contract> contracts = contractRepository.searchContracts(keyword, offset, size);
		List<Map<String, Object>> contractsWithUser = new ArrayList<>();
		int totalContracts = contractRepository.countContracts(keyword);
		int totalPages = (int) Math.ceil((double) totalContracts / size);

		LocalDate today = LocalDate.now();

		for (Contract contract : contracts) {
			User user = userRepository.findById(contract.getUsrId());
			Map<String, Object> contractWithUser = new HashMap<>();
			contractWithUser.put("contract", contract);
			contractWithUser.put("userName", user != null ? user.getName() : "N/A"); // Lấy tên người dùng

			// Check if the contract is expiring within 1 month
			long daysToExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, contract.getEndDate());
			contractWithUser.put("isExpiringSoon", daysToExpiry <= 30 && daysToExpiry > 0);

			contractsWithUser.add(contractWithUser);
		}

		model.addAttribute("contracts", contractsWithUser);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", keyword);

		return "admin/contract/contracts"; // Return the contract list page
	}

	// Chi tiết hợp đồng cho admin
	@GetMapping("/{id}")
	public String getContractByIdForAdmin(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/admin/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}
		model.addAttribute("contract", contract);
		return "admin/contract/contract-details"; // Trả về trang chi tiết hợp đồng cho admin
	}

	// Hiển thị trang chỉnh sửa hợp đồng
	@GetMapping("/edit/{id}")
	public String showEditContractForm(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/admin/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}
		model.addAttribute("contract", contract);
		return "admin/contract/edit_contract"; // Trả về trang chỉnh sửa hợp đồng
	}

	@PostMapping("/update/{id}")

	public String updateContract(@PathVariable("id") int id, @ModelAttribute Contract contract,
			@RequestParam("pdfFile") MultipartFile pdfFile, Model model) throws IOException {

		Contract existingContract = contractRepository.getContractById(id);

		if (existingContract == null) {
			return "redirect:/admin/contracts?error=notfound";
		}

		// Xử lý upload file PDF
		if (!pdfFile.isEmpty()) {
			// Validate file type
			if (!pdfFile.getContentType().equalsIgnoreCase("application/pdf")) {
				model.addAttribute("error", "Chỉ chấp nhận file PDF");
				return "admin/contract/edit_contract";
			}

			// Validate file size (max 5MB)
			if (pdfFile.getSize() > 5 * 1024 * 1024) {
				model.addAttribute("error", "Kích thước file tối đa 5MB");
				return "admin/contract/edit_contract";
			}

			// Tạo thư mục nếu chưa tồn tại
			Path uploadDir = Paths.get("src/main/resources/contracts");
			if (!Files.exists(uploadDir)) {
				Files.createDirectories(uploadDir);
			}

			// Tạo tên file theo ID hợp đồng
			String fileName = "contract_" + id + ".pdf";
			Path filePath = uploadDir.resolve(fileName);

			// Lưu file và ghi đè nếu tồn tại
			Files.copy(pdfFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
		}

		// Cập nhật thông tin hợp đồng
		existingContract.setUsrId(contract.getUsrId());
		existingContract.setStartDate(contract.getStartDate());
		existingContract.setEndDate(contract.getEndDate());
		existingContract.setTotalAmount(contract.getTotalAmount());
		existingContract.setDepositAmount(contract.getDepositAmount());
		existingContract.setStatus(contract.getStatus());
		existingContract.setPaymentStatus(contract.getPaymentStatus());

		// Lưu hợp đồng đã cập nhật vào cơ sở dữ liệu
		contractRepository.updateContract(existingContract);

		// Gửi email thông báo
		User user = userRepository.findById(contract.getUsrId());
		if (user != null) {
			mailService.sendContractUpdateMail(user.getEmail(), existingContract);
		}

		return "redirect:/admin/contracts"; // Quay lại trang danh sách hợp đồng của admin
	}

	// Xác nhận hợp đồng (POST) - đổi tên thành confirmContractPost
	@RequestMapping(value = "/confirm/{id}", method = { RequestMethod.GET, RequestMethod.POST })
	public String confirmContract(@PathVariable("id") int id, Model model) {
		Contract contract = contractRepository.getContractById(id);
		if (contract == null) {
			return "redirect:/admin/contract/contracts?error=notfound"; // Xử lý khi không tìm thấy hợp đồng
		}
		contract.setStatus(1); // Đặt trạng thái là đã xác nhận
		contractRepository.updateContract(contract); // Cập nhật trạng thái trong cơ sở dữ liệu

		return "redirect:/user/contracts"; // Chuyển hướng về trang danh sách hợp đồng của admin
	}

	// Admin xóa hợp đồng
	@GetMapping("/delete/{id}")
	public String deleteContract(@PathVariable("id") int id) {
		Contract existingContract = contractRepository.getContractById(id);
		if (existingContract != null) {
			contractRepository.deleteContract(id);
		}
		return "redirect:/admin/contracts"; // Quay lại trang danh sách hợp đồng của admin
	}

	// Admin tạo hợp đồng mới
	@GetMapping("/create")
	public String showCreateContractFormForAdmin(@RequestParam("usrId") int usrId, Model model) {
		// Lấy người dùng từ cơ sở dữ liệu bằng usrId
		User user = userRepository.findById(usrId);

		// Kiểm tra xem người dùng có tồn tại không
		if (user == null) {
			return "redirect:/admin/contract/contracts?error=usernotfound"; // Nếu không tìm thấy người dùng
		}

		// Tạo đối tượng hợp đồng và gán thông tin người dùng
		Contract contract = new Contract();
		contract.setUsrId(user.getId());

		// Thêm người dùng và hợp đồng vào model để hiển thị trong form
		model.addAttribute("user", user);
		model.addAttribute("contract", contract);

		return "admin/contract/create-contract"; // Trả về trang tạo hợp đồng cho admin
	}

	@PostMapping("/create")
	public String createContractForAdmin(@RequestParam("usrId") int usrId,
			@RequestParam("startDate") LocalDate startDate, @RequestParam("contractDuration") int contractDuration,
			@RequestParam("depositAmount") BigDecimal depositAmount, Model model) {
		try {
			// Tính toán ngày kết thúc
			LocalDate endDate = startDate.plusMonths(contractDuration);

			// Lấy thông tin người dùng
			User user = userRepository.findById(usrId);
			if (user == null) {
				model.addAttribute("error", "Không tìm thấy người dùng");
				return "admin/contract/create-contract";
			}

			// Tính toán tổng số tiền
			BigDecimal daysInMonth = BigDecimal.valueOf(31);
			BigDecimal totalDays = BigDecimal.valueOf(contractDuration).multiply(daysInMonth);

			// Giả sử tính toán tổng số tiền dựa trên một mức giá cơ bản
			BigDecimal dailyRate = BigDecimal.valueOf(100000); // Ví dụ: 100,000 VND/ngày
			BigDecimal totalContractAmount = dailyRate.multiply(totalDays);

			// Tạo đối tượng hợp đồng mới
			Contract newContract = new Contract();
			newContract.setUsrId(usrId);
			newContract.setStartDate(startDate);
			newContract.setEndDate(endDate);
			newContract.setDepositAmount(depositAmount);
			newContract.setTotalAmount(totalContractAmount);
			newContract.setStatus(0); // Trạng thái ban đầu là Pending
			newContract.setPaymentStatus(0); // Trạng thái thanh toán là Unpaid

			// Lưu hợp đồng vào cơ sở dữ liệu
			contractRepository.addContract(newContract);

			// Tạo file PDF
			String filePath = "src/main/resources/contracts/contract_" + newContract.getId() + ".pdf";
			generateContractPdf(newContract, user, filePath);

			// Gửi email
			sendContractEmail(user.getEmail(), filePath);

			return "redirect:/admin/contracts";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "Đã có lỗi xảy ra khi tạo hợp đồng: " + e.getMessage());
			return "admin/contract/create-contract";
		}
	}

	private void generateContractPdf(Contract contract, User user, String filePath)
			throws DocumentException, IOException {
		Document document = new Document();
		File file = new File(filePath);
		file.getParentFile().mkdirs();
		PdfWriter.getInstance(document, new FileOutputStream(file));

		// Nhúng font Arial Unicode MS vào PDF
		BaseFont baseFont = BaseFont.createFont("c:\\windows\\fonts\\arial.ttf", BaseFont.IDENTITY_H,
				BaseFont.EMBEDDED);
		Font vietnameseFont = new Font(baseFont, 12);

		document.open();

		// Tiêu đề hợp đồng
		Paragraph title = new Paragraph("Hợp đồng dịch vụ (Quản trị viên)", vietnameseFont);
		title.setAlignment(Element.ALIGN_CENTER);
		title.setSpacingAfter(10);
		document.add(title);

		// Thêm thông tin hợp đồng cơ bản
		document.add(new Paragraph("Người dùng: " + user.getName(), vietnameseFont));
		document.add(new Paragraph("Email: " + user.getEmail(), vietnameseFont));
		document.add(new Paragraph("Ngày bắt đầu: " + contract.getStartDate(), vietnameseFont));
		document.add(new Paragraph("Ngày kết thúc: " + contract.getEndDate(), vietnameseFont));
		document.add(new Paragraph("Tổng số tiền: " + contract.getTotalAmount(), vietnameseFont));
		document.add(new Paragraph("Số tiền đặt cọc: " + contract.getDepositAmount(), vietnameseFont));

		document.close();
		System.out.println("PDF được tạo tại: " + filePath);
	}

	private void sendContractEmail(String recipientEmail, String filePath) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setTo(recipientEmail);
		helper.setSubject("Hợp đồng mới được tạo bởi quản trị viên");
		helper.setText("Vui lòng kiểm tra file đính kèm để xem chi tiết hợp đồng.");

		File file = new File(filePath);
		helper.addAttachment(file.getName(), file);

		mailSender.send(message);
		System.out.println("Email được gửi đến: " + recipientEmail);
	}

	// Thêm method để xem PDF cho admin
	@GetMapping("/pdf/{id}")
	@ResponseBody
	public void viewContractPdf(@PathVariable("id") int contractId, HttpServletResponse response) {
		String filePath = "src/main/resources/contracts/contract_" + contractId + ".pdf";
		File file = new File(filePath);

		if (!file.exists() || file.length() == 0) {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy file PDF");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		try (FileInputStream fileInputStream = new FileInputStream(file);
				OutputStream responseOutputStream = response.getOutputStream()) {

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "inline; filename=contract_" + contractId + ".pdf");
			response.setContentLength((int) file.length());

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				responseOutputStream.write(buffer, 0, bytesRead);
			}

			responseOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PostMapping("/sendMail/{id}")
	public String sendExpiryNotification(@PathVariable("id") int contractId, Model model) {
		System.out.println("Gửi thông báo cho hợp đồng ID: " + contractId); // log thông tin hợp đồng

		Contract contract = contractRepository.getContractById(contractId);
		if (contract != null) {
			User user = userRepository.findById(contract.getUsrId());
			if (user != null) {
				// Gửi email
				mailService.sendContractExpiryMail(user.getEmail(), contract);
				model.addAttribute("mailStatus", "success");
				model.addAttribute("successMessage", "Email đã được gửi thành công!");
			} else {
				model.addAttribute("mailStatus", "failure");
				model.addAttribute("errorMessage", "Người dùng không tồn tại.");
			}
		} else {
			model.addAttribute("mailStatus", "failure");
			model.addAttribute("errorMessage", "Hợp đồng không tồn tại.");
		}
		return "redirect:/admin/contracts"; // Quay lại trang danh sách hợp đồng
	}

}
