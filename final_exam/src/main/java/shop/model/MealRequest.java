package shop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealRequest {
    private int id;
    private int contractId;
    private LocalDate requestDate;
    private LocalDate deliveryDate;
    private int totalMeals;
    private int status;
    private String statusText; // Thêm thuộc tính statusText để hiển thị trạng thái dạng chữ
    private User user; // Thuộc tính User cho thông tin người dùng
    private Contract contract; // Thuộc tính Contract cho thông tin hợp đồng
    private List<MealRequestDetail> mealRequestDetails = new ArrayList<>(); // Danh sách chi tiết yêu cầu suất ăn
    private Integer userId;
    private BigDecimal totalAmount;
    private String userName; // Thêm thuộc tính tên user
    private String userEmail; // Thêm thuộc tính email user


    
    // Getters và Setters
    
    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public int getTotalMeals() {
        return totalMeals;
    }

    public void setTotalMeals(int totalMeals) {
        this.totalMeals = totalMeals;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() { // Thêm phương thức getter cho statusText
        return statusText;
    }

    public void setStatusText(String statusText) { // Thêm phương thức setter cho statusText
        this.statusText = statusText;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Contract getContract() { // Thêm phương thức getter cho contract
        return contract;
    }

    public void setContract(Contract contract) { // Thêm phương thức setter cho contract
        this.contract = contract;
    }

    public List<MealRequestDetail> getMealRequestDetails() {
        return mealRequestDetails;
    }

    public void setMealRequestDetails(List<MealRequestDetail> mealRequestDetails) {
        this.mealRequestDetails = mealRequestDetails;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
