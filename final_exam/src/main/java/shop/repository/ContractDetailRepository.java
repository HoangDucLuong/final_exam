package shop.repository;

import java.util.List;
import shop.model.ContractDetail;

public interface ContractDetailRepository {
    void saveContractDetail(ContractDetail contractDetail); // Sửa tên phương thức
    List<ContractDetail> getContractDetailsByContractId(int contractId);
    void deleteContractDetailsByContractId(int contractId);
}
