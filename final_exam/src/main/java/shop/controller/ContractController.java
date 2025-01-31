package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.Config.VNPayService;
import shop.model.Contract;
import shop.model.ContractDetail;
import shop.model.MenuDetails;
import shop.model.Payment;
import shop.model.User;
import shop.model.Menu;

import shop.repository.ContractRepository;
import shop.repository.ContractDetailRepository;
import shop.repository.MenuDetailsRepository;
import shop.repository.MenuRepository;
import shop.repository.PaymentRepository;
import shop.repository.UserRepository;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Phrase;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Controller
@RequestMapping("/user")
public class ContractController {

	@Autowired
	private ContractRepository contractRepository;

	@Autowired
	private ContractDetailRepository contractDetailRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MenuDetailsRepository menuDetailsRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private MenuRepository menuRepository;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
    private VNPayService vnPayService;
	@GetMapping("/contracts")
	public String getAllContractsForUser(HttpServletRequest request, Model model) {
	    String email = (String) request.getSession().getAttribute("user");
	    if (email != null) {
	        Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
	        if (userOptional.isPresent()) {
	            User user = userOptional.get();
	            List<Contract> userContracts = contractRepository.getContractsByUserId(user.getId());

	            // Sắp xếp hợp đồng theo ID (mới nhất lên đầu)
	            userContracts.sort(Comparator.comparingInt(Contract::getId).reversed());

	            model.addAttribute("contracts", userContracts);

	            List<Menu> menus = menuRepository.getAllMenus();
	            model.addAttribute("menus", menus);

	            return "user/contracts";
	        }
	    }
	    
	    return "redirect:/user/login";
	}


	@GetMapping("/contracts/{id}")
	public String getContractById(@PathVariable("id") int id, HttpServletRequest request, Model model) {
	    String email = (String) request.getSession().getAttribute("user");
	    if (email != null) {
	        Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
	        if (userOptional.isPresent()) {
	            User user = userOptional.get();
	            Contract contract = contractRepository.getContractById(id);

	            if (contract == null || contract.getUsrId() != user.getId()) {
	                return "redirect:/user/contracts?error=notfound";
	            }

	            // Lấy tất cả chi tiết hợp đồng
	            List<ContractDetail> contractDetails = contractDetailRepository.getContractDetailsByContractId(id);

	            // Lấy tất cả menu liên kết với hợp đồng này
	            List<Menu> menus = contractRepository.findMenusByContractId(id);

	            // Lấy giá từng menu
	            Map<Integer, BigDecimal> menuPrices = new HashMap<>();
	            for (Menu menu : menus) {
	                BigDecimal price = menuRepository.getMenuPriceById(menu.getId());
	                menuPrices.put(menu.getId(), price);
	            }

	            model.addAttribute("contract", contract);
	            model.addAttribute("contractDetails", contractDetails);
	            model.addAttribute("menus", menus);
	            model.addAttribute("menuPrices", menuPrices);

	            return "user/contract-details";
	        }
	    }
	    return "redirect:/user/login";
	}

