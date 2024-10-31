package shop.model;

public class ContractDetail {
    private int id;
    private int contractId;
    private int menuId;
    private String description;
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
	public int getMenuId() {
		return menuId;
	}
	public void setMenuId(int mealId) {
		this.menuId = mealId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

    
    
}

