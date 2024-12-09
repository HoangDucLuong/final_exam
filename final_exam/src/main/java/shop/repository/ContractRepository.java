package shop.repository;

import shop.model.Contract;
import shop.model.Menu; // Thay đổi từ Meal thành Menu

import java.util.List;

public interface ContractRepository {
    // Lấy tất cả hợp đồng (dành cho admin)
    List<Contract> getAllContracts();

    // Lấy tất cả hợp đồng (dành cho admin)
    List<Contract> findAll();  // Phương thức mới

    // Lấy hợp đồng theo ID của user (dành cho user)
    List<Contract> getContractsByUserId(int usrId);
    
    List<Menu> findMenusByContractId(int contractId); // Thay đổi từ Meal thành Menu

    // Lấy chi tiết hợp đồng theo ID hợp đồng (dùng cho cả user và admin)
    Contract getContractById(int id);

    // Thêm mới hợp đồng
    void addContract(Contract contract);

    // Cập nhật hợp đồng
    void updateContract(Contract contract);

    // Xóa hợp đồng
    void deleteContract(int id);

    // Cập nhật trạng thái hợp đồng (dùng để hủy hợp đồng)
    void updateContractStatus(int contractId, int status);
    
    // Lưu hợp đồng (có thể là thêm mới hoặc cập nhật)
    void save(Contract contract);
    
    List<Contract> getContractsExpiringSoon();
    List<Contract> findActiveContracts();
    Contract findById(int id);  // Thêm dòng này
}