	@PostMapping("/cancel")
	public String cancelContract(@RequestParam("contractId") int contractId, HttpServletRequest request, Model model) {
		String email = (String) request.getSession().getAttribute("user");
		if (email != null) {
			Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				Contract contract = contractRepository.getContractById(contractId);
				if (contract != null && contract.getUsrId() == user.getId()) {
					contractRepository.updateContractStatus(contractId, 4); // 4: Cancelled
					model.addAttribute("message", "Hợp đồng đã bị hủy.");
					return "redirect:/user/contracts";
				}
			}
		}
		return "redirect:/user/login";
	}

	@GetMapping("/create-contract")
	public String showCreateContractForm(HttpServletRequest request, Model model) {
		String email = (String) request.getSession().getAttribute("user");
		if (email != null) {
			Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				model.addAttribute("userName", user.getName());
			}
		}

		// Lấy danh sách menu
		List<Menu> menus = menuRepository.getAllMenus();
		model.addAttribute("menus", menus);

		// Tính và truyền giá của từng menu vào model
		Map<Integer, BigDecimal> menuPrices = new HashMap<>();
		for (Menu menu : menus) {
			BigDecimal menuPrice = menuRepository.getMenuPriceById(menu.getId());
			menuPrices.put(menu.getId(), menuPrice);
		}
		model.addAttribute("menuPrices", menuPrices);

		// Log ra menuPrices và menus
		System.out.println("Menus: " + menus);
		System.out.println("Menu Prices: " + menuPrices);

		return "user/create-contract";
	}

	@PostMapping("/create-contract")
	public String createContract(@RequestParam("menuId") List<Integer> menuIds,
	                             @RequestParam("startDate") String startDate,
	                             @RequestParam("contractDuration") Integer contractDuration,
	                             HttpServletRequest request,
	                             Model model) {
	    String email = (String) request.getSession().getAttribute("user");

	    if (email != null) {
	        Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
	        if (userOptional.isPresent()) {
	            User user = userOptional.get();
	            try {
	                if (contractDuration < 6) {
	                    model.addAttribute("error", "Thời gian hợp đồng phải lớn hơn hoặc bằng 6 tháng.");
	                    return "user/create-contract";
	                }
	                if (menuIds == null || menuIds.isEmpty()) {
	                    model.addAttribute("error", "Bạn phải chọn ít nhất một menu.");
	                    return "user/create-contract";
	                }

	                LocalDate start = LocalDate.parse(startDate);
	                LocalDate endDate = start.plusMonths(contractDuration);

	                BigDecimal totalMenuPrice = BigDecimal.ZERO;
	                List<MenuDetails> menuDetails = new ArrayList<>();

	                for (Integer menuId : menuIds) {
	                    List<MenuDetails> details = menuDetailsRepository.findByMenuId(menuId);
	                    if (details.isEmpty()) {
	                        model.addAttribute("error", "Một trong các menu không có chi tiết nào.");
	                        return "user/create-contract";
	                    }
	                    menuDetails.addAll(details);
	                }

	                for (MenuDetails detail : menuDetails) {
	                    BigDecimal mealPrice = detail.getPrice();
	                    totalMenuPrice = totalMenuPrice.add(mealPrice);
	                }

	                BigDecimal daysInMonth = BigDecimal.valueOf(31); // Số ngày trong một tháng
	                BigDecimal totalDays = BigDecimal.valueOf(contractDuration).multiply(daysInMonth);
	                BigDecimal totalContractAmount = totalMenuPrice.multiply(totalDays);
	                BigDecimal depositAmount = totalContractAmount.multiply(BigDecimal.valueOf(0.1));

	                Contract newContract = new Contract();
	                newContract.setUsrId(user.getId());
	                newContract.setStartDate(start);
	                newContract.setEndDate(endDate);
	                newContract.setDepositAmount(depositAmount);
	                newContract.setTotalAmount(totalContractAmount);
	                newContract.setStatus(2);
	                newContract.setPaymentStatus(0);

	                contractRepository.addContract(newContract);

	                for (MenuDetails detail : menuDetails) {
	                    ContractDetail contractDetail = new ContractDetail();
	                    contractDetail.setContractId(newContract.getId());
	                    contractDetail.setMenuId(detail.getMenuId());
	                    contractDetailRepository.saveContractDetail(contractDetail);
	                }

	                String filePath = "src/main/resources/contracts/contract_" + newContract.getId() + ".pdf";
	                generateContractPdf(newContract, user, menuDetails, filePath);
	                sendContractEmail(user.getEmail(), filePath);

	                return "redirect:/user/contracts";
	            } catch (Exception e) {
	                e.printStackTrace();
	                model.addAttribute("error", "Đã có lỗi xảy ra khi tạo hợp đồng: " + e.getMessage());
	                return "user/create-contract";
	            }
	        }
	    }
	    return "redirect:/user/login";
	}


	 private void generateContractPdf(Contract contract, User user, List<MenuDetails> menuDetails, String filePath) throws DocumentException, IOException {
		    Document document = new Document();
		    File file = new File(filePath);
		    file.getParentFile().mkdirs();
		    PdfWriter.getInstance(document, new FileOutputStream(file));

		    // Nhúng font Arial Unicode MS vào PDF
		    BaseFont baseFont = BaseFont.createFont("c:\\windows\\fonts\\arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		    Font vietnameseFont = new Font(baseFont, 12);

		    document.open();

		    // Tiêu đề hợp đồng
		    Paragraph title = new Paragraph("Hợp đồng dịch vụ công nghiệp", vietnameseFont);
		    title.setAlignment(Element.ALIGN_CENTER);
		    title.setSpacingAfter(10); // Khoảng cách sau tiêu đề
		    document.add(title);

		    // Thêm thông tin hợp đồng cơ bản
		    document.add(new Paragraph("Người dùng: " + user.getName(), vietnameseFont));
		    document.add(new Paragraph("Email: " + user.getEmail(), vietnameseFont));
		    document.add(new Paragraph("Ngày bắt đầu: " + contract.getStartDate(), vietnameseFont));
		    document.add(new Paragraph("Ngày kết thúc: " + contract.getEndDate(), vietnameseFont));
		    document.add(new Paragraph("Tổng số tiền: " + contract.getTotalAmount(), vietnameseFont));
		    document.add(new Paragraph("Số tiền đặt cọc: " + contract.getDepositAmount(), vietnameseFont));
		    document.add(new Paragraph(" ", vietnameseFont));

		    // Bảng menu với tên và giá
		    PdfPTable table = new PdfPTable(2);  // Cột cho Tên Menu và Giá
		    table.addCell(new Phrase("Tên Menu", vietnameseFont));
		    table.addCell(new Phrase("Giá", vietnameseFont));

		    for (MenuDetails detail : menuDetails) {
		        // Lấy thông tin Menu từ ID
		        Menu menu = menuRepository.getMenuById(detail.getMenuId());

		        // Thêm tên menu và giá vào bảng
		        table.addCell(new Phrase(menu.getMenuName(), vietnameseFont));
		        table.addCell(new Phrase(detail.getPrice().toString(), vietnameseFont));
		    }

		    document.add(table);
		    document.close();

		    System.out.println("PDF được tạo tại: " + filePath);
		}

	    private void sendContractEmail(String recipientEmail, String filePath) throws MessagingException {
	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);

	        helper.setTo(recipientEmail);
	        helper.setSubject("Chi tiết hợp đồng của bạn");
	        helper.setText("Vui lòng kiểm tra file đính kèm để xem chi tiết hợp đồng của bạn.");

	        File file = new File(filePath);
	        helper.addAttachment(file.getName(), file);

	        mailSender.send(message);
	        System.out.println("Email được gửi đến: " + recipientEmail);
	    }
	
	    @GetMapping("/contracts/pdf/{contractId}")
	    @ResponseBody
	    public void viewContractPdf(@PathVariable("contractId") int contractId, HttpServletResponse response) {
	        String filePath = "src/main/resources/contracts/contract_" + contractId + ".pdf";
	        File file = new File(filePath);

	        if (!file.exists() || file.length() == 0) {
	            try {
	                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File không tồn tại hoặc rỗng");
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            return;
	        }

	        try (FileInputStream fileInputStream = new FileInputStream(file);
	        	OutputStream responseOutputStream = response.getOutputStream()) {

	            response.setContentType("application/pdf");
	            response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
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
	    @GetMapping("/contracts/confirm/{id}")
	    public String confirmContract(@PathVariable("id") int id, HttpServletRequest request) {
	        String email = (String) request.getSession().getAttribute("user");
	        if (email != null) {
	            Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
	            if (userOptional.isPresent()) {
	                Contract contract = contractRepository.getContractById(id);
	                
	                if (contract != null && contract.getStatus() == 0) {
	                    // Lấy baseUrl từ request
	                    String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
	                 // Xác định rằng đây là thanh toán đặt cọc
	                    boolean isDeposit = true;

	                    // Tạo URL thanh toán VNPay với số tiền đặt cọc
	                    String paymentUrl = vnPayService.createOrder(
	                        request,
	                        contract.getDepositAmount().intValue(), // Chuyển BigDecimal sang int
	                        contract.getId(),
	                        isDeposit, // Truyền giá trị isDeposit
	                        baseUrl
	                    );
	                    
	                    return "redirect:" + paymentUrl;
	                }
	            }
	        }
	        return "redirect:/user/contracts";
	    }
	    @GetMapping("/contracts/{id}/payment-callback")
	    public String handlePaymentCallbackGet(@PathVariable("id") int id, HttpServletRequest request, Model model) {
	        try {
	            int paymentResult = vnPayService.orderReturn(request);
	            
	            if (paymentResult == 1) { // Thanh toán thành công
	                Contract contract = contractRepository.getContractById(id);
	                if (contract != null) {
	                    // Lấy số tiền từ VNPay response
	                    String vnpAmount = request.getParameter("vnp_Amount");
	                    BigDecimal amount = new BigDecimal(vnpAmount).divide(new BigDecimal(100));
	                    
	                    // Lưu thông tin thanh toán trước
	                    Payment payment = new Payment();
	                    payment.setContractId(id);
	                    payment.setPaymentAmount(amount);
	                    payment.setPaymentDate(LocalDateTime.now());
	                    payment.setPaymentMethod("VNPAY");
	                    payment.setPaymentStatus(1);
	                    payment.setTransactionRef(request.getParameter("vnp_TransactionNo"));
	                    paymentRepository.save(payment);
	                    
	                    // Cập nhật trạng thái hợp đồng sau khi lưu thanh toán thành công
	                    if (contract.getStatus() == 0 && contract.getPaymentStatus() == 0) {
	                        contract.setStatus(1); // Chuyển sang trạng thái đã xác nhận
	                        contract.setPaymentStatus(1); // Đã thanh toán đặt cọc
	                        contractRepository.updateContract(contract);
	                        return "payment/successs";
	                    }
	                }
	                return "payment/successs";
	            } else if (paymentResult == 0) {
	                return "payment/orderfailed";
	            } else {
	                return "redirect:/user/contracts?error=invalid_signature";
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            return "redirect:/user/contracts?error=system_error";
	        }
	    }
	    @PostMapping("/contracts/{id}/payment-callback")
	    public String handlePaymentCallback(@PathVariable("id") int id, HttpServletRequest request) {
	        int paymentResult = vnPayService.orderReturn(request);
	        
	        if (paymentResult == 1) {
	            // Thanh toán thành công
	            Contract contract = contractRepository.getContractById(id);
	            if (contract != null) {
	                contract.setStatus(1); // Cập nhật trạng thái hợp đồng thành "Đã xác nhận"
	                contract.setPaymentStatus(1); // Cập nhật trạng thái thanh toán
	                contractRepository.updateContract(contract);
	                return "redirect:/user/contracts?success=payment";
	            }
	        } else if (paymentResult == 0) {
	            // Thanh toán thất bại
	            return "redirect:/user/contracts?error=payment_failed";
	        } else {
	            // Sai chữ ký
	            return "redirect:/user/contracts?error=invalid_signature";
	        }
	        
	        return "redirect:/user/contracts";
	    }
	@PostMapping("/contracts/save")
	public String saveContract(@RequestParam(value = "menuId", required = false) Integer menuId,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate,
			@RequestParam("totalAmount") BigDecimal totalAmount, HttpServletRequest request) {

		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			Optional<User> userOptional = Optional.ofNullable(userRepository.findUserByEmail(email));
			if (userOptional.isPresent()) {
				User user = userOptional.get();

				Contract contract = new Contract();
				contract.setUsrId(user.getId());
				contract.setStartDate(LocalDate.parse(startDate));
				contract.setEndDate(LocalDate.parse(endDate));
				contract.setTotalAmount(totalAmount);
				contract.setStatus(0);
				contractRepository.save(contract);

				if (menuId != null && menuId > 0) {
					List<MenuDetails> menuDetails = menuDetailsRepository.findByMenuId(menuId);
					for (MenuDetails detail : menuDetails) {
						ContractDetail contractDetail = new ContractDetail();
						contractDetail.setContractId(contract.getId());
						contractDetail.setMenuId(detail.getMenuId());

						contractDetailRepository.saveContractDetail(contractDetail);
					}
				}

				return "redirect:/user/contracts";
			}
		}
		return "redirect:/user/login";
	}
}